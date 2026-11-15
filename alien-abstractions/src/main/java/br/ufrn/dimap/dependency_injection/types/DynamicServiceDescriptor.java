package br.ufrn.dimap.dependency_injection.types;

import java.util.function.Supplier;

public final class DynamicServiceDescriptor extends ServiceDescriptor {
    public final LifecycleType lifecycle = LifecycleType.Dynamic;
    public final Supplier<Object> factory;

    public DynamicServiceDescriptor(
            ServiceKey key,
            Class<?> implementationType,
            Supplier<Object> factory
    ) {
        super(key, implementationType);
        this.factory = factory;
    }
}
