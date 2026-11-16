package br.ufrn.dimap.service_discovery.controllers;

import br.ufrn.dimap.service_discovery.dtos.*;

import java.util.UUID;

public interface IServicesController {
    RegisterServiceResponse register(RegisterServiceRequest request);
    GetServicesByNameResponse getByName(GetServicesByNameRequest request);
    GetAllServicesResponse getAll();
    boolean deregister(UUID id);
    boolean heartbeat(UUID id);
}
