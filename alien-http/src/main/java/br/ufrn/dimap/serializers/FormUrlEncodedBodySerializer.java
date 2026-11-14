package br.ufrn.dimap.serializers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class FormUrlEncodedBodySerializer implements IHttpBodySerializer {
    @Override
    public String contentType(Object body) {
        return "application/x-www-form-urlencoded";
    }

    @Override
    public boolean canSerialize(Object body) {
        return body instanceof Map<?, ?>;
    }

    @Override
    public InputStream serialize(Object body) {
        Map<?, ?> map = (Map<?, ?>) body;

        String result = map.entrySet().stream()
                .map(entry -> encode(String.valueOf(entry.getKey())) + "=" + encode(String.valueOf(entry.getValue())))
                .collect(Collectors.joining("&"));

        return new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
