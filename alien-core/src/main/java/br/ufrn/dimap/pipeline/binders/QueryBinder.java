package br.ufrn.dimap.pipeline.binders;

import br.ufrn.dimap.annotations.binding.Query;
import br.ufrn.dimap.pipeline.IParameterBinder;
import br.ufrn.dimap.pipeline.types.ProcessContext;
import br.ufrn.dimap.types.HttpQueryParameters;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * Binds parameters annotated with {@link Query} from HTTP query parameters,
 * supporting both scalar values and DTO classes instantiated via constructor injection.
 */
public class QueryBinder implements IParameterBinder {

    @Override
    public boolean tryBind(ProcessContext context, Parameter parameter) {
        if (!parameter.isAnnotationPresent(Query.class)
                || BinderUtils.hasOtherContentAnnotation(parameter, Query.class)) {
            return false;
        }

        HttpQueryParameters queryParams = context.httpContext.request.getQueryParameters();
        Class<?> targetType = parameter.getType();

        if (BinderUtils.isSupportedType(targetType)) {
            String paramName = BinderUtils.resolveParameterName(parameter, null);
            Optional<String> rawValue = queryParams.tryGetParameter(paramName);
            if (rawValue.isEmpty()) {
                return false;
            }
            Optional<Object> converted = BinderUtils.tryConvert(rawValue.get(), targetType);
            if (converted.isEmpty()) {
                return false;
            }
            context.methodParameters.add(converted.get());
            return true;
        }

        return tryBindObject(context, queryParams, targetType);
    }

    private boolean tryBindObject(ProcessContext context, HttpQueryParameters queryParams, Class<?> targetType) {
        Constructor<?>[] constructors = targetType.getConstructors();
        Arrays.sort(constructors, Comparator.comparingInt((Constructor<?> c) -> c.getParameterCount()).reversed());

        for (Constructor<?> constructor : constructors) {
            Parameter[] ctorParams = constructor.getParameters();
            Object[] args = new Object[ctorParams.length];
            boolean compatible = true;

            for (int i = 0; i < ctorParams.length; i++) {
                Parameter ctorParam = ctorParams[i];
                Class<?> ctorParamType = ctorParam.getType();
                if (!BinderUtils.isSupportedType(ctorParamType)) {
                    compatible = false;
                    break;
                }

                String name = BinderUtils.resolveParameterName(ctorParam, null);
                Optional<String> rawValue = queryParams.tryGetParameter(name);
                if (rawValue.isPresent()) {
                    Optional<Object> converted = BinderUtils.tryConvert(rawValue.get(), ctorParamType);
                    if (converted.isEmpty()) {
                        compatible = false;
                        break;
                    }
                    args[i] = converted.get();
                } else {
                    args[i] = BinderUtils.defaultValue(ctorParamType);
                }
            }

            if (compatible) {
                try {
                    context.methodParameters.add(constructor.newInstance(args));
                    return true;
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to instantiate query object of type " + targetType.getName(), e);
                }
            }
        }
        return false;
    }
}
