package br.ufrn.dimap.api_gateway.hosting;

import br.ufrn.dimap.api_gateway.configs.ServiceDiscoveryConsumerConfig;
import br.ufrn.dimap.api_gateway.services.IApiGatewayService;
import br.ufrn.dimap.hosting.IHostedService;
import br.ufrn.dimap.service_discovery.dtos.GetAllServicesResponse;
import br.ufrn.dimap.service_discovery.transport.IDiscoveryProtocol;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically polls the service discovery registry and updates {@link IApiGatewayService}.
 */
public class ApiGatewayConsumeDiscoveryHosted implements IHostedService {

    private final ServiceDiscoveryConsumerConfig config;
    private final IApiGatewayService service;
    private final IDiscoveryProtocol protocol;

    private ScheduledExecutorService scheduler;

    public ApiGatewayConsumeDiscoveryHosted(
            ServiceDiscoveryConsumerConfig config,
            IApiGatewayService service,
            IDiscoveryProtocol protocol
    ) {
        this.config = config;
        this.service = service;
        this.protocol = protocol;
    }

    @Override
    public synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(
                Thread.ofVirtual().name("gateway-discovery-", 0).factory()
        );
        long intervalMillis = Math.max(1L, config.getConsumeInterval().toMillis());
        scheduler.scheduleAtFixedRate(
                this::consumeTick,
                0L,
                intervalMillis,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public synchronized void stop() {
        if (scheduler == null) {
            return;
        }
        scheduler.shutdownNow();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            scheduler = null;
        }
    }

    private void consumeTick() {
        try {
            Optional<GetAllServicesResponse> response = protocol.getAllServices();
            response.ifPresent(service::addOrUpdateServices);
        } catch (RuntimeException ignored) {
            // Transient error: keep existing routes and retry on next tick.
        }
    }
}
