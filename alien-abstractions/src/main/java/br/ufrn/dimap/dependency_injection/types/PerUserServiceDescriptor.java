package br.ufrn.dimap.dependency_injection.types;

import br.ufrn.dimap.dependency_injection.IUserInstances;

import java.time.Duration;

public final class PerUserServiceDescriptor extends ServiceDescriptor {
    public final LifecycleType lifecycle = LifecycleType.PerUser;
    public final Duration leaseDuration;
    public final boolean passivateOnIdle;
    public final IUserInstances userInstances;

    public PerUserServiceDescriptor(
            ServiceKey key,
            Class<?> implementationType,
            Duration leaseDuration,
            boolean passivateOnIdle,
            IUserInstances userInstances
    ) {
        super(key, implementationType);
        this.leaseDuration = leaseDuration;
        this.passivateOnIdle = passivateOnIdle;
        this.userInstances = userInstances;
    }

    public PerUserServiceDescriptor(
            ServiceKey key,
            Class<?> implementationType,
            IUserInstances userInstances
    ) {
        this(key, implementationType, Duration.ofMinutes(5), false, userInstances);
    }
}
