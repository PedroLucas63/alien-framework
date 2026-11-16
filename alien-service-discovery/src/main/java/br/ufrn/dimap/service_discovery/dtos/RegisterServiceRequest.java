package br.ufrn.dimap.service_discovery.dtos;

public record RegisterServiceRequest(
        String name,
        String hostname,
        int port,
        String password
) {
}
