package br.ufrn.dimap.service_discovery.dtos;

import java.util.UUID;

public record RegisterServiceResponse(
        boolean success,
        String message,
        UUID serviceId
) {
    public RegisterServiceResponse(boolean success, String message) {
        this(success, message, null);
    }
}
