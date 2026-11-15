package br.ufrn.dimap.pipeline;

import br.ufrn.dimap.pipeline.types.ProcessContext;

import java.util.function.Consumer;

public interface IMiddleware {
    void invoke(ProcessContext context, Consumer<ProcessContext> next);
}
