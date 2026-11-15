package br.ufrn.dimap.hosting;

import br.ufrn.dimap.dependency_injection.IInversionOfControl;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background {@link IHostedService} that periodically triggers
 * {@link IInversionOfControl#passivateIdleUsers()} to clean up idle per-user scopes.
 */
public class PassivateHosted implements IHostedService {

    private static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(30);

    private final IInversionOfControl inversionOfControl;
    private final Duration interval;
    private ScheduledExecutorService scheduler;

    public PassivateHosted(IInversionOfControl inversionOfControl) {
        this(inversionOfControl, DEFAULT_INTERVAL);
    }

    public PassivateHosted(IInversionOfControl inversionOfControl, Duration interval) {
        this.inversionOfControl = Objects.requireNonNull(inversionOfControl, "inversionOfControl must not be null");
        this.interval = Objects.requireNonNull(interval, "interval must not be null");
    }

    @Override
    public synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(
                Thread.ofVirtual().name("passivate-hosted-", 0).factory()
        );
        long delayMillis = Math.max(1L, interval.toMillis());
        scheduler.scheduleAtFixedRate(
                this::runPassivationSafely,
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

    private void runPassivationSafely() {
        try {
            inversionOfControl.passivateIdleUsers();
        } catch (RuntimeException ignored) {
            // Keep periodic passivation alive if a single pass encounters an error.
        }
    }
}
