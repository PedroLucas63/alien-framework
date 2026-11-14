package br.ufrn.dimap.types;

import java.io.InputStream;
import java.util.Map;

public abstract class HttpMessage{
    private final HttpVersion version;
    private final HttpHeaders headers;
    private final InputStream bodyStream;

    protected HttpMessage(HttpVersion version, HttpHeaders headers, InputStream bodyStream) {
        this.version = version;
        this.headers = headers;
        this.bodyStream = bodyStream;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public InputStream getBodyStream() {
        return bodyStream;
    }

    public static abstract class Builder<
            TBuilder extends Builder<TBuilder, TMessage>,
            TMessage extends HttpMessage
    > {

        protected HttpVersion version;
        protected HttpHeaders headers = new HttpHeaders();
        protected InputStream bodyStream;

        public HttpVersion getVersion() {
            return version;
        }

        public HttpHeaders getHeaders() {
            return headers;
        }

        public InputStream getBodyStream() {
            return bodyStream;
        }

        public TBuilder withVersion(HttpVersion version) {
            this.version = version;
            return self();
        }

        public TBuilder withHeader(String key, String value) {
            this.headers.set(key, value);
            return self();
        }

        public TBuilder withHeader(HttpHeader header, String value) {
            this.headers.set(header.getKey(), value);
            return self();
        }

        public TBuilder withHeaders(Map<String, String> headers) {
            this.headers = new HttpHeaders(headers);
            return self();
        }

        public TBuilder withHeaders(HttpHeaders headers) {
            this.headers = headers;
            return self();
        }

        public TBuilder withHeaderMap(Map<HttpHeader, String> headers) {
            this.headers = HttpHeaders.fromHeaders(headers);
            return self();
        }

        public TBuilder withBody(InputStream bodyStream) {
            this.bodyStream = bodyStream;
            return self();
        }

        protected abstract TBuilder self();

        public abstract TMessage build();
    }
}
