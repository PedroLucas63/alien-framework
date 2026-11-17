package br.ufrn.dimap.api_gateway.transport;

import br.ufrn.dimap.parsers.HttpParser;
import br.ufrn.dimap.serializers.HttpMessageSerializer;
import br.ufrn.dimap.service_discovery.dtos.ServiceResponse;
import br.ufrn.dimap.types.HttpRequest;
import br.ufrn.dimap.types.HttpResponse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * UDP implementation of {@link IRedirectProtocol}.
 */
public class UdpRedirectProtocol implements IRedirectProtocol {

    private static final int TIMEOUT_MILLIS = 5000;
    private static final int MAX_UDP_PACKET_BYTES = 65507;

    @Override
    public HttpResponse send(ServiceResponse serviceResponse, HttpRequest httpRequest) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MILLIS);

            byte[] buffer = HttpMessageSerializer.serialize(httpRequest);
            InetAddress address = InetAddress.getByName(serviceResponse.hostname());
            DatagramPacket outgoing = new DatagramPacket(
                    buffer, buffer.length, address, serviceResponse.port()
            );
            socket.send(outgoing);

            byte[] receiveBuffer = new byte[MAX_UDP_PACKET_BYTES];
            DatagramPacket incoming = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(incoming);

            byte[] payload = Arrays.copyOf(incoming.getData(), incoming.getLength());
            return HttpParser.parseResponse(payload);
        }
    }
}
