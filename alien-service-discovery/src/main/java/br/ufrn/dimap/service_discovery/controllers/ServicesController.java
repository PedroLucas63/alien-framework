package br.ufrn.dimap.service_discovery.controllers;

import br.ufrn.dimap.annotations.binding.Body;
import br.ufrn.dimap.annotations.binding.Param;
import br.ufrn.dimap.annotations.binding.Query;
import br.ufrn.dimap.annotations.routing.*;
import br.ufrn.dimap.service_discovery.dtos.*;
import br.ufrn.dimap.service_discovery.services.IServiceRegistry;

import java.util.UUID;

@Controller("services")
public class ServicesController implements IServicesController {
    private final IServiceRegistry serviceRegistry;

    public ServicesController(IServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @MapPost
    @Override
    public RegisterServiceResponse register(@Body RegisterServiceRequest request) {
        return serviceRegistry.register(request);
    }

    @MapGet
    @Override
    public GetServicesByNameResponse getByName(@Query GetServicesByNameRequest request) {
        return serviceRegistry.getByName(request);
    }

    @MapGet("all")
    @Override
    public GetAllServicesResponse getAll() {
        return serviceRegistry.getAll();
    }

    @MapDelete("{id}")
    @Override
    public boolean deregister(@Param("id") UUID id) {
        return serviceRegistry.deregister(id);
    }

    @MapPatch("{id}")
    @Override
    public boolean heartbeat(@Param("id") UUID id) {
        return serviceRegistry.heartbeat(id);
    }
}
