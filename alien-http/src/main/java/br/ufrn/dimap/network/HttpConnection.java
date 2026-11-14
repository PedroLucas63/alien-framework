package br.ufrn.dimap.network;

import br.ufrn.dimap.exceptions.HttpException;
import br.ufrn.dimap.parsers.HttpParser;
import br.ufrn.dimap.serializers.HttpMessageSerializer;
import br.ufrn.dimap.types.HttpMessage;
import br.ufrn.dimap.types.HttpRequest;
import br.ufrn.dimap.types.HttpResponse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class HttpConnection implements AutoCloseable {
    private final Socket socket;

    public HttpConnection() {
        this.socket = new Socket();
    }
    public HttpConnection(InetAddress address, int port) throws IOException {
        this.socket = new Socket(address, port);
    }
    public HttpConnection(InetSocketAddress endpoint) throws IOException {
        this.socket = new Socket();
        this.socket.connect(endpoint);
    }
    public HttpConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
    }
    public HttpConnection(Socket socket) {
        this.socket = socket;
    }

    public void connect(String host, int port) throws IOException {
        this.socket.connect(new InetSocketAddress(host, port));
    }
    public void connect(String host, int port, int timeout) throws IOException {
        this.socket.connect(new InetSocketAddress(host, port), timeout);
    }
    public void connect(InetSocketAddress endpoint) throws IOException {
        this.socket.connect(endpoint);
    }
    public void connect(InetSocketAddress endpoint, int timeout) throws IOException {
        this.socket.connect(endpoint, timeout);
    }
    public void connect(InetAddress address, int port) throws IOException {
        this.socket.connect(new InetSocketAddress(address, port));
    }
    public void connect(InetAddress address, int port, int timeout) throws IOException {
        this.socket.connect(new InetSocketAddress(address, port), timeout);
    }

    public HttpMessage receive() throws IOException {
        var stream = socket.getInputStream();
        return HttpParser.parse(stream);
    }

    public HttpRequest receiveRequest() throws IOException {
        var stream = socket.getInputStream();
        return HttpParser.parseRequest(stream);
    }

    public HttpResponse receiveResponse() throws IOException {
        var stream = socket.getInputStream();
        return HttpParser.parseResponse(stream);
    }

    public void send(HttpMessage message) throws IOException {
        var stream = socket.getOutputStream();
        switch (message) {
            case HttpRequest request:
                HttpMessageSerializer.serialize(request, stream);
                break;
            case HttpResponse response:
                HttpMessageSerializer.serialize(response, stream);
                break;
            default:
                throw new HttpException("Unsupported message type: " + message.getClass().getName());
        }
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
