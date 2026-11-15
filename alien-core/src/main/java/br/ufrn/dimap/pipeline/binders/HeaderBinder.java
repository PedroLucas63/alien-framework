package br.ufrn.dimap.pipeline.binders;

import br.ufrn.dimap.annotations.binding.Header;
import br.ufrn.dimap.pipeline.IParameterBinder;
import br.ufrn.dimap.pipeline.types.ProcessContext;

import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * Binds parameters annotated with {@link Header} from the HTTP request headers.
 */
public class HeaderBinder implements IParameterBinder {

    @Override
    public boolean tryBind(ProcessContext context, Parameter parameter) {
        Header headerAnnotation = parameter.getAnnotation(Header.class);
        if (headerAnnotation == null || BinderUtils.hasOtherContentAnnotation(parameter, Header.class)) {
            return false;
        }

        String headerName = BinderUtils.resolveParameterName(parameter, headerAnnotation.value());
        Optional<String> rawValue = context.httpContext.request.getHeaders().tryGetHeader(headerName);
        if (rawValue.isEmpty()) {
            return false;
        }

        Optional<Object> converted = BinderUtils.tryConvert(rawValue.get(), parameter.getType());
        if (converted.isEmpty()) {
            return false;
        }

        context.methodParameters.add(converted.get());
        return true;
    }
}
