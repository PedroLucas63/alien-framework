package br.ufrn.dimap.api_gateway.managers;

import br.ufrn.dimap.service_discovery.dtos.ServiceResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe round-robin cycle of instances for a single service name.
 */
public class ServicesCycle {

    private final Lock lock = new ReentrantLock();
    private int currentCycle;
    private final List<UUID> serviceIds = new ArrayList<>();
    private final Map<UUID, ServiceResponse> serviceResponses = new HashMap<>();

    public int count() {
        lock.lock();
        try {
            return serviceIds.size();
        } finally {
            lock.unlock();
        }
    }

    public void addOrUpdate(ServiceResponse serviceResponse) {
        lock.lock();
        try {
            if (!serviceResponses.containsKey(serviceResponse.id())) {
                serviceIds.add(serviceResponse.id());
            }
            serviceResponses.put(serviceResponse.id(), serviceResponse);
        } finally {
            lock.unlock();
        }
    }

    public void addOrUpdateRange(Iterable<ServiceResponse> responses) {
        lock.lock();
        try {
            Set<UUID> activeIds = new HashSet<>();
            for (ServiceResponse response : responses) {
                activeIds.add(response.id());
                if (!serviceResponses.containsKey(response.id())) {
                    serviceIds.add(response.id());
                }
                serviceResponses.put(response.id(), response);
            }

            for (int i = serviceIds.size() - 1; i >= 0; i--) {
                UUID id = serviceIds.get(i);
                if (!activeIds.contains(id)) {
                    serviceIds.remove(i);
                    serviceResponses.remove(id);
                }
            }

            normalizeCycle();
        } finally {
            lock.unlock();
        }
    }

    public void remove(UUID serviceId) {
        lock.lock();
        try {
            if (serviceResponses.remove(serviceId) == null) {
                return;
            }
            serviceIds.remove(serviceId);
            normalizeCycle();
        } finally {
            lock.unlock();
        }
    }

    public ServiceResponse getNext() {
        lock.lock();
        try {
            if (serviceIds.isEmpty()) {
                throw new IllegalStateException("No services were found.");
            }
            int index = currentCycle % serviceIds.size();
            currentCycle = (index + 1) % serviceIds.size();
            return serviceResponses.get(serviceIds.get(index));
        } finally {
            lock.unlock();
        }
    }

    private void normalizeCycle() {
        if (serviceIds.isEmpty()) {
            currentCycle = 0;
        } else {
            currentCycle %= serviceIds.size();
        }
    }
}
