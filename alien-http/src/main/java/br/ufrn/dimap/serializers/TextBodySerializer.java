package br.ufrn.dimap.serializers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TextBodySerializer implements IHttpBodySerializer {
    @Override
    public String contentType(Object body) {
        return "text/plain; charset=utf-8";
    }

    @Override
    public boolean canSerialize(Object body) {
        return body instanceof String;
    }

    @Override
    public InputStream serialize(Object body) {
        var strBody = (String) body;
        return new ByteArrayInputStream(strBody.getBytes(StandardCharsets.UTF_8));
    }
}
