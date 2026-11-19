package br.ufrn.dimap.service_discovery.hosting;

import br.ufrn.dimap.annotations.configuration.ConfigurationProperties;
import br.ufrn.dimap.configuration.sources.EnvConfigurationSource;
import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.hosting.IAlienAppBuilder;
import br.ufrn.dimap.hosting.IAlienModule;
import br.ufrn.dimap.options.HttpMessageOptions;
import br.ufrn.dimap.serializers.JsonBodySerializer;
import br.ufrn.dimap.serializers.TextBodySerializer;
import br.ufrn.dimap.service_discovery.configs.ServiceDiscoveryServerConfig;
import br.ufrn.dimap.service_discovery.controllers.IServicesController;
import br.ufrn.dimap.service_discovery.controllers.ServicesController;
import br.ufrn.dimap.service_discovery.services.IServiceRegistry;
import br.ufrn.dimap.service_discovery.services.ServiceRegistry;
import br.ufrn.dimap.types.HttpVersion;

import java.util.List;
import java.util.Objects;

/**
 * Configures the service discovery server registry, endpoints, and cleanup hosted service.
 */
public class ServiceDiscoveryServerModule implements IAlienModule {

    private final HttpMessageOptions messageOptions;

    public ServiceDiscoveryServerModule() {
        this(HttpMessageOptions.builder()
                .withVersion(HttpVersion.Http1_1)
                .withBodySerializers(List.of(new TextBodySerializer(), new JsonBodySerializer()))
                .build());
    }

    public ServiceDiscoveryServerModule(HttpMessageOptions messageOptions) {
        this.messageOptions = Objects.requireNonNull(messageOptions, "messageOptions must not be null");
    }

    @Override
    public void configure(IAlienAppBuilder builder) {
        ensureHttpResponseFactory(builder);

        builder.getConfiguration().addSource(new EnvConfigurationSource());

        ConfigurationProperties annotation = ServiceDiscoveryServerConfig.class
                .getAnnotation(ConfigurationProperties.class);
        String prefix = annotation != null && !annotation.prefix().isBlank() ? annotation.prefix() : null;
        builder.getServices().registerConfig(ServiceDiscoveryServerConfig.class, builder.getConfiguration(), prefix);

        builder.getServices().registerPerRequest(IServicesController.class, ServicesController.class);
        builder.getServices().registerEternal(IServiceRegistry.class, ServiceRegistry.class);
        builder.getRoutes().registerController(ServicesController.class);

        builder.addHostedServices(ServiceDiscoveryCleanupHosted.class);
    }

    private void ensureHttpResponseFactory(IAlienAppBuilder builder) {
        if (builder.getServices().contains(HttpResponseFactory.class)) {
            return;
        }
        builder.getServices().registerEternal(new HttpResponseFactory(messageOptions));
    }
}
