package br.ufrn.dimap.service_discovery.dtos;

import java.util.UUID;

public record ServiceResponse(
        UUID id,
        String hostname,
        int port
) {
}
