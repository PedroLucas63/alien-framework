package br.ufrn.dimap.hosting;

import br.ufrn.dimap.configuration.IConfiguration;
import br.ufrn.dimap.configuration.IConfigurationSource;
import br.ufrn.dimap.dependency_injection.IInversionOfControl;
import br.ufrn.dimap.pipeline.*;
import br.ufrn.dimap.routing.IRouteRegistry;
import br.ufrn.dimap.transport.IBeamDispatcher;
import br.ufrn.dimap.transport.IBeamProtocol;

import java.util.function.Function;

public interface IAlienAppBuilder {
    IInversionOfControl getServices();
    IConfiguration getConfiguration();
    IRouteRegistry getRoutes();

    IAlienAppBuilder addConfigurationSource(IConfigurationSource source);
    IAlienAppBuilder addModule(IAlienModule module);
    <TModule extends IAlienModule> IAlienAppBuilder addModules(Class<TModule> type);

    IAlienAppBuilder useMiddleware(IMiddleware middleware);
    IAlienAppBuilder addBeforeInterceptor(IBeforeInterceptor beforeInterceptor);
    IAlienAppBuilder addAfterInterceptor(IAfterInterceptor afterInterceptor);
    IAlienAppBuilder addExceptionHandler(IExceptionHandler exceptionHandler);
    IAlienAppBuilder addParameterBinder(IParameterBinder parameterBinder);

    IAlienAppBuilder useProtocol(IBeamProtocol protocol);
    IAlienAppBuilder useProtocol(Function<IBeamDispatcher, IBeamProtocol> protocol);
    <THostedService extends IHostedService> IAlienAppBuilder addHostedServices(Class<THostedService> type);
    <THostedService extends IHostedService> IAlienAppBuilder addHostedServices(THostedService service);

    IAlienApp build();
}
