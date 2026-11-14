package br.ufrn.dimap.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;

public class HttpListener implements AutoCloseable {
    private final ServerSocket socket;
    private final InetSocketAddress endpoint;
    private final int backlog;

    public HttpListener(InetSocketAddress endpoint, int backlog) throws IOException {
        this.socket = new ServerSocket();
        this.endpoint = endpoint;
        this.backlog = backlog;
    }
    public HttpListener(InetSocketAddress endpoint) throws IOException {
        this(endpoint, 0);
    }
    public HttpListener(int port, int backlog) throws IOException {
        this(new InetSocketAddress(port), backlog);
    }
    public HttpListener(int port) throws IOException {
        this(port, 0);
    }
    public HttpListener(ServerSocket socket) throws IOException {
        this.socket = socket;
        this.endpoint = (InetSocketAddress) socket.getLocalSocketAddress();
        this.backlog = 0;
    }

    public SocketAddress getLocalEndpoint() {
        return this.socket.getLocalSocketAddress();
    }

    public void start() throws IOException {
        this.socket.bind(this.endpoint, this.backlog);
    }
    public void start(int backlog) throws IOException {
        this.socket.bind(this.endpoint, backlog);
    }

    public HttpConnection accept() throws IOException {
        var connection = this.socket.accept();
        return new HttpConnection(connection);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
