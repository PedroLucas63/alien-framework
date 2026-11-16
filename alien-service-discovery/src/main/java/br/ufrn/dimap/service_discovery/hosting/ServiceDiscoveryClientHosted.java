package br.ufrn.dimap.service_discovery.hosting;

import br.ufrn.dimap.hosting.IHostedService;
import br.ufrn.dimap.service_discovery.configs.ClientInformation;
import br.ufrn.dimap.service_discovery.configs.ServiceDiscoveryClientConfig;
import br.ufrn.dimap.service_discovery.dtos.RegisterServiceRequest;
import br.ufrn.dimap.service_discovery.dtos.RegisterServiceResponse;
import br.ufrn.dimap.service_discovery.transport.IDiscoveryProtocol;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Registers the current service on startup, sends periodic heartbeats,
 * and deregisters on shutdown.
 */
public class ServiceDiscoveryClientHosted implements IHostedService {

    private final ClientInformation clientInformation;
    private final ServiceDiscoveryClientConfig clientConfig;
    private final IDiscoveryProtocol protocol;

    private volatile UUID serviceId;
    private ScheduledExecutorService scheduler;

    public ServiceDiscoveryClientHosted(
            ClientInformation clientInformation,
            ServiceDiscoveryClientConfig clientConfig,
            IDiscoveryProtocol protocol
    ) {
        this.clientInformation = clientInformation;
        this.clientConfig = clientConfig;
        this.protocol = protocol;
    }

    @Override
    public synchronized void start() {
        if (!clientConfig.enabled) {
            return;
        }

        try {
            register();
        } catch (RuntimeException ignored) {
            // Registration will be retried in the background heartbeat loop.
            serviceId = null;
        }

        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(
                    Thread.ofVirtual().name("sd-heartbeat-", 0).factory()
            );
            long intervalMillis = Math.max(1L, clientConfig.getHeartbeatInterval().toMillis());
            scheduler.scheduleAtFixedRate(
                    this::heartbeatTick,
                    intervalMillis,
                    intervalMillis,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    @Override
    public synchronized void stop() {
        if (!clientConfig.enabled) {
            return;
        }

        if (scheduler != null) {
            scheduler.shutdownNow();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                scheduler = null;
            }
        }

        UUID currentId = serviceId;
        if (currentId == null) {
            return;
        }

        try {
            protocol.deregisterService(currentId);
        } catch (RuntimeException ignored) {
            // Best-effort deregistration on shutdown.
        } finally {
            serviceId = null;
        }
    }

    private void register() {
        RegisterServiceRequest request = new RegisterServiceRequest(
                clientConfig.serviceName,
                clientInformation.hostname,
                clientInformation.port,
                clientConfig.password
        );

        RegisterServiceResponse response = protocol.registerService(request);
        if (!response.success()) {
            throw new IllegalStateException(response.message());
        }
        serviceId = response.serviceId();
    }

    private void heartbeatTick() {
        try {
            UUID currentId = serviceId;
            if (currentId == null) {
                register();
                return;
            }

            boolean pulsed = protocol.pulse(currentId);
            if (!pulsed) {
                serviceId = null;
                register();
            }
        } catch (RuntimeException ignored) {
            // Transient error: keep current serviceId and retry on next tick.
        }
    }
}
