package br.ufrn.dimap.pipeline.interceptors;

import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.pipeline.IExceptionHandler;
import br.ufrn.dimap.pipeline.types.ProcessContext;
import br.ufrn.dimap.types.HttpResponse;

/**
 * Default {@link IExceptionHandler} that converts unhandled exceptions into
 * HTTP 500 Internal Server Error responses.
 */
public class ExceptionInterceptor implements IExceptionHandler {

    protected final HttpResponseFactory responseFactory;

    public ExceptionInterceptor(HttpResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    @Override
    public void after(ProcessContext context) {
        if (context.exception != null && canHandle(context.exception)) {
            context.httpContext.response = handle(context.exception);
        }
    }

    @Override
    public boolean canHandle(Exception exception) {
        return true;
    }

    @Override
    public HttpResponse handle(Exception exception) {
        return responseFactory.internalServerError(exception.getMessage());
    }
}
