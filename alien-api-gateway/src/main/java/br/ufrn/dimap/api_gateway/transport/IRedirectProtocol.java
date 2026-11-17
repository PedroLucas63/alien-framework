package br.ufrn.dimap.api_gateway.transport;

import br.ufrn.dimap.service_discovery.dtos.ServiceResponse;
import br.ufrn.dimap.types.HttpRequest;
import br.ufrn.dimap.types.HttpResponse;

import java.io.IOException;

/**
 * Transport contract for forwarding an {@link HttpRequest} to a downstream service instance.
 */
public interface IRedirectProtocol {
    HttpResponse send(ServiceResponse serviceResponse, HttpRequest httpRequest) throws IOException;
}
