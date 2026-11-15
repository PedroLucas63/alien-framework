package br.ufrn.dimap.pipeline.binders;

import br.ufrn.dimap.annotations.binding.Param;
import br.ufrn.dimap.pipeline.IParameterBinder;
import br.ufrn.dimap.pipeline.types.ProcessContext;

import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * Binds route parameters from {@link ProcessContext#routeValues},
 * optionally qualified with {@link Param}.
 */
public class ParamBinder implements IParameterBinder {

    @Override
    public boolean tryBind(ProcessContext context, Parameter parameter) {
        if (BinderUtils.hasOtherContentAnnotation(parameter, Param.class)) {
            return false;
        }
        if (context.routeValues == null) {
            return false;
        }

        Param paramAnnotation = parameter.getAnnotation(Param.class);
        String explicitName = paramAnnotation != null ? paramAnnotation.value() : null;
        String paramName = BinderUtils.resolveParameterName(parameter, explicitName);

        String rawValue = context.routeValues.get(paramName);
        if (rawValue == null) {
            return false;
        }

        Optional<Object> converted = BinderUtils.tryConvert(rawValue, parameter.getType());
        if (converted.isEmpty()) {
            return false;
        }

        context.methodParameters.add(converted.get());
        return true;
    }
}
