package br.ufrn.dimap.service_discovery.transport;

import br.ufrn.dimap.factories.HttpRequestFactory;
import br.ufrn.dimap.service_discovery.configs.ServiceDiscoveryConfig;
import br.ufrn.dimap.service_discovery.dtos.GetAllServicesResponse;
import br.ufrn.dimap.service_discovery.dtos.GetServicesByNameRequest;
import br.ufrn.dimap.service_discovery.dtos.GetServicesByNameResponse;
import br.ufrn.dimap.service_discovery.dtos.RegisterServiceRequest;
import br.ufrn.dimap.service_discovery.dtos.RegisterServiceResponse;
import br.ufrn.dimap.types.HttpHeader;
import br.ufrn.dimap.types.HttpHeaders;
import br.ufrn.dimap.types.HttpRequest;
import br.ufrn.dimap.types.HttpResponse;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Base implementation for service discovery transport protocols over HTTP messages.
 */
public abstract class DiscoveryProtocolBase implements IDiscoveryProtocol {

    private static final int MAX_RESPONSE_BODY_BYTES = 10 * 1024 * 1024;

    protected final ServiceDiscoveryConfig config;
    private final HttpRequestFactory requestFactory;
    private final ObjectMapper objectMapper;

    protected DiscoveryProtocolBase(ServiceDiscoveryConfig config, HttpRequestFactory requestFactory) {
        this.config = config;
        this.requestFactory = requestFactory;
        this.objectMapper = JsonMapper.builder()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .build();
    }

    protected String basePath() {
        String raw = config.registryPath == null ? "services" : config.registryPath;
        return "/" + raw.replaceAll("^/+|/+$", "");
    }

    @Override
    public RegisterServiceResponse registerService(RegisterServiceRequest request) {
        HttpRequest message = requestFactory.post(basePath(), request);
        HttpResponse response = executeSend(message);

        RegisterServiceResponse fallback = new RegisterServiceResponse(false, "An internal error has occurred");
        return deserializeResponse(response, RegisterServiceResponse.class).orElse(fallback);
    }

    @Override
    public Optional<GetServicesByNameResponse> getByServiceName(GetServicesByNameRequest request) {
        String name = request != null && request.name() != null ? request.name() : "";
        String path = basePath() + "?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
        HttpRequest message = requestFactory.get(path);
        HttpResponse response = executeSend(message);

        return deserializeResponse(response, GetServicesByNameResponse.class);
    }

    @Override
    public Optional<GetAllServicesResponse> getAllServices() {
        String path = basePath() + "/all";
        HttpRequest message = requestFactory.get(path);
        HttpResponse response = executeSend(message);

        return deserializeResponse(response, GetAllServicesResponse.class);
    }

    @Override
    public boolean deregisterService(UUID serviceId) {
        String path = basePath() + "/" + serviceId;
        HttpRequest message = requestFactory.delete(path);
        HttpResponse response = executeSend(message);

        return deserializeResponse(response, Boolean.class).orElse(false);
    }

    @Override
    public boolean pulse(UUID serviceId) {
        String path = basePath() + "/" + serviceId;
        HttpRequest message = requestFactory.patch(path);
        HttpResponse response = executeSend(message);

        return deserializeResponse(response, Boolean.class).orElse(false);
    }

    protected abstract HttpResponse sendRequest(HttpRequest request) throws IOException;

    private HttpResponse executeSend(HttpRequest request) {
        try {
            return sendRequest(request);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to send service discovery request", e);
        }
    }

    private <T> Optional<T> deserializeResponse(HttpResponse response, Class<T> targetType) {
        if (response == null) {
            return Optional.empty();
        }
        int statusCode = response.getStatusCode().getCode();
        if (statusCode < 200 || statusCode >= 300) {
            return Optional.empty();
        }

        HttpHeaders headers = response.getHeaders();
        Optional<String> contentType = headers.tryGetHeader(HttpHeader.ContentType);
        Optional<String> contentLength = headers.tryGetHeader(HttpHeader.ContentLength);
        if (contentType.isEmpty()
                || !contentType.get().toLowerCase(Locale.ROOT).contains("application/json")
                || contentLength.isEmpty()) {
            return Optional.empty();
        }

        int length;
        try {
            length = Integer.parseInt(contentLength.get().trim());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        if (length <= 0 || length > MAX_RESPONSE_BODY_BYTES) {
            return Optional.empty();
        }

        try {
            byte[] bytes = response.getBodyStream().readNBytes(length);
            if (bytes.length < length) {
                return Optional.empty();
            }
            return Optional.ofNullable(objectMapper.readValue(bytes, targetType));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
