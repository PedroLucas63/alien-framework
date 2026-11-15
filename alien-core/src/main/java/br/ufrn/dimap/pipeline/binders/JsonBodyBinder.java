package br.ufrn.dimap.pipeline.binders;

import br.ufrn.dimap.annotations.binding.Body;
import br.ufrn.dimap.pipeline.IParameterBinder;
import br.ufrn.dimap.pipeline.types.ProcessContext;
import br.ufrn.dimap.types.HttpHeader;
import br.ufrn.dimap.types.HttpRequest;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.Optional;

/**
 * Binds JSON request bodies into target parameter types using Jackson.
 */
public class JsonBodyBinder implements IParameterBinder {

    private final ObjectMapper objectMapper;

    public JsonBodyBinder() {
        this(JsonMapper.builder()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .build());
    }

    public JsonBodyBinder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean tryBind(ProcessContext context, Parameter parameter) {
        if (BinderUtils.hasOtherContentAnnotation(parameter, Body.class)) {
            return false;
        }
        if (context.httpContext.isBodyRead) {
            return false;
        }

        HttpRequest request = context.httpContext.request;
        Optional<String> contentType = request.getHeaders().tryGetHeader(HttpHeader.ContentType);
        if (contentType.isEmpty() || !contentType.get().toLowerCase(Locale.ROOT).contains("application/json")) {
            return false;
        }

        Optional<String> contentLengthHeader = request.getHeaders().tryGetHeader(HttpHeader.ContentLength);
        if (contentLengthHeader.isEmpty()) {
            return false;
        }

        int contentLength;
        try {
            contentLength = Integer.parseInt(contentLengthHeader.get().trim());
        } catch (NumberFormatException e) {
            return false;
        }
        if (contentLength <= 0) {
            return false;
        }

        try {
            byte[] bodyBytes = request.getBodyStream().readNBytes(contentLength);
            context.httpContext.isBodyRead = true;
            Object value = objectMapper.readValue(bodyBytes, parameter.getType());
            context.methodParameters.add(value);
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to deserialize JSON request body", e);
        }
    }
}
