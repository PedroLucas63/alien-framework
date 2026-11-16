package br.ufrn.dimap.service_discovery.hosting;

import br.ufrn.dimap.hosting.IHostedService;
import br.ufrn.dimap.service_discovery.configs.ServiceDiscoveryServerConfig;
import br.ufrn.dimap.service_discovery.services.IServiceRegistry;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically purges expired service instances from {@link IServiceRegistry}.
 */
public class ServiceDiscoveryCleanupHosted implements IHostedService {

    private final IServiceRegistry registry;
    private final ServiceDiscoveryServerConfig config;
    private ScheduledExecutorService scheduler;

    public ServiceDiscoveryCleanupHosted(IServiceRegistry registry, ServiceDiscoveryServerConfig config) {
        this.registry = registry;
        this.config = config;
    }

    @Override
    public synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(
                Thread.ofVirtual().name("sd-cleanup-", 0).factory()
        );
        long delayMillis = Math.max(1L, config.getHeartbeatInterval().toMillis());
        scheduler.scheduleAtFixedRate(
                this::purgeSafely,
                delayMillis,
                delayMillis,
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

    private void purgeSafely() {
        try {
            registry.purgeExpiredServices();
        } catch (RuntimeException ignored) {
            // Keep periodic cleanup running if a single pass fails.
        }
    }
}
