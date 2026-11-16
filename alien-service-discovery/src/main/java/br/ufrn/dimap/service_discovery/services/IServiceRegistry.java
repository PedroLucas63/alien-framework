package br.ufrn.dimap.service_discovery.services;

import br.ufrn.dimap.service_discovery.dtos.*;

import java.util.UUID;

public interface IServiceRegistry {
    RegisterServiceResponse register(RegisterServiceRequest request);
    GetServicesByNameResponse getByName(GetServicesByNameRequest request);
    GetAllServicesResponse getAll();
    boolean deregister(UUID id);
    boolean heartbeat(UUID id);
    void purgeExpiredServices();
}
