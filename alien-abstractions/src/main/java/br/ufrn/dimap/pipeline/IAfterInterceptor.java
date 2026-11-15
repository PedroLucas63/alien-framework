package br.ufrn.dimap.pipeline;

import br.ufrn.dimap.pipeline.types.ProcessContext;

public interface IAfterInterceptor {
    void after(ProcessContext context);
}
