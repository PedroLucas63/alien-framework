package br.ufrn.dimap.service_discovery.services;

import br.ufrn.dimap.service_discovery.configs.ServiceDiscoveryServerConfig;
import br.ufrn.dimap.service_discovery.domain.RegisteredServiceInstance;
import br.ufrn.dimap.service_discovery.dtos.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceRegistry implements IServiceRegistry {
    private final ServiceDiscoveryServerConfig config;
    private final ConcurrentMap<UUID, RegisteredServiceInstance> registry;
    private final Lock registerLock;

    public ServiceRegistry(ServiceDiscoveryServerConfig config) {
        this.config = config;
        this.registry = new ConcurrentHashMap<>();
        this.registerLock = new ReentrantLock();
    }

    @Override
    public RegisterServiceResponse register(RegisterServiceRequest request) {
        if (!isPasswordValid(request.password())) {
            return new RegisterServiceResponse(false, "Passwords do not match");
        }

        if (
                request.name() == null || request.name().isBlank() ||
                request.hostname() == null || request.hostname().isBlank() ||
                request.port() <= 0 || request.port() > 65535
        ) {
            return new RegisterServiceResponse(false, "Invalid service registration data");
        }

        RegisteredServiceInstance instance;
        registerLock.lock();
        try {
            Optional<RegisteredServiceInstance> existing = tryGetService(
                    request.name(), request.hostname(), request.port()
            );
            if (existing.isPresent()) {
                instance = existing.get();
                instance.updateHeartbeat();
            } else {
                instance = new RegisteredServiceInstance(
                        UUID.randomUUID(),
                        request.name(),
                        request.hostname(),
                        request.port()
                );
                registry.put(instance.id, instance);
            }
        } finally {
            registerLock.unlock();
        }

        return new RegisterServiceResponse(
                true,
                "The service is registered",
                instance.id
        );
    }

    private Optional<RegisteredServiceInstance> tryGetService(String serviceName, String hostname, int port) {
        return registry.values().stream()
                .filter(s -> s.serviceName.equalsIgnoreCase(serviceName)
                        && s.hostname.equalsIgnoreCase(hostname)
                        && s.port == port)
                .findFirst();
    }

    @Override
    public GetServicesByNameResponse getByName(GetServicesByNameRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            String name = request != null && request.name() != null ? request.name() : "";
            return new GetServicesByNameResponse(name, List.of());
        }

        Duration timeout = config.getHeartbeatTimeout();
        List<ServiceResponse> services = registry.values().stream()
                .filter(s -> s.serviceName.equalsIgnoreCase(request.name()) && s.isValid(timeout))
                .map(s -> new ServiceResponse(s.id, s.hostname, s.port))
                .toList();

        return new GetServicesByNameResponse(request.name(), services);
    }

    @Override
    public GetAllServicesResponse getAll() {
        Duration timeout = config.getHeartbeatTimeout();
        Map<String, List<RegisteredServiceInstance>> grouped = new LinkedHashMap<>();

        for (RegisteredServiceInstance instance : registry.values()) {
            if (!instance.isValid(timeout)) {
                continue;
            }
            String key = instance.serviceName.toLowerCase(Locale.ROOT);
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(instance);
        }

        List<GetServicesByNameResponse> servicesByName = grouped.values().stream()
                .map(group -> new GetServicesByNameResponse(
                        group.get(0).serviceName,
                        group.stream()
                                .map(s -> new ServiceResponse(s.id, s.hostname, s.port))
                                .toList()
                ))
                .toList();

        return new GetAllServicesResponse(servicesByName);
    }

    @Override
    public boolean deregister(UUID id) {
        return registry.remove(id) != null;
    }

    @Override
    public boolean heartbeat(UUID id) {
        RegisteredServiceInstance instance = registry.get(id);
        if (instance == null || !instance.isValid(config.getHeartbeatTimeout())) {
            return false;
        }
        instance.updateHeartbeat();
        return true;
    }

    @Override
    public void purgeExpiredServices() {
        Duration timeout = config.getHeartbeatTimeout();
        registry.entrySet().removeIf(entry -> !entry.getValue().isValid(timeout));
    }

    private boolean isPasswordValid(String password) {
        if (config.password == null) {
            return password == null;
        }

        if (password == null) {
            return false;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            var expectedHash = digest.digest(config.password.getBytes(StandardCharsets.UTF_8));
            var providedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(expectedHash, providedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
