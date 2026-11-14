package br.ufrn.dimap.types;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class HttpQueryParameters {
    public final Map<String, String> values;

    public HttpQueryParameters() {
        this.values = new HashMap<>();
    }

    public HttpQueryParameters(Map<String, String> values) {
        this.values = new HashMap<>(values);
    }

    public HttpQueryParameters(HttpQueryParameters parameters) {
        this.values = new HashMap<>(parameters.values);
    }

    public void add(String parameter, String value) {
        values.merge(parameter, value, (existing, incoming) -> existing + "," + incoming);
    }

    public String get(String parameter) {
        if (!values.containsKey(parameter)) {
            throw new NoSuchElementException("Parameter not found: " + parameter);
        }
        return values.get(parameter);
    }

    public void set(String header, String value) {
        values.put(header, value);
    }

    public Optional<String> tryGetParameter(String parameter) {
        if (!values.containsKey(parameter)) {
            return Optional.empty();
        }
        return Optional.of(values.get(parameter));
    }
}
