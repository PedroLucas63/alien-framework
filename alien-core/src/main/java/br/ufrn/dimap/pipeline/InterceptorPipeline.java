package br.ufrn.dimap.pipeline;

import br.ufrn.dimap.pipeline.types.ProcessContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Objects;

/**
 * Executes parameter binding, {@link IBeforeInterceptor}s, endpoint invocation via {@link IInvoker},
 * and {@link IAfterInterceptor}s (including exception handlers) in a {@code try/catch/finally} flow.
 */
public class InterceptorPipeline {

    private final UniversalTranslator translator;
    private final List<IBeforeInterceptor> beforeInterceptors;
    private final List<IAfterInterceptor> afterInterceptors;
    private final IInvoker invoker;

    public InterceptorPipeline(
            UniversalTranslator translator,
            List<IBeforeInterceptor> beforeInterceptors,
            List<IAfterInterceptor> afterInterceptors,
            IInvoker invoker
    ) {
        this.translator = translator;
        this.beforeInterceptors = List.copyOf(beforeInterceptors);
        this.afterInterceptors = List.copyOf(afterInterceptors);
        this.invoker = invoker;
    }

    public void execute(ProcessContext context) {
        Objects.requireNonNull(context.endpointDescriptor, "EndpointDescriptor must not be null");

        try {
            for (Parameter parameter : context.endpointDescriptor.parameters()) {
                if (!translator.tryBind(context, parameter)) {
                    throw new IllegalArgumentException("Can't bind parameter '" + parameter.getName() + "'");
                }
            }

            for (IBeforeInterceptor interceptor : beforeInterceptors) {
                interceptor.before(context);
            }

            invoker.invoke(context);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException ite && ite.getCause() instanceof Exception cause) {
                context.exception = cause;
            } else {
                context.exception = e;
            }
        } finally {
            for (IAfterInterceptor interceptor : afterInterceptors) {
                interceptor.after(context);
            }
        }
    }
}
