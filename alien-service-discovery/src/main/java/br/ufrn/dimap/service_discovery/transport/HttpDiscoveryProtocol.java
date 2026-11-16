package br.ufrn.dimap.service_discovery.transport;

import br.ufrn.dimap.factories.HttpRequestFactory;
import br.ufrn.dimap.network.HttpConnection;
import br.ufrn.dimap.service_discovery.configs.ServiceDiscoveryConfig;
import br.ufrn.dimap.types.HttpRequest;
import br.ufrn.dimap.types.HttpResponse;

import java.io.IOException;

/**
 * TCP/HTTP implementation of {@link DiscoveryProtocolBase}.
 */
public class HttpDiscoveryProtocol extends DiscoveryProtocolBase {

    public HttpDiscoveryProtocol(ServiceDiscoveryConfig config, HttpRequestFactory requestFactory) {
        super(config, requestFactory);
    }

    @Override
    protected HttpResponse sendRequest(HttpRequest request) throws IOException {
        try (HttpConnection connection = new HttpConnection()) {
            connection.connect(config.registryHostname, config.registryPort);
            connection.send(request);
            return connection.receiveResponse();
        }
    }
}
