package br.ufrn.dimap.pipeline;

import br.ufrn.dimap.pipeline.types.ProcessContext;

public interface IBeforeInterceptor {
    void before(ProcessContext context);
}
