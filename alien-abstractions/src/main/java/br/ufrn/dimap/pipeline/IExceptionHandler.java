package br.ufrn.dimap.pipeline;

import br.ufrn.dimap.types.HttpResponse;

public interface IExceptionHandler extends IAfterInterceptor {
    boolean canHandle(Exception exception);
    HttpResponse handle(Exception exception);
}
