package br.ufrn.dimap.pipeline.interceptors;

import br.ufrn.dimap.pipeline.IAfterInterceptor;
import br.ufrn.dimap.pipeline.types.ProcessContext;

/**
 * Executes the {@link ProcessContext#onFinish} cleanup callback after request processing completes.
 */
public class ProcessFinishInterceptor implements IAfterInterceptor {

    @Override
    public void after(ProcessContext context) {
        if (context.onFinish != null) {
            context.onFinish.run();
        }
    }
}
