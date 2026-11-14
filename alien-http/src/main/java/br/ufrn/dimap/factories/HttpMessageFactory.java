package br.ufrn.dimap.factories;

import br.ufrn.dimap.options.HttpMessageOptions;
import br.ufrn.dimap.types.HttpHeader;
import br.ufrn.dimap.types.HttpHeaders;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class HttpMessageFactory {
    protected final HttpMessageOptions options;
    protected record SerializedBody(String contentType, InputStream bodyStream) {}

    public HttpMessageFactory(HttpMessageOptions options) {
        this.options = options;
    }

    protected SerializedBody serializerBody(Object body) {
        for (var serializer : options.bodySerializers()) {
            if (serializer.canSerialize(body)) {
                return new SerializedBody(
                        serializer.contentType(body),
                        serializer.serialize(body)
                );
            }
        }

        throw new IllegalStateException("Can't serialize body of type " + body.getClass().getName());
    }

    protected HttpHeaders getHeaders(String contentType, long contentLength) {
        var headers = new HttpHeaders(options.defaultHeaders());

        if (options.includeDateHeader()) {
            String date = ZonedDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.RFC_1123_DATE_TIME);
            headers.set(HttpHeader.Date, date);
        }

        if (options.useKeepAlive())
            headers.set(HttpHeader.Connection, "keep-alive");
        else
            headers.set(HttpHeader.Connection, "close");

        if (contentLength <= 0)
            return headers;

        if (contentType != null && !contentType.isEmpty())
            headers.set(HttpHeader.ContentType, contentType);
        headers.set(HttpHeader.ContentLength, Long.toString(contentLength));

        return headers;
    }

    protected static long getContentLength(InputStream body) {
        if (body instanceof ByteArrayInputStream bais) {
            return bais.available();
        }
        if (body instanceof FileInputStream fis) {
            try {
                return fis.getChannel().size();
            } catch (IOException e) {
                return 0;
            }
        }
        return 0;
    }
}
