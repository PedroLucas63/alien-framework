package br.ufrn.dimap.pipeline.interceptors;

import br.ufrn.dimap.dependency_injection.types.UserId;
import br.ufrn.dimap.pipeline.IAfterInterceptor;
import br.ufrn.dimap.pipeline.types.ProcessContext;
import br.ufrn.dimap.spawning_pool.types.SessionId;
import br.ufrn.dimap.types.HttpResponse;

/**
 * Appends the {@link SessionId#HEADER_TITLE} header to the HTTP response when a {@link UserId} is present.
 */
public class AddSessionIdHeaderInterceptor implements IAfterInterceptor {

    @Override
    public void after(ProcessContext context) {
        HttpResponse response = context.httpContext.response;
        UserId userId = context.userId;
        if (response == null || userId == null) {
            return;
        }
        response.getHeaders().add(SessionId.HEADER_TITLE, userId.toString());
    }
}
