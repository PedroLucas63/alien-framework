package br.ufrn.dimap.pipeline.middlewares;

import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.pipeline.IMiddleware;
import br.ufrn.dimap.pipeline.types.ProcessContext;
import br.ufrn.dimap.routing.IRouteMatcher;
import br.ufrn.dimap.routing.types.RouteMatchResult;
import br.ufrn.dimap.types.HttpRequest;

import java.util.function.Consumer;

/**
 * Matches incoming HTTP requests against registered routes and populates
 * {@link ProcessContext#endpointDescriptor} and {@link ProcessContext#routeValues}.
 */
public class RoutingMiddleware implements IMiddleware {

    private final IRouteMatcher routeMatcher;
    private final HttpResponseFactory responseFactory;

    public RoutingMiddleware(IRouteMatcher routeMatcher, HttpResponseFactory responseFactory) {
        this.routeMatcher = routeMatcher;
        this.responseFactory = responseFactory;
    }

    @Override
    public void invoke(ProcessContext context, Consumer<ProcessContext> next) {
        HttpRequest request = context.httpContext.request;
        if (request == null) {
            context.httpContext.response = responseFactory.badRequest("Missing request.");
            return;
        }

        RouteMatchResult result = routeMatcher.match(request.getMethod(), request.getPath());
        if (!result.success()) {
            context.httpContext.response = responseFactory.notFound("Route '" + request.getPath() + "' not found.");
            return;
        }

        if (result.endpoint() == null) {
            context.httpContext.response = responseFactory.internalServerError("Missing endpoint descriptor.");
            return;
        }

        context.endpointDescriptor = result.endpoint();
        context.routeValues = result.routeValues();

        next.accept(context);
    }
}
