package br.ufrn.dimap.service_discovery.transport;

import br.ufrn.dimap.service_discovery.dtos.GetAllServicesResponse;
import br.ufrn.dimap.service_discovery.dtos.GetServicesByNameRequest;
import br.ufrn.dimap.service_discovery.dtos.GetServicesByNameResponse;
import br.ufrn.dimap.service_discovery.dtos.RegisterServiceRequest;
import br.ufrn.dimap.service_discovery.dtos.RegisterServiceResponse;

import java.util.Optional;
import java.util.UUID;

/**
 * Transport contract for communicating with the service discovery registry.
 */
public interface IDiscoveryProtocol {
    RegisterServiceResponse registerService(RegisterServiceRequest request);
    Optional<GetServicesByNameResponse> getByServiceName(GetServicesByNameRequest request);
    Optional<GetAllServicesResponse> getAllServices();
    boolean deregisterService(UUID serviceId);
    boolean pulse(UUID serviceId);
}
