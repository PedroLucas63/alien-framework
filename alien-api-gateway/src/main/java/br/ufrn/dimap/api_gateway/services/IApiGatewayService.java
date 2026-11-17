package br.ufrn.dimap.api_gateway.services;

import br.ufrn.dimap.service_discovery.dtos.GetAllServicesResponse;
import br.ufrn.dimap.types.HttpContext;
import br.ufrn.dimap.types.HttpResponse;

/**
 * Routes incoming gateway requests to discovered downstream services and updates
 * dynamic route registrations.
 */
public interface IApiGatewayService {
    HttpResponse route(HttpContext httpContext);
    void addOrUpdateServices(GetAllServicesResponse response);
}
