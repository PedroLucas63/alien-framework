package br.ufrn.dimap.pipeline.binders;

import br.ufrn.dimap.pipeline.IParameterBinder;
import br.ufrn.dimap.pipeline.types.ProcessContext;

import java.lang.reflect.Parameter;

/**
 * Binds {@link ProcessContext} parameters directly to the active execution context.
 */
public class ProcessContextBinder implements IParameterBinder {

    @Override
    public boolean tryBind(ProcessContext context, Parameter parameter) {
        if (BinderUtils.hasOtherContentAnnotation(parameter, null)) {
            return false;
        }
        if (!parameter.getType().equals(ProcessContext.class)) {
            return false;
        }
        context.methodParameters.add(context);
        return true;
    }
}
