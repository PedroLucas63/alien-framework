package br.ufrn.dimap.transport;

import br.ufrn.dimap.parsers.HttpParser;
import br.ufrn.dimap.serializers.HttpMessageSerializer;
import br.ufrn.dimap.types.HttpContext;
import br.ufrn.dimap.types.HttpRequest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpBeamProtocol implements IBeamProtocol {
    private final IBeamDispatcher dispatcher;
    private final int port;

    private volatile boolean running;
    private DatagramSocket listener;

    public UdpBeamProtocol(IBeamDispatcher dispatcher, int port) {
        this.dispatcher = dispatcher;
        this.port = port;
    }

    @Override
    public void start() {
        try (var datagramSocket = new DatagramSocket(port)) {
            this.listener = datagramSocket;
            this.running = true;



            while (running) {
                try {
                    var buffer = new byte[65535];
                    var packet = new DatagramPacket(buffer, buffer.length);

                    listener.receive(packet);

                    Thread.startVirtualThread(() -> processConnection(packet));
                } catch (SocketException e) {
                    if (!running) {
                        break;
                    }
                    throw e;
                }
            }
        } catch (Exception e) {
            if (running) {
                throw new RuntimeException("Error in HttpBeamProtocol", e);
            }
        } finally {
            this.running = false;
            this.listener = null;
        }
    }

    private void processConnection(DatagramPacket packet) {
        HttpRequest request = null;
        try {
            request = HttpParser.parseRequest(packet.getData());
        } catch (IOException ignored) {
        }

        var context = new HttpContext();
        context.request = request;

        dispatcher.dispatch(context);

        if (context.response != null) {
            try {
                var buffer = HttpMessageSerializer.serialize(context.response);
                var sendPacket = new DatagramPacket(
                        buffer, buffer.length, packet.getAddress(), packet.getPort());
                listener.send(sendPacket);
            } catch (IOException ignored) {

            }
        }
    }

    @Override
    public void stop() {
        running = false;
        var currentListener = this.listener;
        if (currentListener != null) {
            try {
                currentListener.close();
            } catch (Exception ignored) {
            }
        }
    }
}
