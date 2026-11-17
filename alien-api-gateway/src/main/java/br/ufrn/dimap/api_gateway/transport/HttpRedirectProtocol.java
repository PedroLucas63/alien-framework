package br.ufrn.dimap.api_gateway.transport;

import br.ufrn.dimap.network.HttpConnection;
import br.ufrn.dimap.service_discovery.dtos.ServiceResponse;
import br.ufrn.dimap.types.HttpRequest;
import br.ufrn.dimap.types.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TCP/HTTP implementation of {@link IRedirectProtocol}.
 */
public class HttpRedirectProtocol implements IRedirectProtocol {

    @Override
    public HttpResponse send(ServiceResponse serviceResponse, HttpRequest httpRequest) throws IOException {
        try (HttpConnection connection = new HttpConnection()) {
            connection.connect(serviceResponse.hostname(), serviceResponse.port());
            connection.send(httpRequest);
            HttpResponse response = connection.receiveResponse();

            InputStream bodyStream = response.getBodyStream();
            if (bodyStream == null || bodyStream == InputStream.nullInputStream()) {
                return response;
            }

            byte[] bufferedBytes = bodyStream.readAllBytes();
            return new HttpResponse(
                    response.getVersion(),
                    response.getStatusCode(),
                    response.getReasonPhrase(),
                    response.getHeaders(),
                    new ByteArrayInputStream(bufferedBytes)
            );
        }
    }
}
