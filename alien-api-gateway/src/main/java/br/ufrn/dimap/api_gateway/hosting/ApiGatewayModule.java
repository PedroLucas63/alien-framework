package br.ufrn.dimap.api_gateway.hosting;

import br.ufrn.dimap.annotations.configuration.ConfigurationProperties;
import br.ufrn.dimap.api_gateway.configs.ServiceDiscoveryConsumerConfig;
import br.ufrn.dimap.api_gateway.managers.IServicesManager;
import br.ufrn.dimap.api_gateway.managers.ServicesManager;
import br.ufrn.dimap.api_gateway.services.ApiGatewayService;
import br.ufrn.dimap.api_gateway.services.IApiGatewayService;
import br.ufrn.dimap.api_gateway.transport.IRedirectProtocol;
import br.ufrn.dimap.configuration.sources.EnvConfigurationSource;
import br.ufrn.dimap.factories.HttpRequestFactory;
import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.hosting.IAlienAppBuilder;
import br.ufrn.dimap.hosting.IAlienModule;
import br.ufrn.dimap.options.HttpMessageOptions;
import br.ufrn.dimap.serializers.JsonBodySerializer;
import br.ufrn.dimap.serializers.TextBodySerializer;
import br.ufrn.dimap.service_discovery.configs.ServiceDiscoveryConfig;
import br.ufrn.dimap.service_discovery.transport.IDiscoveryProtocol;
import br.ufrn.dimap.types.HttpVersion;

import java.util.List;
import java.util.Objects;

/**
 * Configures the API gateway discovery consumer, redirect protocol, load-balancing manager,
 * and dynamic routing service.
 */
public class ApiGatewayModule implements IAlienModule {

    private final Class<? extends IDiscoveryProtocol> discoveryProtocolType;
    private final Class<? extends IRedirectProtocol> redirectProtocolType;
    private final HttpMessageOptions messageOptions;

    public ApiGatewayModule(
            Class<? extends IDiscoveryProtocol> discoveryProtocolType,
            Class<? extends IRedirectProtocol> redirectProtocolType
    ) {
        this(
                discoveryProtocolType,
                redirectProtocolType,
                HttpMessageOptions.builder()
                        .withVersion(HttpVersion.Http1_1)
                        .withBodySerializers(List.of(new TextBodySerializer(), new JsonBodySerializer()))
                        .build()
        );
    }

    public ApiGatewayModule(
            Class<? extends IDiscoveryProtocol> discoveryProtocolType,
            Class<? extends IRedirectProtocol> redirectProtocolType,
            HttpMessageOptions messageOptions
    ) {
        this.discoveryProtocolType = Objects.requireNonNull(
                discoveryProtocolType, "discoveryProtocolType must not be null"
        );
        this.redirectProtocolType = Objects.requireNonNull(
                redirectProtocolType, "redirectProtocolType must not be null"
        );
        this.messageOptions = Objects.requireNonNull(messageOptions, "messageOptions must not be null");
    }

    @Override
    public void configure(IAlienAppBuilder builder) {
        ensureHttpRequestFactory(builder);
        ensureHttpResponseFactory(builder);

        builder.getConfiguration().addSource(new EnvConfigurationSource());

        ConfigurationProperties annotation = ServiceDiscoveryConsumerConfig.class
                .getAnnotation(ConfigurationProperties.class);
        String prefix = annotation != null && !annotation.prefix().isBlank() ? annotation.prefix() : null;
        builder.getServices().registerConfig(
                ServiceDiscoveryConsumerConfig.class,
                builder.getConfiguration(),
                prefix
        );
        builder.getServices().registerConfig(
                ServiceDiscoveryConfig.class,
                ServiceDiscoveryConsumerConfig.class,
                builder.getConfiguration(),
                prefix
        );

        builder.getServices().registerEternal(IDiscoveryProtocol.class, discoveryProtocolType);
        builder.getServices().registerEternal(IRedirectProtocol.class, redirectProtocolType);
        builder.getServices().registerEternal(IServicesManager.class, ServicesManager.class);
        builder.getServices().registerEternal(IApiGatewayService.class, ApiGatewayService.class);

        builder.addHostedServices(ApiGatewayConsumeDiscoveryHosted.class);
    }

    private void ensureHttpRequestFactory(IAlienAppBuilder builder) {
        if (builder.getServices().contains(HttpRequestFactory.class)) {
            return;
        }
        builder.getServices().registerEternal(new HttpRequestFactory(messageOptions));
    }

    private void ensureHttpResponseFactory(IAlienAppBuilder builder) {
        if (builder.getServices().contains(HttpResponseFactory.class)) {
            return;
        }
        builder.getServices().registerEternal(new HttpResponseFactory(messageOptions));
    }
}
