package br.ufrn.dimap.types;

import java.io.InputStream;
import java.util.Map;

public class HttpRequest extends HttpMessage {
    private final HttpMethod method;
    private final String path;
    private final HttpQueryParameters queryParameters;

    public HttpRequest(
            HttpMethod method,
            String path,
            HttpVersion version,
            HttpQueryParameters queryParameters,
            HttpHeaders headers,
            InputStream bodyStream
    ) {
        super(version, headers, bodyStream);
        this.method = method;
        this.path = path;
        this.queryParameters = queryParameters;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public HttpQueryParameters getQueryParameters() {
        return queryParameters;
    }

    public static class Builder extends HttpMessage.Builder<Builder, HttpRequest> {
        private HttpMethod method;
        private String path = "";
        private HttpQueryParameters queryParameters = new HttpQueryParameters();

        public HttpMethod getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }

        public HttpQueryParameters getQueryParameters() {
            return queryParameters;
        }

        public Builder withMethod(HttpMethod method) {
            this.method = method;
            return self();
        }

        public Builder withPath(String path) {
            this.path = path;
            return self();
        }

        public Builder withQueryParameter(String parameter, String value) {
            this.queryParameters.set(parameter, value);
            return self();
        }

        public Builder withQueryParameters(Map<String, String> parameters) {
            this.queryParameters = new HttpQueryParameters(parameters);
            return self();
        }

        public Builder withQueryParameters(HttpQueryParameters queryParameters) {
            this.queryParameters = new HttpQueryParameters(queryParameters);
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public HttpRequest build() {
            if (this.method == null)
                throw new IllegalStateException("The HTTP method is required");
            if (this.version == null)
                throw new IllegalStateException("The HTTP version is required");
            if (this.path == null || this.path.isEmpty())
                throw new IllegalStateException("The path is required");
            if (this.bodyStream == null)
                throw new IllegalStateException("The body stream is required");

            return new HttpRequest(
                    this.method,
                    this.path,
                    this.version,
                    this.queryParameters,
                    this.headers,
                    this.bodyStream
            );
        }
    }
}
