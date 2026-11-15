package br.ufrn.dimap.spawning_pool.types;

import br.ufrn.dimap.dependency_injection.types.UserId;

import java.util.UUID;

public record SessionId(UUID id) implements UserId {
    public static final String HEADER_TITLE = "X-Session-Id";

    @Override
    public String toString() {
        return id.toString();
    }
}
