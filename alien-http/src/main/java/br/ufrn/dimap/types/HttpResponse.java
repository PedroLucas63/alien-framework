package br.ufrn.dimap.types;

import java.io.InputStream;

public class HttpResponse extends HttpMessage {
    private final HttpStatusCode statusCode;
    private final String reasonPhrase;

    public HttpResponse(
            HttpVersion version,
            HttpStatusCode statusCode,
            String reasonPhrase,
            HttpHeaders headers,
            InputStream bodyStream
    ) {
        super(version, headers, bodyStream);
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public static class Builder extends HttpMessage.Builder<Builder, HttpResponse> {
        private HttpStatusCode statusCode;
        private String reasonPhrase = "";

        public HttpStatusCode getStatusCode() {
            return statusCode;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }

        public Builder withStatusCode(HttpStatusCode statusCode) {
            this.statusCode = statusCode;
            return self();
        }

        public Builder withReasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public HttpResponse build() {
            if (this.version == null)
                throw new IllegalStateException("The HTTP version is required");
            if (this.statusCode == null)
                throw new IllegalStateException("The HTTP status code is required");
            if (this.reasonPhrase == null ||  this.reasonPhrase.isEmpty())
                this.reasonPhrase = this.statusCode.getReasonPhrase();
            if (this.bodyStream == null)
                throw new IllegalStateException("The body stream is required");

            return new HttpResponse(
                    this.version,
                    this.statusCode,
                    this.reasonPhrase,
                    this.headers,
                    this.bodyStream
            );
        }
    }
}
