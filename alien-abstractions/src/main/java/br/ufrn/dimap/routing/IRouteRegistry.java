package br.ufrn.dimap.routing;

import br.ufrn.dimap.routing.types.EndpointDescriptor;

import java.util.List;

public interface IRouteRegistry {
    void autoRegister();

    <TController> void registerController(Class<TController> type);

    void mapEndpoint(EndpointDescriptor descriptor);
    List<EndpointDescriptor> getEndpoints();
}
