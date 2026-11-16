package br.ufrn.dimap.service_discovery.dtos;

import java.util.List;

public record GetServicesByNameResponse(
    String name,
    List<ServiceResponse> services
) {
}
