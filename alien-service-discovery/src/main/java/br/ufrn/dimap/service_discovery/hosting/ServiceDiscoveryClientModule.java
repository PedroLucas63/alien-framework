package br.ufrn.dimap.service_discovery.hosting;

import br.ufrn.dimap.annotations.configuration.ConfigurationProperties;
import br.ufrn.dimap.factories.HttpRequestFactory;
import br.ufrn.dimap.hosting.IAlienAppBuilder;
import br.ufrn.dimap.hosting.IAlienModule;
import br.ufrn.dimap.options.HttpMessageOptions;
import br.ufrn.dimap.serializers.JsonBodySerializer;
import br.ufrn.dimap.service_discovery.configs.ClientInformation;
import br.ufrn.dimap.service_discovery.configs.ServiceDiscoveryClientConfig;
import br.ufrn.dimap.service_discovery.configs.ServiceDiscoveryConfig;
import br.ufrn.dimap.service_discovery.transport.IDiscoveryProtocol;
import br.ufrn.dimap.types.HttpVersion;

import java.util.List;
import java.util.Objects;

/**
 * Configures service discovery client registration, configuration binding, and heartbeat hosting.
 */
public class ServiceDiscoveryClientModule implements IAlienModule {

    private final Class<? extends IDiscoveryProtocol> protocolType;
    private final HttpMessageOptions messageOptions;

    public ServiceDiscoveryClientModule(Class<? extends IDiscoveryProtocol> protocolType) {
        this(protocolType, HttpMessageOptions.builder()
                .withVersion(HttpVersion.Http1_1)
                .withBodySerializers(List.of(new JsonBodySerializer()))
                .build());
    }

    public ServiceDiscoveryClientModule(
            Class<? extends IDiscoveryProtocol> protocolType,
            HttpMessageOptions messageOptions
    ) {
        this.protocolType = Objects.requireNonNull(protocolType, "protocolType must not be null");
        this.messageOptions = Objects.requireNonNull(messageOptions, "messageOptions must not be null");
    }

    @Override
    public void configure(IAlienAppBuilder builder) {
        ensureHttpRequestFactory(builder);

        String clientInfoPrefix = getPrefix(ClientInformation.class);
        builder.getServices().registerConfig(ClientInformation.class, builder.getConfiguration(), clientInfoPrefix);

        String clientConfigPrefix = getPrefix(ServiceDiscoveryClientConfig.class);
        builder.getServices().registerConfig(
                ServiceDiscoveryClientConfig.class,
                builder.getConfiguration(),
                clientConfigPrefix
        );
        builder.getServices().registerConfig(
                ServiceDiscoveryConfig.class,
                ServiceDiscoveryClientConfig.class,
                builder.getConfiguration(),
                clientConfigPrefix
        );

        builder.getServices().registerEternal(IDiscoveryProtocol.class, protocolType);
        builder.addHostedServices(ServiceDiscoveryClientHosted.class);
    }

    private void ensureHttpRequestFactory(IAlienAppBuilder builder) {
        if (builder.getServices().contains(HttpRequestFactory.class)) {
            return;
        }
        builder.getServices().registerEternal(new HttpRequestFactory(messageOptions));
    }

    private static String getPrefix(Class<?> configClass) {
        ConfigurationProperties annotation = configClass.getAnnotation(ConfigurationProperties.class);
        return annotation != null && !annotation.prefix().isBlank() ? annotation.prefix() : null;
    }
}
