package br.ufrn.dimap.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class JsonBodySerializer implements IHttpBodySerializer {
    private final ObjectMapper objectMapper;

    public JsonBodySerializer() {
        this.objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    public JsonBodySerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String contentType(Object body) {
        return "application/json; charset=utf-8";
    }

    @Override
    public boolean canSerialize(Object body) {
        return body != null && !(body instanceof String) && !(body instanceof byte[]);
    }

    @Override
    public InputStream serialize(Object body) {
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(body);
            return new ByteArrayInputStream(jsonBytes);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("Failed to serialize body to JSON", e);
        }
    }
}
