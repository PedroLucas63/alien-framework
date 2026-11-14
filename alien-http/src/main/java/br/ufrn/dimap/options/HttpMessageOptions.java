package br.ufrn.dimap.options;

import br.ufrn.dimap.serializers.IHttpBodySerializer;
import br.ufrn.dimap.types.HttpHeaders;
import br.ufrn.dimap.types.HttpVersion;

import java.util.List;

public record HttpMessageOptions (
        HttpVersion version,
        boolean includeDateHeader,
        boolean useKeepAlive,
        HttpHeaders defaultHeaders,
        List<IHttpBodySerializer> bodySerializers
){
    public static Builder builder() {
        return new Builder();
    }

    public static HttpMessageOptions defaults() {
        return new Builder().build();
    }

    public static final class Builder {
        private HttpVersion version = HttpVersion.Http1_0;
        private boolean includeDateHeader = true;
        private boolean useKeepAlive = true;
        private HttpHeaders defaultHeaders = new HttpHeaders();
        private List<IHttpBodySerializer> bodySerializers = List.of();

        public Builder withVersion(HttpVersion version) {
            this.version = version;
            return this;
        }

        public Builder withIncludeDateHeader(boolean includeDateHeader) {
            this.includeDateHeader = includeDateHeader;
            return this;
        }

        public Builder withUseKeepAlive(boolean useKeepAlive) {
            this.useKeepAlive = useKeepAlive;
            return this;
        }

        public Builder withDefaultHeaders(HttpHeaders defaultHeaders) {
            this.defaultHeaders = defaultHeaders;
            return this;
        }

        public Builder withBodySerializers(List<IHttpBodySerializer> bodySerializers) {
            this.bodySerializers = List.copyOf(bodySerializers);
            return this;
        }

        public HttpMessageOptions build() {
            return new HttpMessageOptions(
                    version,
                    includeDateHeader,
                    useKeepAlive,
                    defaultHeaders,
                    bodySerializers
            );
        }
    }
}
