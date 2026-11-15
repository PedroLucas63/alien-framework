package br.ufrn.dimap.dependency_injection.types;

public final class EternalServiceDescriptor extends ServiceDescriptor {
    public final LifecycleType lifecycle = LifecycleType.Eternal;
    public final boolean lazy;
    public Object singleton;

    public EternalServiceDescriptor(ServiceKey key, Class<?> implementationType, boolean lazy) {
        super(key, implementationType);
        this.lazy = lazy;
    }

    public EternalServiceDescriptor(ServiceKey key, Class<?> implementationType) {
        this(key, implementationType, true);
    }
}
