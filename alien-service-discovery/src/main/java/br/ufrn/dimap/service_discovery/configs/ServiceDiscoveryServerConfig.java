package br.ufrn.dimap.service_discovery.configs;

import br.ufrn.dimap.annotations.configuration.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix="ServiceDiscovery")
public class ServiceDiscoveryServerConfig {
    public int heartbeatTimeoutSeconds = 90;
    public int cleanupIntervalSeconds = 120;
    public String password;

    public Duration getHeartbeatTimeout() {
        return Duration.ofSeconds(heartbeatTimeoutSeconds);
    }

    public Duration getHeartbeatInterval() {
        return Duration.ofSeconds(cleanupIntervalSeconds);
    }
}
