package br.ufrn.dimap.spawning_pool;

import br.ufrn.dimap.annotations.configuration.ConfigurationProperties;
import br.ufrn.dimap.annotations.dependency_injection.Component;
import br.ufrn.dimap.annotations.dependency_injection.Eternal;
import br.ufrn.dimap.annotations.dependency_injection.Lifecycle;
import br.ufrn.dimap.annotations.dependency_injection.PerRequest;
import br.ufrn.dimap.annotations.dependency_injection.PerUser;
import br.ufrn.dimap.annotations.dependency_injection.ServiceConstructor;
import br.ufrn.dimap.configuration.IConfiguration;
import br.ufrn.dimap.configuration.IOptions;
import br.ufrn.dimap.configuration.OptionsWrapper;
import br.ufrn.dimap.dependency_injection.IInversionOfControl;
import br.ufrn.dimap.dependency_injection.IServicePool;
import br.ufrn.dimap.dependency_injection.IServiceScope;
import br.ufrn.dimap.dependency_injection.IServiceScopeFactory;
import br.ufrn.dimap.dependency_injection.types.DynamicServiceDescriptor;
import br.ufrn.dimap.dependency_injection.types.EternalServiceDescriptor;
import br.ufrn.dimap.dependency_injection.types.LifecycleType;
import br.ufrn.dimap.dependency_injection.types.PerRequestServiceDescriptor;
import br.ufrn.dimap.dependency_injection.types.PerUserServiceDescriptor;
import br.ufrn.dimap.dependency_injection.types.ServiceDescriptor;
import br.ufrn.dimap.dependency_injection.types.ServiceKey;
import br.ufrn.dimap.dependency_injection.types.UserId;
import br.ufrn.dimap.discovery.AssemblyDiscovery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class SpawningPoolManager implements IInversionOfControl, IServiceScopeFactory {
    private final Map<ServiceKey, ServiceDescriptor> serviceDescriptors = new ConcurrentHashMap<>();
    private final Map<ServiceKey, Supplier<Object>> optionsFactories = new ConcurrentHashMap<>();
    private final ThreadLocal<IServiceScope> localScope = new ThreadLocal<>();
    private final List<Runnable> passivateCallbacks = new CopyOnWriteArrayList<>();

    private final Lock eternalLock = new ReentrantLock();
    private final Lock poolLock = new ReentrantLock();

    @Override
    public void autoRegister() {
        for (Class<?> type : AssemblyDiscovery.getClasses()) {
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                continue;
            }
            if (findLifecycleAnnotation(type) == null) {
                continue;
            }
            register(type);
        }
    }

    @Override
    public void passivateIdleUsers() {
        for (Runnable callback : passivateCallbacks) {
            callback.run();
        }
    }

    @Override
    public boolean contains(Class<?> type, String name) {
        return serviceDescriptors.containsKey(new ServiceKey(type, normalizeName(name)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T spawn(Class<T> type, String name) {
        ServiceKey serviceKey = new ServiceKey(type, normalizeName(name));
        ServiceDescriptor descriptor = serviceDescriptors.get(serviceKey);
        if (descriptor == null) {
            throw new NoSuchElementException("No service descriptor found for " + serviceKey);
        }

        return (T) switch (descriptor) {
            case EternalServiceDescriptor eternal -> spawnEternal(eternal);
            case PerRequestServiceDescriptor perRequest -> spawnPerRequest(perRequest);
            case PerUserServiceDescriptor perUser -> spawnPerUser(perUser);
            case DynamicServiceDescriptor dynamic -> dynamic.factory.get();
            default -> throw new IllegalStateException("Lifecycle type not supported: " + descriptor.getClass().getName());
        };
    }

    private Object spawnEternal(EternalServiceDescriptor descriptor) {
        if (descriptor.singleton != null) {
            return descriptor.singleton;
        }

        eternalLock.lock();
        try {
            if (descriptor.singleton == null) {
                descriptor.singleton = createSpecimen(descriptor.key);
            }
            return descriptor.singleton;
        } finally {
            eternalLock.unlock();
        }
    }

    private Object spawnPerRequest(PerRequestServiceDescriptor descriptor) {
        if (descriptor.pooled) {
            return spawnPooledPerRequest(descriptor);
        }

        IServiceScope scope = localScope.get();
        if (scope == null) {
            throw new IllegalStateException("No local scope has been registered");
        }

        return scope.GetOrAdd(descriptor.key, () -> createSpecimen(descriptor.key));
    }

    private Object spawnPooledPerRequest(PerRequestServiceDescriptor descriptor) {
        IServicePool pool = descriptor.pool;
        if (pool == null) {
            throw new IllegalStateException("No pool has been registered");
        }

        poolLock.lock();
        try {
            if (pool.isFull()) {
                return pool.Get();
            }
            Object specimen = createSpecimen(descriptor.key);
            return pool.Register(specimen);
        } finally {
            poolLock.unlock();
        }
    }

    private Object spawnPerUser(PerUserServiceDescriptor descriptor) {
        if (descriptor.userInstances == null) {
            throw new IllegalStateException("No user instances store has been registered");
        }
        IServiceScope scope = localScope.get();
        UserId userId = scope != null ? scope.getUserId() : null;
        if (userId == null) {
            throw new IllegalStateException("No user id available");
        }

        return descriptor.userInstances.GetOrAdd(userId, id -> createSpecimen(descriptor.key));
    }

    private Object createSpecimen(ServiceKey serviceKey) {
        Class<?> specimenType = serviceKey.type();

        Constructor<?> constructor = Arrays.stream(specimenType.getConstructors())
                .min(Comparator
                        .comparing((Constructor<?> c) -> !c.isAnnotationPresent(ServiceConstructor.class))
                        .thenComparingInt(Constructor::getParameterCount))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Specimen " + specimenType.getName() + " must have at least one public constructor."
                ));

        Parameter[] parameters = constructor.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> paramType = parameter.getType();

            if (paramType.isAssignableFrom(specimenType)) {
                throw new IllegalStateException("Specimens cannot have parameters of type " + specimenType.getName());
            }

            Component component = parameter.getAnnotation(Component.class);
            String name = (component != null && !component.value().isBlank()) ? component.value() : null;

            if (paramType == IOptions.class && parameter.getParameterizedType() instanceof ParameterizedType pt) {
                var typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length == 1 && typeArgs[0] instanceof Class<?> optionsClass) {
                    Supplier<Object> factory = optionsFactories.get(new ServiceKey(optionsClass, name));
                    if (factory != null) {
                        arguments[i] = factory.get();
                        continue;
                    }
                }
            }

            arguments[i] = spawn(paramType, name);
        }

        try {
            return constructor.newInstance(arguments);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate specimen " + specimenType.getName(), e);
        }
    }

    // RegisterConfig
    @Override
    public <TInstance> void registerConfig(TInstance instance, String name) {
        registerEternal(instance, name);
    }

    @Override
    public <TInterface, TInstance extends TInterface> void registerConfig(
            Class<TInterface> iType, TInstance instance, String name
    ) {
        registerEternal(iType, instance, name);
    }

    @Override
    public void registerConfig(Class<?> type, IConfiguration configuration, String prefix, String name) {
        ensureConstructible(type);
        String resolvedPrefix = resolvePrefix(type, prefix);
        String normalizedName = normalizeName(name);

        ServiceKey instanceKey = new ServiceKey(type, normalizedName);
        var eternalDescriptor = new EternalServiceDescriptor(instanceKey, type, false);
        eternalDescriptor.singleton = configuration.bind(type, resolvedPrefix);
        serviceDescriptors.put(instanceKey, eternalDescriptor);

        registerOptionsDescriptor(type, type, configuration, resolvedPrefix, normalizedName);
    }

    @Override
    public <TBase, TInstance extends TBase> void registerConfig(
            Class<TBase> baseType,
            Class<TInstance> type,
            IConfiguration configuration,
            String prefix,
            String name
    ) {
        ensureConstructible(type);
        if (!baseType.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Type " + baseType.getName() + " is not assignable from " + type.getName());
        }

        String resolvedPrefix = resolvePrefix(type, prefix);
        String normalizedName = normalizeName(name);

        ServiceKey baseKey = new ServiceKey(baseType, normalizedName);
        ServiceKey instanceKey = new ServiceKey(type, normalizedName);
        var eternalDescriptor = new EternalServiceDescriptor(instanceKey, type, false);
        eternalDescriptor.singleton = configuration.bind(type, resolvedPrefix);

        serviceDescriptors.put(baseKey, eternalDescriptor);
        serviceDescriptors.put(instanceKey, eternalDescriptor);

        registerOptionsDescriptor(baseType, type, configuration, resolvedPrefix, normalizedName);
        if (!baseType.equals(type)) {
            registerOptionsDescriptor(type, type, configuration, resolvedPrefix, normalizedName);
        }
    }

    private void registerOptionsDescriptor(
            Class<?> optionsType,
            Class<?> bindType,
            IConfiguration configuration,
            String prefix,
            String name
    ) {
        optionsFactories.put(new ServiceKey(optionsType, name), () -> {
            configuration.reload();
            Object freshConfig = configuration.bind(bindType, prefix);
            return new OptionsWrapper<>(freshConfig);
        });
    }

    // RegisterEternal
    @Override
    public <TInstance> void registerEternal(TInstance instance, String name) {
        Class<?> instanceType = instance.getClass();
        ServiceKey instanceKey = new ServiceKey(instanceType, normalizeName(name));
        var descriptor = new EternalServiceDescriptor(instanceKey, instanceType, false);
        descriptor.singleton = instance;
        serviceDescriptors.put(instanceKey, descriptor);
    }

    @Override
    public <TInterface, TInstance extends TInterface> void registerEternal(
            Class<TInterface> iType, TInstance instance, String name
    ) {
        String normalizedName = normalizeName(name);
        Class<?> instanceType = instance.getClass();
        ServiceKey interfaceKey = new ServiceKey(iType, normalizedName);
        ServiceKey instanceKey = new ServiceKey(instanceType, normalizedName);

        var descriptor = new EternalServiceDescriptor(instanceKey, instanceType, false);
        descriptor.singleton = instance;

        serviceDescriptors.put(interfaceKey, descriptor);
        serviceDescriptors.put(instanceKey, descriptor);
    }

    @Override
    public void registerEternal(Class<?> type, String name) {
        register(type, name, LifecycleType.Eternal);
    }

    @Override
    public <TInterface, TInstance extends TInterface> void registerEternal(
            Class<TInterface> iType, Class<TInstance> type, String name
    ) {
        register(iType, type, name, LifecycleType.Eternal);
    }

    // RegisterPerRequest
    @Override
    public void registerPerRequest(Class<?> type, String name) {
        register(type, name, LifecycleType.PerRequest);
    }

    @Override
    public <TInterface, TInstance extends TInterface> void registerPerRequest(
            Class<TInterface> iType, Class<TInstance> type, String name
    ) {
        register(iType, type, name, LifecycleType.PerRequest);
    }

    // RegisterPerUser
    @Override
    public void registerPerUser(Class<?> type, String name) {
        register(type, name, LifecycleType.PerUser);
    }

    @Override
    public <TInterface, TInstance extends TInterface> void registerPerUser(
            Class<TInterface> iType, Class<TInstance> type, String name
    ) {
        register(iType, type, name, LifecycleType.PerUser);
    }

    // Register
    @Override
    public void register(Class<?> type) {
        ensureConstructible(type);
        Annotation lifecycleAnn = findLifecycleAnnotation(type);
        if (lifecycleAnn == null) {
            throw new IllegalArgumentException("Type " + type.getName() + " does not have a @Lifecycle annotation.");
        }

        String name = normalizeName(extractName(lifecycleAnn));
        ServiceKey instanceKey = new ServiceKey(type, name);
        ServiceDescriptor descriptor = createServiceDescriptor(instanceKey, lifecycleAnn);
        serviceDescriptors.put(instanceKey, descriptor);
        postRegisterService(instanceKey, descriptor);
    }

    @Override
    public <TInterface, TInstance extends TInterface> void register(Class<TInterface> iType, Class<TInstance> type) {
        ensureConstructible(type);
        if (!iType.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Type " + iType.getName() + " is not assignable from " + type.getName());
        }

        Annotation lifecycleAnn = findLifecycleAnnotation(type);
        if (lifecycleAnn == null) {
            throw new IllegalArgumentException("Type " + type.getName() + " does not have a @Lifecycle annotation.");
        }

        String name = normalizeName(extractName(lifecycleAnn));
        ServiceKey interfaceKey = new ServiceKey(iType, name);
        ServiceKey instanceKey = new ServiceKey(type, name);
        ServiceDescriptor descriptor = createServiceDescriptor(instanceKey, lifecycleAnn);

        serviceDescriptors.put(interfaceKey, descriptor);
        serviceDescriptors.put(instanceKey, descriptor);
        postRegisterService(instanceKey, descriptor);
    }

    @Override
    public void register(Class<?> type, String name, LifecycleType lifecycleType) {
        ensureConstructible(type);
        String normalizedName = normalizeName(name);
        ServiceKey instanceKey = new ServiceKey(type, normalizedName);

        Annotation lifecycleAnn = findLifecycleAnnotation(type);
        if (lifecycleAnn != null && getLifecycleType(lifecycleAnn) != lifecycleType) {
            throw new IllegalArgumentException("Type " + type.getName() + " has an incorrect lifecycle annotation.");
        }

        ServiceDescriptor descriptor = lifecycleAnn != null
                ? createServiceDescriptor(instanceKey, lifecycleAnn)
                : createDefaultDescriptor(instanceKey, lifecycleType);

        serviceDescriptors.put(instanceKey, descriptor);
        postRegisterService(instanceKey, descriptor);
    }

    @Override
    public <TInterface, TInstance extends TInterface> void register(
            Class<TInterface> iType, Class<TInstance> type, String name, LifecycleType lifecycleType
    ) {
        ensureConstructible(type);
        if (!iType.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Type " + iType.getName() + " is not assignable from " + type.getName());
        }

        String normalizedName = normalizeName(name);
        ServiceKey interfaceKey = new ServiceKey(iType, normalizedName);
        ServiceKey instanceKey = new ServiceKey(type, normalizedName);

        Annotation lifecycleAnn = findLifecycleAnnotation(type);
        if (lifecycleAnn != null && getLifecycleType(lifecycleAnn) != lifecycleType) {
            throw new IllegalArgumentException("Type " + type.getName() + " has an incorrect lifecycle annotation.");
        }

        ServiceDescriptor descriptor = lifecycleAnn != null
                ? createServiceDescriptor(instanceKey, lifecycleAnn)
                : createDefaultDescriptor(instanceKey, lifecycleType);

        serviceDescriptors.put(interfaceKey, descriptor);
        serviceDescriptors.put(instanceKey, descriptor);
        postRegisterService(instanceKey, descriptor);
    }

    @Override
    public IServiceScope createScope(UserId userId) {
        var scope = new ServiceScope(this, userId, disposedScope -> {
            if (localScope.get() == disposedScope) {
                localScope.remove();
            }
        });
        localScope.set(scope);
        return scope;
    }

    private static void ensureConstructible(Class<?> type) {
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException(
                    "Cannot register non-constructible type " + type.getName() + ". It must be a concrete class."
            );
        }
    }

    private static String resolvePrefix(Class<?> type, String prefix) {
        if (prefix != null) {
            return prefix;
        }
        ConfigurationProperties cp = type.getAnnotation(ConfigurationProperties.class);
        return (cp != null && !cp.prefix().isBlank()) ? cp.prefix() : null;
    }

    private static String normalizeName(String name) {
        return (name == null || name.isBlank()) ? null : name;
    }

    private static Annotation findLifecycleAnnotation(Class<?> type) {
        for (Annotation ann : type.getAnnotations()) {
            if (ann.annotationType().isAnnotationPresent(Lifecycle.class)) {
                return ann;
            }
        }
        return null;
    }

    private static LifecycleType getLifecycleType(Annotation lifecycleAnn) {
        return lifecycleAnn.annotationType().getAnnotation(Lifecycle.class).value();
    }

    private static String extractName(Annotation lifecycleAnn) {
        return switch (lifecycleAnn) {
            case Eternal e -> e.name();
            case PerRequest pr -> pr.name();
            case PerUser pu -> pu.name();
            default -> null;
        };
    }

    private ServiceDescriptor createServiceDescriptor(ServiceKey instanceKey, Annotation lifecycleAnn) {
        return switch (lifecycleAnn) {
            case Eternal e -> new EternalServiceDescriptor(instanceKey, instanceKey.type(), e.lazy());
            case PerRequest pr -> new PerRequestServiceDescriptor(
                    instanceKey,
                    instanceKey.type(),
                    pr.withPool(),
                    pr.poolSize(),
                    pr.withPool() ? new InstancePool(pr.poolSize()) : null
            );
            case PerUser pu -> {
                Duration lease = pu.leaseDurationMicroseconds() == 0
                        ? Duration.ofMinutes(5)
                        : Duration.of(pu.leaseDurationMicroseconds(), ChronoUnit.MICROS);
                var store = new UserInstanceStore(pu.passivateOnIdle(), lease);
                if (pu.passivateOnIdle()) {
                    passivateCallbacks.add(store::passivateIdle);
                }
                yield new PerUserServiceDescriptor(instanceKey, instanceKey.type(), lease, pu.passivateOnIdle(), store);
            }
            default -> throw new IllegalStateException("Unsupported lifecycle annotation: " + lifecycleAnn);
        };
    }

    private ServiceDescriptor createDefaultDescriptor(ServiceKey instanceKey, LifecycleType lifecycleType) {
        return switch (lifecycleType) {
            case Eternal -> new EternalServiceDescriptor(instanceKey, instanceKey.type());
            case PerRequest -> new PerRequestServiceDescriptor(instanceKey, instanceKey.type());
            case PerUser -> {
                Duration lease = Duration.ofMinutes(5);
                var store = new UserInstanceStore(true, lease);
                passivateCallbacks.add(store::passivateIdle);
                yield new PerUserServiceDescriptor(instanceKey, instanceKey.type(), lease, true, store);
            }
            case Dynamic -> throw new IllegalArgumentException("Dynamic lifecycle requires a factory.");
        };
    }

    private void postRegisterService(ServiceKey instanceKey, ServiceDescriptor descriptor) {
        if (descriptor instanceof EternalServiceDescriptor eternal && !eternal.lazy) {
            eternal.singleton = createSpecimen(instanceKey);
        }
    }
}
