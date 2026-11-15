package br.ufrn.dimap.pipeline.binders;

import br.ufrn.dimap.pipeline.IParameterBinder;
import br.ufrn.dimap.pipeline.types.ProcessContext;
import br.ufrn.dimap.types.HttpContext;

import java.lang.reflect.Parameter;

/**
 * Binds {@link HttpContext} parameters from the current {@link ProcessContext}.
 */
public class HttpContextBinder implements IParameterBinder {

    @Override
    public boolean tryBind(ProcessContext context, Parameter parameter) {
        if (BinderUtils.hasOtherContentAnnotation(parameter, null)) {
            return false;
        }
        if (!parameter.getType().equals(HttpContext.class)) {
            return false;
        }
        context.methodParameters.add(context.httpContext);
        return true;
    }
}
