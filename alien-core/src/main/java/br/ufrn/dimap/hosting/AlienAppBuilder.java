package br.ufrn.dimap.hosting;

import br.ufrn.dimap.annotations.dependency_injection.Lifecycle;
import br.ufrn.dimap.configuration.ConfigurationManager;
import br.ufrn.dimap.configuration.IConfiguration;
import br.ufrn.dimap.configuration.IConfigurationSource;
import br.ufrn.dimap.configuration.sources.InternalConfigurationSource;
import br.ufrn.dimap.dependency_injection.IInversionOfControl;
import br.ufrn.dimap.dependency_injection.IServiceScopeFactory;
import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.options.HttpMessageOptions;
import br.ufrn.dimap.pipeline.IAfterInterceptor;
import br.ufrn.dimap.pipeline.IBeforeInterceptor;
import br.ufrn.dimap.pipeline.IExceptionHandler;
import br.ufrn.dimap.pipeline.IMiddleware;
import br.ufrn.dimap.pipeline.IParameterBinder;
import br.ufrn.dimap.pipeline.InterceptorPipeline;
import br.ufrn.dimap.pipeline.MiddlewarePipeline;
import br.ufrn.dimap.pipeline.OvniInvoker;
import br.ufrn.dimap.pipeline.RequestDispatcher;
import br.ufrn.dimap.pipeline.UniversalTranslator;
import br.ufrn.dimap.pipeline.binders.FileBodyBinder;
import br.ufrn.dimap.pipeline.binders.HeaderBinder;
import br.ufrn.dimap.pipeline.binders.HttpContextBinder;
import br.ufrn.dimap.pipeline.binders.JsonBodyBinder;
import br.ufrn.dimap.pipeline.binders.ParamBinder;
import br.ufrn.dimap.pipeline.binders.ProcessContextBinder;
import br.ufrn.dimap.pipeline.binders.QueryBinder;
import br.ufrn.dimap.pipeline.binders.SpawningPoolBinder;
import br.ufrn.dimap.pipeline.interceptors.AddSessionIdHeaderInterceptor;
import br.ufrn.dimap.pipeline.interceptors.ExceptionInterceptor;
import br.ufrn.dimap.pipeline.interceptors.ProcessFinishInterceptor;
import br.ufrn.dimap.pipeline.middlewares.RoutingMiddleware;
import br.ufrn.dimap.pipeline.middlewares.ScopeMiddleware;
import br.ufrn.dimap.routing.IRouteMatcher;
import br.ufrn.dimap.routing.IRouteRegistry;
import br.ufrn.dimap.routing.types.EndpointDescriptor;
import br.ufrn.dimap.serializers.ByteArrayBodySerializer;
import br.ufrn.dimap.serializers.FileBodySerializer;
import br.ufrn.dimap.serializers.FormUrlEncodedBodySerializer;
import br.ufrn.dimap.serializers.IHttpBodySerializer;
import br.ufrn.dimap.serializers.JsonBodySerializer;
import br.ufrn.dimap.serializers.TextBodySerializer;
import br.ufrn.dimap.serializers.XmlBodySerializer;
import br.ufrn.dimap.spawning_pool.SpawningPoolManager;
import br.ufrn.dimap.star_map.StarMapManager;
import br.ufrn.dimap.transport.HttpBeamProtocol;
import br.ufrn.dimap.transport.IBeamDispatcher;
import br.ufrn.dimap.transport.IBeamProtocol;
import br.ufrn.dimap.types.HttpVersion;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Builder for configuring and assembling an {@link AlienApp} instance.
 */
public class AlienAppBuilder implements IAlienAppBuilder {

    private static final int DEFAULT_PORT = 8080;

    private final SpawningPoolManager spawningPoolManager;
    private final ConfigurationManager configurationManager;
    private final StarMapManager starMapManager;

    private final List<IMiddleware> middlewares = new ArrayList<>();
    private final List<IBeforeInterceptor> beforeInterceptors = new ArrayList<>();
    private final List<IAfterInterceptor> afterInterceptors = new ArrayList<>();
    private IExceptionHandler exceptionHandler;
    private final List<IParameterBinder> parameterBinders = new ArrayList<>();
    private IBeamProtocol protocol;
    private Function<IBeamDispatcher, IBeamProtocol> protocolFactory;
    private final List<Class<? extends IHostedService>> hostedServicesTypes = new ArrayList<>();
    private final List<IHttpBodySerializer> bodySerializers = new ArrayList<>();

    public AlienAppBuilder() {
        this.spawningPoolManager = new SpawningPoolManager();
        this.configurationManager = new ConfigurationManager(spawningPoolManager);
        this.starMapManager = new StarMapManager();

        spawningPoolManager.registerEternal(IInversionOfControl.class, spawningPoolManager);
        spawningPoolManager.registerEternal(IServiceScopeFactory.class, spawningPoolManager);
        spawningPoolManager.registerEternal(IConfiguration.class, configurationManager);
        spawningPoolManager.registerEternal(IRouteRegistry.class, starMapManager);
        spawningPoolManager.registerEternal(IRouteMatcher.class, starMapManager);

        addHostedServices(PassivateHosted.class);

        configurationManager.addSource(new InternalConfigurationSource());
    }

    @Override
    public IInversionOfControl getServices() {
        return spawningPoolManager;
    }

    @Override
    public IConfiguration getConfiguration() {
        return configurationManager;
    }

    @Override
    public IRouteRegistry getRoutes() {
        return starMapManager;
    }

    @Override
    public IAlienAppBuilder addConfigurationSource(IConfigurationSource source) {
        configurationManager.addSource(source);
        return this;
    }

    @Override
    public IAlienAppBuilder addModule(IAlienModule module) {
        module.configure(this);
        return this;
    }

    @Override
    public <TModule extends IAlienModule> IAlienAppBuilder addModules(Class<TModule> type) {
        try {
            TModule module = type.getDeclaredConstructor().newInstance();
            module.configure(this);
            return this;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate module: " + type.getName(), e);
        }
    }

    @Override
    public IAlienAppBuilder useMiddleware(IMiddleware middleware) {
        middlewares.add(middleware);
        return this;
    }

    @Override
    public IAlienAppBuilder addBeforeInterceptor(IBeforeInterceptor beforeInterceptor) {
        beforeInterceptors.add(beforeInterceptor);
        return this;
    }

    @Override
    public IAlienAppBuilder addAfterInterceptor(IAfterInterceptor afterInterceptor) {
        afterInterceptors.add(afterInterceptor);
        return this;
    }

    @Override
    public IAlienAppBuilder addExceptionHandler(IExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    @Override
    public IAlienAppBuilder addParameterBinder(IParameterBinder parameterBinder) {
        parameterBinders.add(parameterBinder);
        return this;
    }

    @Override
    public IAlienAppBuilder useProtocol(IBeamProtocol protocol) {
        this.protocol = protocol;
        this.protocolFactory = null;
        return this;
    }

    @Override
    public IAlienAppBuilder useProtocol(Function<IBeamDispatcher, IBeamProtocol> protocol) {
        this.protocolFactory = protocol;
        this.protocol = null;
        return this;
    }

    @Override
    public <THostedService extends IHostedService> IAlienAppBuilder addHostedServices(Class<THostedService> type) {
        spawningPoolManager.registerEternal(type);
        hostedServicesTypes.add(type);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <THostedService extends IHostedService> IAlienAppBuilder addHostedServices(THostedService service) {
        spawningPoolManager.registerEternal(service);
        hostedServicesTypes.add((Class<? extends IHostedService>) service.getClass());
        return this;
    }

    public IAlienAppBuilder addDefaultParameterBinders() {
        parameterBinders.addAll(List.of(
                new HttpContextBinder(),
                new ProcessContextBinder(),
                new SpawningPoolBinder(),
                new HeaderBinder(),
                new ParamBinder(),
                new QueryBinder(),
                new FileBodyBinder(),
                new JsonBodyBinder()
        ));
        return this;
    }

    public IAlienAppBuilder addDefaultBodySerializers() {
        bodySerializers.addAll(List.of(
                new TextBodySerializer(),
                new ByteArrayBodySerializer(),
                new FileBodySerializer(),
                new FormUrlEncodedBodySerializer(),
                new XmlBodySerializer(),
                new JsonBodySerializer()
        ));
        return this;
    }

    @Override
    public IAlienApp build() {
        ensureHttpResponseFactory();
        ensureControllersRegistered();

        return new AlienApp(
                spawningPoolManager,
                configurationManager,
                starMapManager,
                getConcreteProtocol(),
                hostedServicesTypes
        );
    }

    private void ensureHttpResponseFactory() {
        if (spawningPoolManager.contains(HttpResponseFactory.class)) {
            return;
        }
        if (bodySerializers.isEmpty()) {
            addDefaultBodySerializers();
        }

        HttpMessageOptions messageOptions = HttpMessageOptions.builder()
                .withVersion(HttpVersion.Http1_1)
                .withBodySerializers(bodySerializers)
                .build();

        HttpResponseFactory responseFactory = new HttpResponseFactory(messageOptions);
        spawningPoolManager.registerEternal(responseFactory);
    }

    private void ensureControllersRegistered() {
        for (EndpointDescriptor endpoint : starMapManager.getEndpoints()) {
            Class<?> controllerType = endpoint.controllerType();
            if (Modifier.isAbstract(controllerType.getModifiers())
                    || controllerType.isInterface()
                    || spawningPoolManager.contains(controllerType)) {
                continue;
            }

            if (hasLifecycleAnnotation(controllerType)) {
                spawningPoolManager.register(controllerType);
            } else {
                spawningPoolManager.registerPerRequest(controllerType);
            }
        }
    }

    private static boolean hasLifecycleAnnotation(Class<?> type) {
        if (type.isAnnotationPresent(Lifecycle.class)) {
            return true;
        }
        for (Annotation annotation : type.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Lifecycle.class)) {
                return true;
            }
        }
        return false;
    }

    private IBeamProtocol getConcreteProtocol() {
        IBeamDispatcher beamDispatcher = getConcreteDispatcher();

        if (protocol != null) {
            return protocol;
        }
        if (protocolFactory != null) {
            return protocolFactory.apply(beamDispatcher);
        }

        int port = configurationManager.tryGet("PORT")
                .map(value -> Integer.parseInt(value.trim()))
                .orElse(DEFAULT_PORT);

        return new HttpBeamProtocol(beamDispatcher, port);
    }

    private IBeamDispatcher getConcreteDispatcher() {
        if (spawningPoolManager.contains(IBeamDispatcher.class)) {
            return spawningPoolManager.spawn(IBeamDispatcher.class);
        }

        HttpResponseFactory responseFactory = spawningPoolManager.spawn(HttpResponseFactory.class);

        List<IMiddleware> pipeline = new ArrayList<>();
        pipeline.add(new ScopeMiddleware(spawningPoolManager));
        pipeline.addAll(middlewares);
        pipeline.add(new RoutingMiddleware(starMapManager, responseFactory));
        MiddlewarePipeline middlewarePipeline = new MiddlewarePipeline(pipeline);

        List<IAfterInterceptor> allAfterInterceptors = new ArrayList<>(afterInterceptors);
        allAfterInterceptors.add(exceptionHandler != null
                ? exceptionHandler
                : new ExceptionInterceptor(responseFactory));
        allAfterInterceptors.add(new AddSessionIdHeaderInterceptor());
        allAfterInterceptors.add(new ProcessFinishInterceptor());

        UniversalTranslator translator = getUniversalTranslator();
        OvniInvoker invoker = new OvniInvoker(spawningPoolManager, responseFactory);
        InterceptorPipeline interceptorPipeline = new InterceptorPipeline(
                translator,
                beforeInterceptors,
                allAfterInterceptors,
                invoker
        );

        RequestDispatcher dispatcher = new RequestDispatcher(middlewarePipeline, interceptorPipeline, responseFactory);
        spawningPoolManager.registerEternal(IBeamDispatcher.class, dispatcher);
        return dispatcher;
    }

    private UniversalTranslator getUniversalTranslator() {
        if (parameterBinders.isEmpty()) {
            addDefaultParameterBinders();
        }
        return new UniversalTranslator(parameterBinders);
    }
}
