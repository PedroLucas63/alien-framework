package br.ufrn.dimap.serializers;

import br.ufrn.dimap.types.HttpHeaders;
import br.ufrn.dimap.types.HttpRequest;
import br.ufrn.dimap.types.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public final class HttpMessageSerializer {
    private HttpMessageSerializer() {
    }

    public static byte[] serialize(HttpResponse response) throws IOException {
        var stream = new ByteArrayOutputStream();
        serialize(response, stream);
        return stream.toByteArray();
    }

    public static void serialize(HttpResponse response, OutputStream destination) throws IOException {
        String startLine = response.getVersion().getValue()
                + " "
                + response.getStatusCode().getCode()
                + " "
                + response.getReasonPhrase();

        serializeMessage(startLine, response.getHeaders(), response.getBodyStream(), destination);
    }

    public static byte[] serialize(HttpRequest request) throws IOException {
        var stream = new ByteArrayOutputStream();
        serialize(request, stream);
        return stream.toByteArray();
    }

    public static void serialize(HttpRequest request, OutputStream destination) throws IOException {
        String target = request.getPath();
        var queryParams = request.getQueryParameters();
        if (queryParams != null && !queryParams.values.isEmpty()) {
            String queryString = queryParams.values.entrySet().stream()
                    .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                            + "="
                            + URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            target += (target.contains("?") ? "&" : "?") + queryString;
        }

        String startLine = request.getMethod().getValue()
                + " "
                + target
                + " "
                + request.getVersion().getValue();

        serializeMessage(startLine, request.getHeaders(), request.getBodyStream(), destination);
    }

    private static void serializeMessage(
            String startLine,
            HttpHeaders headers,
            InputStream bodyStream,
            OutputStream destination
    ) throws IOException {
        var builder = new StringBuilder();
        builder.append(startLine).append("\r\n");

        for (var entry : headers.values.entrySet()) {
            builder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }

        builder.append("\r\n");

        byte[] headerBytes = builder.toString().getBytes(StandardCharsets.US_ASCII);
        destination.write(headerBytes);

        if (bodyStream != null) {
            bodyStream.transferTo(destination);
        }
    }
}
