package br.ufrn.dimap.types;

import java.util.Optional;

public enum HttpVersion {
    Http0_9("HTTP/0.9"),
    Http1_0("HTTP/1.0"),
    Http1_1("HTTP/1.1"),
    Http2("HTTP/2"),
    Http3("HTTP/3");

    private final String value;

    private HttpVersion(String version) {
        value = version;
    }

    public String getValue() {
        return value;
    }

    public static HttpVersion parse(String version) {
        for (var httpVersion : HttpVersion.values()) {
            if (httpVersion.getValue().equals(version)) {
                return httpVersion;
            }
        }

        throw new IllegalArgumentException("Unknown HTTP version: " + version);
    }

    public static Optional<HttpVersion> tryParse(String version) {
        for (var httpVersion : HttpVersion.values()) {
            if (httpVersion.getValue().equals(version)) {
                return Optional.of(httpVersion);
            }
        }

        return Optional.empty();
    }
}
