package br.ufrn.dimap.api_gateway.managers;

import br.ufrn.dimap.service_discovery.dtos.GetAllServicesResponse;
import br.ufrn.dimap.service_discovery.dtos.GetServicesByNameResponse;
import br.ufrn.dimap.service_discovery.dtos.ServiceResponse;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default {@link IServicesManager} maintaining case-insensitive {@link ServicesCycle} instances.
 */
public class ServicesManager implements IServicesManager {

    private final ConcurrentMap<String, ServicesCycle> serviceCycles = new ConcurrentHashMap<>();

    @Override
    public void updateServices(GetAllServicesResponse response) {
        Set<String> activeServices = new HashSet<>();

        for (GetServicesByNameResponse servicesInfo : response.servicesByName()) {
            String key = normalizeKey(servicesInfo.name());
            activeServices.add(key);

            ServicesCycle cycle = serviceCycles.computeIfAbsent(key, ignored -> new ServicesCycle());
            cycle.addOrUpdateRange(servicesInfo.services());
        }

        serviceCycles.keySet().removeIf(key -> !activeServices.contains(key));
    }

    @Override
    public ServiceResponse getNext(String serviceName) {
        ServicesCycle cycle = serviceCycles.get(normalizeKey(serviceName));
        if (cycle == null) {
            throw new IllegalStateException("No services with name \"" + serviceName + "\" were found.");
        }
        return cycle.getNext();
    }

    @Override
    public void removeService(String serviceName, UUID serviceId) {
        ServicesCycle cycle = serviceCycles.get(normalizeKey(serviceName));
        if (cycle != null) {
            cycle.remove(serviceId);
        }
    }

    private static String normalizeKey(String serviceName) {
        return serviceName == null ? "" : serviceName.toLowerCase(Locale.ROOT);
    }
}
