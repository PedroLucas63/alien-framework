package br.ufrn.dimap.serializers;

import java.io.InputStream;

public interface IHttpBodySerializer {
    String contentType(Object body);
    boolean canSerialize(Object body);
    InputStream serialize(Object body);
}
