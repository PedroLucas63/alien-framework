package br.ufrn.dimap.pipeline;

import br.ufrn.dimap.pipeline.types.ProcessContext;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * Coordinates registered {@link IParameterBinder} instances to bind endpoint parameters.
 */
public class UniversalTranslator {

    private final List<IParameterBinder> binders;

    public UniversalTranslator(List<IParameterBinder> binders) {
        this.binders = List.copyOf(binders);
    }

    public boolean tryBind(ProcessContext context, Parameter parameter) {
        for (IParameterBinder binder : binders) {
            if (binder.tryBind(context, parameter)) {
                return true;
            }
        }
        return false;
    }
}
