package br.ufrn.dimap.types;

import java.util.Optional;

public enum HttpMethod {
    Get("GET"),
    Query("QUERY"),
    Head("HEAD"),
    Post("POST"),
    Put("PUT"),
    Delete("DELETE"),
    Connect("CONNECT"),
    Options("OPTIONS"),
    Trace("TRACE"),
    Patch("PATCH");

    private final String value;

    private HttpMethod(String method) {
        value = method;
    }

    public String getValue() {
        return value;
    }

    public static HttpMethod parse(String method) {
        for (var httpMethod : HttpMethod.values()) {
            if (httpMethod.getValue().equals(method)) {
                return httpMethod;
            }
        }

        throw new IllegalArgumentException("Unknown HTTP method: " + method);
    }

    public static Optional<HttpMethod> tryParse(String method) {
        for (var httpMethod : HttpMethod.values()) {
            if (httpMethod.getValue().equals(method)) {
                return Optional.of(httpMethod);
            }
        }

        return Optional.empty();
    }
}
