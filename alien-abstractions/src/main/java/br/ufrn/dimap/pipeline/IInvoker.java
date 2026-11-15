package br.ufrn.dimap.pipeline;

import br.ufrn.dimap.pipeline.types.ProcessContext;

public interface IInvoker {
    void invoke(ProcessContext context);
}
