package br.ufrn.dimap.api_gateway.services;

import br.ufrn.dimap.api_gateway.managers.IServicesManager;
import br.ufrn.dimap.api_gateway.transport.IRedirectProtocol;
import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.routing.IRouteRegistry;
import br.ufrn.dimap.routing.types.EndpointDescriptor;
import br.ufrn.dimap.service_discovery.dtos.GetAllServicesResponse;
import br.ufrn.dimap.service_discovery.dtos.GetServicesByNameResponse;
import br.ufrn.dimap.service_discovery.dtos.ServiceResponse;
import br.ufrn.dimap.types.HttpContext;
import br.ufrn.dimap.types.HttpRequest;
import br.ufrn.dimap.types.HttpResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default {@link IApiGatewayService} that registers wildcard routes for discovered services
 * and forwards requests with round-robin load balancing and failover.
 */
public class ApiGatewayService implements IApiGatewayService {

    private static final int MAX_FAILOVER_ATTEMPTS = 3;

    private final IRouteRegistry routeRegistry;
    private final IServicesManager servicesManager;
    private final IRedirectProtocol redirectProtocol;
    private final HttpResponseFactory responseFactory;

    private final Lock routesLock = new ReentrantLock();
    private final Set<String> registeredRoutes = new HashSet<>();

    public ApiGatewayService(
            IRouteRegistry routeRegistry,
            IServicesManager servicesManager,
            IRedirectProtocol redirectProtocol,
            HttpResponseFactory responseFactory
    ) {
        this.routeRegistry = routeRegistry;
        this.servicesManager = servicesManager;
        this.redirectProtocol = redirectProtocol;
        this.responseFactory = responseFactory;
    }

    @Override
    public HttpResponse route(HttpContext httpContext) {
        if (httpContext.request == null) {
            return responseFactory.badRequest("Missing request");
        }
        if (httpContext.response != null) {
            return httpContext.response;
        }

        String rawPath = httpContext.request.getPath();
        String trimmedPath = rawPath == null ? "" : rawPath.replaceAll("^/+", "");
        if (trimmedPath.isEmpty()) {
            return responseFactory.badRequest("Missing service name");
        }

        String[] segments = trimmedPath.split("/", 2);
        String serviceName = segments[0];
        String newPath = segments.length > 1 ? "/" + segments[1] : "/";

        HttpRequest orig = httpContext.request;
        HttpRequest forwardedRequest = new HttpRequest(
                orig.getMethod(),
                newPath,
                orig.getVersion(),
                orig.getQueryParameters(),
                orig.getHeaders(),
                orig.getBodyStream()
        );

        Set<UUID> triedInstances = new HashSet<>();
        for (int attempt = 0; attempt < MAX_FAILOVER_ATTEMPTS; attempt++) {
            ServiceResponse serviceResponse;
            try {
                serviceResponse = servicesManager.getNext(serviceName);
            } catch (IllegalStateException e) {
                return attempt == 0
                        ? responseFactory.notFound("No services with name \"" + serviceName + "\" were found.")
                        : responseFactory.badGateway("Connection refused");
            }

            if (!triedInstances.add(serviceResponse.id())) {
                return responseFactory.badGateway("Connection refused");
            }

            try {
                httpContext.response = redirectProtocol.send(serviceResponse, forwardedRequest);
                return httpContext.response;
            } catch (IOException e) {
                servicesManager.removeService(serviceName, serviceResponse.id());
            } catch (RuntimeException e) {
                return responseFactory.internalServerError("An internal server error occurred");
            }
        }

        return responseFactory.badGateway("Connection refused");
    }

    @Override
    public void addOrUpdateServices(GetAllServicesResponse response) {
        Method routeMethod = resolveRouteMethod();

        for (GetServicesByNameResponse servicesInfo : response.servicesByName()) {
            String normalizedName = servicesInfo.name().toLowerCase(Locale.ROOT);
            routesLock.lock();
            try {
                if (registeredRoutes.contains(normalizedName)) {
                    continue;
                }

                String pathPattern = servicesInfo.name() + "/*";
                EndpointDescriptor descriptor = new EndpointDescriptor(
                        null,
                        pathPattern,
                        IApiGatewayService.class,
                        routeMethod,
                        List.of(routeMethod.getParameters()),
                        List.of()
                );

                routeRegistry.mapEndpoint(descriptor);
                registeredRoutes.add(normalizedName);
            } finally {
                routesLock.unlock();
            }
        }

        servicesManager.updateServices(response);
    }

    private static Method resolveRouteMethod() {
        try {
            return IApiGatewayService.class.getMethod("route", HttpContext.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not locate IApiGatewayService#route(HttpContext)", e);
        }
    }
}
