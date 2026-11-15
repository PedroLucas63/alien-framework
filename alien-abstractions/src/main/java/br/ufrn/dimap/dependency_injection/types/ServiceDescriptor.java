package br.ufrn.dimap.dependency_injection.types;

public abstract class ServiceDescriptor {
    public final ServiceKey key;
    public final Class<?> implementationType;
    public LifecycleType lifecycle;

    ServiceDescriptor(ServiceKey key, Class<?> implementationType) {
        this.key = key;
        this.implementationType = implementationType;
    }
}

