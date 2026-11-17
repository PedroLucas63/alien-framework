package br.ufrn.dimap.api_gateway.configs;

import br.ufrn.dimap.annotations.configuration.ConfigurationProperties;
import br.ufrn.dimap.service_discovery.configs.ServiceDiscoveryConfig;

import java.time.Duration;

/**
 * Configuration properties for API gateway service discovery polling.
 */
@ConfigurationProperties(prefix = "ServiceDiscovery:Consumer")
public class ServiceDiscoveryConsumerConfig extends ServiceDiscoveryConfig {
    public int consumeIntervalSeconds = 60;

    public Duration getConsumeInterval() {
        return Duration.ofSeconds(consumeIntervalSeconds);
    }
}
