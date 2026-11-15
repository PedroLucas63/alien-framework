package br.ufrn.dimap.spawning_pool;

import br.ufrn.dimap.dependency_injection.IUserInstances;
import br.ufrn.dimap.dependency_injection.types.UserId;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class UserInstanceStore implements IUserInstances {
    private static final class InstanceInfo {
        final Object instance;
        volatile Instant timestamp;

        InstanceInfo(Object instance, Instant timestamp) {
            this.instance = instance;
            this.timestamp = timestamp;
        }
    }

    private final boolean passivateOnIdle;
    private final Duration timeout;
    private final Map<UserId, InstanceInfo> storage = new ConcurrentHashMap<>();

    public UserInstanceStore(boolean passivateOnIdle, Duration timeout) {
        this.passivateOnIdle = passivateOnIdle;
        this.timeout = timeout;
    }

    public boolean isPassivateOnIdle() {
        return passivateOnIdle;
    }

    public Duration getTimeout() {
        return timeout;
    }

    @Override
    public Object GetOrAdd(UserId userId, Function<UserId, Object> factory) {
        InstanceInfo info = storage.computeIfAbsent(
                userId,
                id -> new InstanceInfo(factory.apply(id), Instant.now())
        );
        info.timestamp = Instant.now();
        return info.instance;
    }

    @Override
    public void passivate(UserId userId) {
        InstanceInfo info = storage.remove(userId);
        if (info == null) {
            return;
        }

        if (info.instance instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void passivateIdle() {
        if (!passivateOnIdle) {
            return;
        }

        Instant now = Instant.now();
        for (var entry : storage.entrySet()) {
            Duration elapsed = Duration.between(entry.getValue().timestamp, now);
            if (elapsed.compareTo(timeout) > 0) {
                passivate(entry.getKey());
            }
        }
    }
}
