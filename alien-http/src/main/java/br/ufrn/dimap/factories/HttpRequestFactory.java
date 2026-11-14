package br.ufrn.dimap.factories;

import br.ufrn.dimap.options.HttpMessageOptions;
import br.ufrn.dimap.types.HttpMethod;
import br.ufrn.dimap.types.HttpRequest;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpRequestFactory extends HttpMessageFactory{
    public HttpRequestFactory(HttpMessageOptions options) {
        super(options);
    }

    public HttpRequest get(String path) {
        return create(path, HttpMethod.Get);
    }
    public HttpRequest head(String path) {
        return create(path, HttpMethod.Head);
    }
    public HttpRequest trace(String path) {
        return create(path, HttpMethod.Trace);
    }
    public HttpRequest options(String path) {
        return create(path, HttpMethod.Options);
    }
    public HttpRequest connect(String path) {
        return create(path, HttpMethod.Connect);
    }

    public HttpRequest post(String path, Object body) {
        return create(path, HttpMethod.Post, body);
    }
    public HttpRequest put(String path, Object body) {
        return create(path, HttpMethod.Put, body);
    }
    public HttpRequest query(String path, Object body) {
        return create(path, HttpMethod.Query, body);
    }

    public HttpRequest patch(String path) {
        return patch(path, null);
    }
    public HttpRequest patch(String path, Object body) {
        return create(path, HttpMethod.Patch, body);
    }

    public HttpRequest delete(String path) {
        return delete(path, null);
    }
    public HttpRequest delete(String path, Object body) {
        return create(path, HttpMethod.Delete, body);
    }

    private HttpRequest create(String path, HttpMethod method) {
        return create(path, method, null);
    }

    private HttpRequest create(String path, HttpMethod method, Object content) {
        var contentType = "";
        var body = InputStream.nullInputStream();

        if (content != null) {
            var serialized = serializerBody(content);
            contentType = serialized.contentType();
            body = serialized.bodyStream();
        }

        var contentLength = getContentLength(body);
        var headers = getHeaders(contentType, contentLength);

        return new HttpRequest.Builder()
                .withMethod(method)
                .withPath(path)
                .withVersion(options.version())
                .withHeaders(headers)
                .withBody(body)
                .build();
    }
}
