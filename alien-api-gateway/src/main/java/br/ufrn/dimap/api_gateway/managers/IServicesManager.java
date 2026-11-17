package br.ufrn.dimap.api_gateway.managers;

import br.ufrn.dimap.service_discovery.dtos.GetAllServicesResponse;
import br.ufrn.dimap.service_discovery.dtos.ServiceResponse;

import java.util.UUID;

/**
 * Manages discovered service instances and provides round-robin selection per service name.
 */
public interface IServicesManager {
    void updateServices(GetAllServicesResponse response);
    ServiceResponse getNext(String serviceName);
    void removeService(String serviceName, UUID serviceId);
}
