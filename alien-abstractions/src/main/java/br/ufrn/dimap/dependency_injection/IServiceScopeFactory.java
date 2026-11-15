package br.ufrn.dimap.dependency_injection;

import br.ufrn.dimap.dependency_injection.types.UserId;

public interface IServiceScopeFactory {
    IServiceScope createScope(UserId userId);
}
