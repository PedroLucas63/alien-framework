package br.ufrn.dimap.types;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;

public class HttpHeaders {
    public final Map<String, String> values;

    public HttpHeaders() {
        this.values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public HttpHeaders(Map<String, String> values) {
        this.values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.values.putAll(values);
    }

    public HttpHeaders(HttpHeaders headers) {
        this.values = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.values.putAll(headers.values);
    }

    public static HttpHeaders fromHeaders(Map<HttpHeader, String> values) {
        var headers = new HttpHeaders();
        for (var entry : values.entrySet()) {
            headers.values.put(entry.getKey().getKey(), entry.getValue());
        }
        return headers;
    }

    public void add(String header, String value) {
        values.merge(header, value, (existing, incoming) -> existing + "," + incoming);
    }

    public void add(HttpHeader header, String value) {
        add(header.getKey(), value);
    }

    public void extend(Map<String, String> headers) {
        for (var entry : headers.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public void extend(HttpHeaders headers) {
        extend(headers.values);
    }

    public void extendFromHeaders(Map<HttpHeader, String> headers) {
        for (var entry : headers.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public String get(String header) {
        if (!values.containsKey(header)) {
            throw new NoSuchElementException("Header not found: " + header);
        }
        return values.get(header);
    }

    public String get(HttpHeader header) {
        if (!values.containsKey(header.getKey())) {
            throw new NoSuchElementException("Header not found: " + header.getKey());
        }
        return values.get(header.getKey());
    }

    public void set(String header, String value) {
        values.put(header, value);
    }

    public void set(HttpHeader header, String value) {
        values.put(header.getKey(), value);
    }

    public Optional<String> tryGetHeader(String header) {
        if (!values.containsKey(header)) {
            return Optional.empty();
        }
        return Optional.of(values.get(header));
    }

    public Optional<String> tryGetHeader(HttpHeader header) {
        return tryGetHeader(header.getKey());
    }
}
