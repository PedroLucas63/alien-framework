package br.ufrn.dimap.pipeline.middlewares;

import br.ufrn.dimap.dependency_injection.IServiceScope;
import br.ufrn.dimap.dependency_injection.IServiceScopeFactory;
import br.ufrn.dimap.pipeline.IMiddleware;
import br.ufrn.dimap.pipeline.types.ProcessContext;
import br.ufrn.dimap.spawning_pool.types.SessionId;
import br.ufrn.dimap.types.HttpRequest;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Resolves or creates a {@link SessionId} for the request and opens a scoped
 * {@link IServiceScope} for the duration of downstream pipeline execution.
 */
public class ScopeMiddleware implements IMiddleware {

    private final IServiceScopeFactory scopeFactory;

    public ScopeMiddleware(IServiceScopeFactory scopeFactory) {
        this.scopeFactory = scopeFactory;
    }

    @Override
    public void invoke(ProcessContext context, Consumer<ProcessContext> next) {
        if (context.userId == null) {
            context.userId = new SessionId(parseOrGenerateUserId(context.httpContext.request));
        }

        try (IServiceScope scope = scopeFactory.createScope(context.userId)) {
            context.scope = scope;
            next.accept(context);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to close service scope", e);
        }
    }

    private static UUID parseOrGenerateUserId(HttpRequest request) {
        if (request != null && request.getHeaders() != null) {
            Optional<String> headerValue = request.getHeaders().tryGetHeader(SessionId.HEADER_TITLE);
            if (headerValue.isPresent()) {
                try {
                    return UUID.fromString(headerValue.get().trim());
                } catch (IllegalArgumentException ignored) {
                    // Fall back to a new session UUID when the header value is not a valid UUID.
                }
            }
        }
        return UUID.randomUUID();
    }
}
