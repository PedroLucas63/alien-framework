package br.ufrn.dimap.parsers;

import br.ufrn.dimap.exceptions.HttpException;
import br.ufrn.dimap.network.HttpBodyStream;
import br.ufrn.dimap.network.HttpStreamReader;
import br.ufrn.dimap.types.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpParser {
    private enum MessageType {
        Response,
        Request,
        Invalid
    }

    public static HttpMessage parse(byte[] buffer) throws IOException {
        return parse(new ByteArrayInputStream(buffer));
    }

    public static HttpMessage parse(final InputStream stream) throws IOException {
        var reader = new HttpStreamReader(stream);
        var startLine = reader.readLine();
        var type = getMessageType(startLine);

        return switch (type) {
            case Response -> parseResponse(reader, startLine);
            case Request -> parseRequest(reader, startLine);
            default -> throw new HttpException("HTTP message type not identified.");
        };
    }

    private static MessageType getMessageType(String startLine) {
        if (startLine == null || startLine.isEmpty())
            return MessageType.Invalid;

        var tokens = startLine.trim().split("\\s+");
        var first = tokens[0];

        if (HttpMethod.tryParse(first).isPresent())
            return MessageType.Request;

        if (HttpVersion.tryParse(first).isPresent())
            return MessageType.Response;

        return MessageType.Invalid;
    }

    public static HttpRequest parseRequest(byte[] buffer) throws IOException {
        return parseRequest(new ByteArrayInputStream(buffer));
    }

    public static HttpRequest parseRequest(InputStream stream) throws IOException {
        var reader =  new HttpStreamReader(stream);
        var startLine = reader.readLine();
        return parseRequest(reader, startLine);
    }

    private static HttpRequest parseRequest(HttpStreamReader reader, String startLine) throws IOException {
        var builder = new HttpRequest.Builder();
        parseRequestFirstLine(startLine, builder);
        parseHeaders(reader, builder);
        parseBody(reader, builder);
        return builder.build();
    }

    private static void parseRequestFirstLine(String startLine, HttpRequest.Builder builder) {
        if (startLine == null)
            throw new HttpException("Invalid request: unexpected end of stream.");

        var tokens = startLine.trim().split("\\s+");
        if (tokens.length != 3)
            throw new HttpException("Invalid request: start line does not have 3 parts.");

        var method = HttpMethod.tryParse(tokens[0]);
        if (method.isEmpty())
            throw new HttpException("Invalid request: method not identified.");

        var path = parsePath(tokens[1]);
        var queryParameters = parseQueryParameters(tokens[1]);

        var version = HttpVersion.tryParse(tokens[2]);
        if (version.isEmpty())
            throw new HttpException("Invalid request: version not identified.");

        builder.withMethod(method.get())
                .withPath(path)
                .withQueryParameters(queryParameters)
                .withVersion(version.get());
    }

    private static String parsePath(String path) {
        if (path == null || !path.startsWith("/"))
            throw new HttpException("Invalid request: path does not start with '/'.");

        var pathParts = path.split("\\?", 2);
        return pathParts[0];
    }

    private static HttpQueryParameters parseQueryParameters(String path) {
        var queryParameters = new HttpQueryParameters();
        var pathParts = path.split("\\?", 2);

        if (pathParts.length != 2)
            return queryParameters;

        var query = pathParts[1];
        var fields = query.split("&+");

        for (var field : fields) {
            if (field.isEmpty())
                continue;

            var fieldParts = field.split("=", 2);

            if (fieldParts.length == 1) {
                var key = fieldParts[0];

                if (key == null || key.isEmpty())
                    throw new HttpException("Invalid request: query parameter key is empty.");

                queryParameters.add(key, Boolean.toString(true));
            } else if (fieldParts.length == 2) {
                var key = fieldParts[0];
                var value = fieldParts[1];

                if (key == null || key.isEmpty() || value == null || value.isEmpty())
                    throw new HttpException("Invalid request: query parameter value is empty.");
                queryParameters.add(key, value);
            } else {
                throw new HttpException("Invalid request: query parameter key or value is empty.");
            }
        }

        return queryParameters;
    }

    public static HttpResponse parseResponse(byte[] buffer) throws IOException {
        return parseResponse(new ByteArrayInputStream(buffer));
    }

    public static HttpResponse parseResponse(InputStream stream) throws IOException {
        var reader =  new HttpStreamReader(stream);
        var startLine = reader.readLine();
        return parseResponse(reader, startLine);
    }

    private static HttpResponse parseResponse(HttpStreamReader reader, String startLine) throws IOException {
        var builder = new HttpResponse.Builder();
        parseResponseFirstLine(startLine, builder);
        parseHeaders(reader, builder);
        parseBody(reader, builder);
        return builder.build();
    }

    private static void parseResponseFirstLine(String startLine, HttpResponse.Builder builder) {
        if (startLine == null)
            throw new HttpException("Invalid response: unexpected end of stream.");

        var tokens = startLine.trim().split("\\s+", 3);
        if (tokens.length != 3)
            throw new HttpException("Invalid response: start line does not have 3 parts.");

        var version = HttpVersion.tryParse(tokens[0]);
        if (version.isEmpty())
            throw new HttpException("Invalid response: version not identified.");

        var statusCode = parseStatusCode(tokens[1]);
        var reasonPhrase = tokens[2].trim();

        builder.withVersion(version.get())
                .withStatusCode(statusCode)
                .withReasonPhrase(reasonPhrase);
    }

    private static HttpStatusCode parseStatusCode(String statusCode) {
        int code;
        try {
            code = Integer.parseInt(statusCode);
        } catch (NumberFormatException e) {
            throw new HttpException("Invalid response: invalid status code '" + statusCode + "'.");
        }

        var status = HttpStatusCode.tryParse(code);
        if (status.isEmpty())
            throw new HttpException("Invalid response: status not identified.");
        return status.get();
    }

    private static void parseHeaders(HttpStreamReader reader, HttpMessage.Builder<?, ?> builder) throws IOException {
        var line = reader.readLine();
        while (line != null && !line.isEmpty()) {
            parseHeader(line, builder);
            line = reader.readLine();
        }

        if (line == null)
            throw new HttpException("Unexpected end of stream: incomplete HTTP headers.");
    }

    private static void parseHeader(String line, HttpMessage.Builder<?, ?> builder) {
        var headerParts = line.trim().split(":", 2);
        if (headerParts.length != 2)
            throw new HttpException("Invalid header: header not identified.");

        var key = headerParts[0].trim();
        var value = headerParts[1].trim();

        if (key.isEmpty() || value.isEmpty())
            throw new HttpException("Invalid header: key or value is empty.");

        builder.withHeader(key, value);
    }

    private static void parseBody(HttpStreamReader reader, HttpMessage.Builder<?, ?> builder) throws IOException {
        var contentLength = 0L;

        var headerValue = builder.getHeaders().tryGetHeader(HttpHeader.ContentLength);
        if (headerValue.isPresent()) {
            try {
                contentLength = Long.parseLong(headerValue.get());
            } catch (NumberFormatException e) {
                throw new HttpException("Invalid Content-Length header: '" + headerValue.get() + "'.");
            }
        }

        InputStream bodyStream = contentLength > 0
                ? new HttpBodyStream(reader, contentLength)
                : InputStream.nullInputStream();
        builder.withBody(bodyStream);
    }
}
