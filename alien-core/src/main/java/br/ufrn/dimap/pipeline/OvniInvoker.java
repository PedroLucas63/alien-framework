package br.ufrn.dimap.pipeline;

import br.ufrn.dimap.dependency_injection.IInversionOfControl;
import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.pipeline.types.ProcessContext;
import br.ufrn.dimap.types.HttpResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Default {@link IInvoker} implementation that instantiates controller classes via
 * {@link IInversionOfControl}, invokes the target endpoint method, and adapts its return
 * value into an {@link HttpResponse}.
 */
public class OvniInvoker implements IInvoker {

    private final IInversionOfControl inversionOfControl;
    private final HttpResponseFactory responseFactory;

    public OvniInvoker(IInversionOfControl inversionOfControl, HttpResponseFactory responseFactory) {
        this.inversionOfControl = inversionOfControl;
        this.responseFactory = responseFactory;
    }

    @Override
    public void invoke(ProcessContext context) {
        Objects.requireNonNull(context.endpointDescriptor, "EndpointDescriptor must not be null");

        Method method = context.endpointDescriptor.methodFunction();
        Object[] parameters = context.methodParameters.toArray();
        Class<?> declaringType = method.getDeclaringClass();

        Object result;
        try {
            if (Modifier.isStatic(method.getModifiers())) {
                result = method.invoke(null, parameters);
            } else {
                Object instance = inversionOfControl.spawn(declaringType);
                result = method.invoke(instance, parameters);
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new RuntimeException("Endpoint invocation failed", cause != null ? cause : e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access endpoint method: " + method.getName(), e);
        }

        context.httpContext.response = adaptResponse(result);
    }

    private HttpResponse adaptResponse(Object result) {
        if (result == null) {
            return responseFactory.noContent();
        }
        if (result instanceof HttpResponse response) {
            return response;
        }
        return responseFactory.ok(result);
    }
}
