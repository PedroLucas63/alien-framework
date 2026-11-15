package br.ufrn.dimap.pipeline;

import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.pipeline.types.ProcessContext;
import br.ufrn.dimap.transport.IBeamDispatcher;
import br.ufrn.dimap.types.HttpContext;

/**
 * Entry point of the request processing pipeline, bridging {@link IBeamDispatcher}
 * with {@link MiddlewarePipeline} and {@link InterceptorPipeline}.
 */
public class RequestDispatcher implements IBeamDispatcher {

    private final MiddlewarePipeline middlewarePipeline;
    private final InterceptorPipeline interceptorPipeline;
    private final HttpResponseFactory responseFactory;

    public RequestDispatcher(
            MiddlewarePipeline middlewarePipeline,
            InterceptorPipeline interceptorPipeline,
            HttpResponseFactory responseFactory
    ) {
        this.middlewarePipeline = middlewarePipeline;
        this.interceptorPipeline = interceptorPipeline;
        this.responseFactory = responseFactory;
    }

    @Override
    public void dispatch(HttpContext httpContext) {
        if (httpContext.request == null) {
            httpContext.response = responseFactory.badRequest("Missing request.");
            return;
        }

        ProcessContext context = new ProcessContext(httpContext);
        middlewarePipeline.invoke(context, interceptorPipeline::execute);
    }
}
