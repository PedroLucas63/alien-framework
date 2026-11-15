package br.ufrn.dimap.pipeline;

import br.ufrn.dimap.pipeline.types.ProcessContext;

import java.lang.reflect.Parameter;
import java.util.Optional;

public interface IParameterBinder {
    boolean tryBind(ProcessContext context, Parameter parameter);
}
