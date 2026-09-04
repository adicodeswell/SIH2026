package com.mahasetu.application.controller;

import com.mahasetu.application.dto.ServiceResponse;
import com.mahasetu.application.service.ServiceCatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    public List<ServiceResponse> getAllServices() {
        return serviceCatalogService.getAllActiveServices();
    }

    @GetMapping("/{id}")
    public ServiceResponse getServiceDetails(@PathVariable("id") String id) {
        return serviceCatalogService.getServiceDetails(id);
    }
}
