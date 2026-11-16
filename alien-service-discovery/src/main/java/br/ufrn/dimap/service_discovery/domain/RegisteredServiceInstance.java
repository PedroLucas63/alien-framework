package br.ufrn.dimap.service_discovery.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class RegisteredServiceInstance {
    public final UUID id;
    public final String serviceName;
    public final String hostname;
    public final int port;
    public final Instant registeredOn;
    private final AtomicLong heartbeatEpochMillis;

    public RegisteredServiceInstance(UUID id, String serviceName, String hostname, int port) {
        Instant now = Instant.now();
        this.id = id;
        this.serviceName = serviceName;
        this.hostname = hostname;
        this.port = port;
        this.registeredOn = now;
        this.heartbeatEpochMillis = new AtomicLong(now.toEpochMilli());
    }

    public Instant getHeartbeatOn() {
        return Instant.ofEpochMilli(heartbeatEpochMillis.get());
    }

    public void updateHeartbeat() {
        heartbeatEpochMillis.set(System.currentTimeMillis());
    }

    public boolean isValid(Duration timeout) {
        Duration elapsed = Duration.between(getHeartbeatOn(), Instant.now());
        return elapsed.compareTo(timeout) < 0;
    }
}