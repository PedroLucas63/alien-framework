package br.ufrn.dimap.spawning_pool;

import br.ufrn.dimap.dependency_injection.IInversionOfControl;
import br.ufrn.dimap.dependency_injection.IServiceScope;
import br.ufrn.dimap.dependency_injection.types.ServiceKey;
import br.ufrn.dimap.dependency_injection.types.UserId;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ServiceScope implements IServiceScope {
    private final IInversionOfControl container;
    private final UserId userId;
    private final Consumer<IServiceScope> onDispose;
    private final Map<ServiceKey, Object> perRequestObjects = new HashMap<>();

    private boolean disposed = false;

    public ServiceScope(IInversionOfControl container, UserId userId) {
        this(container, userId, null);
    }

    public ServiceScope(
            IInversionOfControl container,
            UserId userId,
            Consumer<IServiceScope> onDispose
    ) {
        this.container = container;
        this.userId = userId;
        this.onDispose = onDispose;
    }

    @Override
    public IInversionOfControl getContainer() {
        return container;
    }

    @Override
    public UserId getUserId() {
        return userId;
    }

    @Override
    public Object GetOrAdd(ServiceKey key, Supplier<Object> factory) {
        if (disposed) {
            throw new IllegalStateException("ServiceScope has already been disposed.");
        }

        Object existing = perRequestObjects.get(key);
        if (existing != null) {
            return existing;
        }

        Object created = factory.get();
        perRequestObjects.put(key, created);
        return created;
    }

    @Override
    public void close() {
        if (disposed) {
            return;
        }
        disposed = true;

        try {
            for (Object value : perRequestObjects.values()) {
                if (value instanceof AutoCloseable closeable) {
                    try {
                        closeable.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        } finally {
            perRequestObjects.clear();
            if (onDispose != null) {
                onDispose.accept(this);
            }
        }
    }
}
