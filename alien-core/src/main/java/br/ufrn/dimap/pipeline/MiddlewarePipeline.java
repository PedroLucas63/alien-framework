package br.ufrn.dimap.pipeline;

import br.ufrn.dimap.pipeline.types.ProcessContext;

import java.util.List;
import java.util.function.Consumer;

/**
 * Composes registered {@link IMiddleware} components into a sequential execution chain
 * terminating at a downstream {@link Consumer}.
 */
public class MiddlewarePipeline {

    private final List<IMiddleware> middlewares;

    public MiddlewarePipeline(List<IMiddleware> middlewares) {
        this.middlewares = List.copyOf(middlewares);
    }

    public void invoke(ProcessContext context, Consumer<ProcessContext> terminal) {
        Consumer<ProcessContext> current = terminal;

        for (int i = middlewares.size() - 1; i >= 0; i--) {
            IMiddleware middleware = middlewares.get(i);
            Consumer<ProcessContext> next = current;
            current = ctx -> middleware.invoke(ctx, next);
        }

        current.accept(context);
    }
}
