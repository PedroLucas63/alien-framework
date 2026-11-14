package br.ufrn.dimap.serializers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ByteArrayBodySerializer implements IHttpBodySerializer {
    @Override
    public String contentType(Object body) {
        return "application/octet-stream";
    }

    @Override
    public boolean canSerialize(Object body) {
        return body instanceof byte[];
    }

    @Override
    public InputStream serialize(Object body) {
        byte[] bytes = body instanceof byte[] data ? data : new byte[0];
        return new ByteArrayInputStream(bytes);
    }
}
