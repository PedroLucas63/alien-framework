package br.ufrn.dimap.pipeline.types;

import br.ufrn.dimap.dependency_injection.IServiceScope;
import br.ufrn.dimap.dependency_injection.types.UserId;
import br.ufrn.dimap.routing.types.EndpointDescriptor;
import br.ufrn.dimap.types.HttpContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessContext {
    public final HttpContext httpContext;

    public EndpointDescriptor endpointDescriptor;
    public Map<String, String> routeValues = new HashMap<>();
    public IServiceScope scope;
    public UserId userId;

    public final List<Object> methodParameters = new ArrayList<>();
    public Runnable onFinish;
    public Exception exception;

    public ProcessContext(HttpContext httpContext) {
        this.httpContext = httpContext;
    }
}
