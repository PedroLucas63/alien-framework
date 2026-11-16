package br.ufrn.dimap.service_discovery.transport;

import br.ufrn.dimap.factories.HttpRequestFactory;
import br.ufrn.dimap.parsers.HttpParser;
import br.ufrn.dimap.serializers.HttpMessageSerializer;
import br.ufrn.dimap.service_discovery.configs.ServiceDiscoveryConfig;
import br.ufrn.dimap.types.HttpRequest;
import br.ufrn.dimap.types.HttpResponse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * UDP implementation of {@link DiscoveryProtocolBase}.
 */
public class UdpDiscoveryProtocol extends DiscoveryProtocolBase {

    private static final int TIMEOUT_MILLIS = 5000;
    private static final int MAX_UDP_PACKET_BYTES = 65507;

    public UdpDiscoveryProtocol(ServiceDiscoveryConfig config, HttpRequestFactory requestFactory) {
        super(config, requestFactory);
    }

    @Override
    protected HttpResponse sendRequest(HttpRequest request) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MILLIS);

            byte[] buffer = HttpMessageSerializer.serialize(request);
            InetAddress address = InetAddress.getByName(config.registryHostname);
            DatagramPacket outgoing = new DatagramPacket(buffer, buffer.length, address, config.registryPort);
            socket.send(outgoing);

            byte[] receiveBuffer = new byte[MAX_UDP_PACKET_BYTES];
            DatagramPacket incoming = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(incoming);

            byte[] payload = Arrays.copyOf(incoming.getData(), incoming.getLength());
            return HttpParser.parseResponse(payload);
        }
    }
}
