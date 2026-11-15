package br.ufrn.dimap.dependency_injection;

import br.ufrn.dimap.dependency_injection.types.ServiceKey;
import br.ufrn.dimap.dependency_injection.types.UserId;

import java.util.function.Supplier;

public interface IServiceScope extends AutoCloseable {
    IInversionOfControl getContainer();
    UserId getUserId();
    Object GetOrAdd(ServiceKey key, Supplier<Object> factory);
}
