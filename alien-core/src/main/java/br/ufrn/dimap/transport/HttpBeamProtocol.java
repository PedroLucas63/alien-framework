package br.ufrn.dimap.transport;

import br.ufrn.dimap.network.HttpConnection;
import br.ufrn.dimap.network.HttpListener;
import br.ufrn.dimap.types.HttpContext;
import br.ufrn.dimap.types.HttpRequest;

import java.net.SocketException;

public class HttpBeamProtocol implements IBeamProtocol {
    private final IBeamDispatcher dispatcher;
    private final int port;

    private volatile boolean running;
    private HttpListener listener;

    public HttpBeamProtocol(IBeamDispatcher dispatcher, int port) {
        this.dispatcher = dispatcher;
        this.port = port;
    }

    @Override
    public void start() {
        try (var httpListener = new HttpListener(port)) {
            this.listener = httpListener;
            this.running = true;
            httpListener.start();

            while (running) {
                try {
                    HttpConnection connection = httpListener.accept();
                    Thread.startVirtualThread(() -> processConnection(connection));
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

    private void processConnection(HttpConnection connection) {
        try (connection) {
            HttpRequest request;
            try {
                request = connection.receiveRequest();
            } catch (Exception e) {
                request = null;
            }

            var context = new HttpContext();
            context.request = request;

            dispatcher.dispatch(context);

            if (context.response != null) {
                try {
                    connection.send(context.response);
                } finally {
                    context.response.getBodyStream().close();
                }
            }
        } catch (Exception ignored) {
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
