package br.ufrn.dimap.dependency_injection.types;

import br.ufrn.dimap.dependency_injection.IServicePool;

public final class PerRequestServiceDescriptor extends ServiceDescriptor {
    public final LifecycleType lifecycle = LifecycleType.PerRequest;
    public final boolean pooled;
    public final Integer poolSize;
    public final IServicePool pool;

    public PerRequestServiceDescriptor(
            ServiceKey key,
            Class<?> implementationType,
            boolean pooled,
            Integer poolSize,
            IServicePool pool
    ) {
        super(key, implementationType);
        this.pooled = pooled;
        this.poolSize = poolSize;
        this.pool = pool;
    }

    public PerRequestServiceDescriptor(
            ServiceKey key,
            Class<?> implementationType
    ) {
        this(key, implementationType, false, null, null);
    }
}
