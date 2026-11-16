package br.ufrn.dimap.service_discovery.dtos;

import java.util.List;

public record GetAllServicesResponse(
        List<GetServicesByNameResponse> servicesByName
) {
}
