package br.ufrn.dimap.hosting;

import br.ufrn.dimap.configuration.IConfiguration;
import br.ufrn.dimap.dependency_injection.IInversionOfControl;
import br.ufrn.dimap.routing.IRouteRegistry;
import br.ufrn.dimap.transport.IBeamProtocol;

import java.util.List;
import java.util.Objects;

/**
 * Default {@link IAlienApp} runtime coordinating hosted services and the transport protocol lifecycle.
 */
public class AlienApp implements IAlienApp {

    private final IInversionOfControl services;
    private final IConfiguration configuration;
    private final IRouteRegistry routes;
    private final IBeamProtocol protocol;
    private final List<Class<? extends IHostedService>> hostedServicesTypes;

    public AlienApp(
            IInversionOfControl services,
            IConfiguration configuration,
            IRouteRegistry routes,
            IBeamProtocol protocol,
            List<Class<? extends IHostedService>> hostedServicesTypes
    ) {
        this.services = Objects.requireNonNull(services, "services must not be null");
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        this.routes = Objects.requireNonNull(routes, "routes must not be null");
        this.protocol = Objects.requireNonNull(protocol, "protocol must not be null");
        this.hostedServicesTypes = List.copyOf(hostedServicesTypes);
    }

    @Override
    public IInversionOfControl getServices() {
        return services;
    }

    @Override
    public IConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public IRouteRegistry getRoutes() {
        return routes;
    }

    @Override
    public void start() {
        for (Class<? extends IHostedService> hostedServiceType : hostedServicesTypes) {
            IHostedService host = services.spawn(hostedServiceType);
            host.start();
        }
        protocol.start();
    }

    @Override
    public void stop() {
        protocol.stop();
        for (int i = hostedServicesTypes.size() - 1; i >= 0; i--) {
            IHostedService host = services.spawn(hostedServicesTypes.get(i));
            host.stop();
        }
    }
}
