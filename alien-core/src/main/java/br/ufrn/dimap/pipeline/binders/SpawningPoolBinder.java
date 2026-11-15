package br.ufrn.dimap.pipeline.binders;

import br.ufrn.dimap.annotations.binding.FromServices;
import br.ufrn.dimap.dependency_injection.IInversionOfControl;
import br.ufrn.dimap.pipeline.IParameterBinder;
import br.ufrn.dimap.pipeline.types.ProcessContext;

import java.lang.reflect.Parameter;

/**
 * Binds parameters resolved from the IoC container ({@link IInversionOfControl}),
 * optionally qualified with {@link FromServices}.
 */
public class SpawningPoolBinder implements IParameterBinder {

    @Override
    public boolean tryBind(ProcessContext context, Parameter parameter) {
        if (BinderUtils.hasOtherContentAnnotation(parameter, FromServices.class)) {
            return false;
        }
        if (context.scope == null) {
            return false;
        }

        FromServices fromServices = parameter.getAnnotation(FromServices.class);
        String name = fromServices != null && !fromServices.value().isBlank()
                ? fromServices.value()
                : null;

        IInversionOfControl ioc = context.scope.getContainer();
        if (ioc == null || !ioc.contains(parameter.getType(), name)) {
            return false;
        }

        context.methodParameters.add(ioc.spawn(parameter.getType(), name));
        return true;
    }
}
