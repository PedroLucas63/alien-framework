package br.ufrn.dimap.hosting;

import br.ufrn.dimap.configuration.IConfiguration;
import br.ufrn.dimap.dependency_injection.IInversionOfControl;
import br.ufrn.dimap.routing.IRouteRegistry;

public interface IAlienApp {
    IInversionOfControl getServices();
    IConfiguration getConfiguration();
    IRouteRegistry getRoutes();

    void start();
    void stop();
}
