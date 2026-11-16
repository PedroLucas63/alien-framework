package br.ufrn.dimap.service_discovery.configs;

import br.ufrn.dimap.annotations.configuration.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix="ServiceDiscovery:Client")
public class ServiceDiscoveryClientConfig extends ServiceDiscoveryConfig {
    public boolean enabled = true;
    public String serviceName;
    public int heartbeatIntervalSeconds = 30;

    public Duration getHeartbeatInterval() {
        return Duration.ofSeconds(heartbeatIntervalSeconds);
    }
}
