package br.ufrn.dimap.dependency_injection;

import br.ufrn.dimap.dependency_injection.types.UserId;

import java.util.function.Function;

public interface IUserInstances {
    Object GetOrAdd(UserId userId, Function<UserId, Object> factory);
    void passivate(UserId userId);
    void passivateIdle();
}
