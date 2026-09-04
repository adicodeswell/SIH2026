package com.mahasetu.application.service;

import com.mahasetu.application.dto.ServiceResponse;
import com.mahasetu.application.exception.ResourceNotFoundException;
import com.mahasetu.application.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    public ServiceCatalogService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> listActiveServices() {
        return serviceRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllActiveServices() {
        return listActiveServices();
    }

    @Transactional(readOnly = true)
    public ServiceResponse getService(String serviceCode) {
        return getServiceDetails(serviceCode);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceDetails(String serviceCode) {
        com.mahasetu.application.entity.Service service = serviceRepository.findByServiceCode(serviceCode)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with code: " + serviceCode));
        return mapToResponse(service);
    }

    private ServiceResponse mapToResponse(com.mahasetu.application.entity.Service service) {
        ServiceResponse response = new ServiceResponse();
        response.setServiceCode(service.getServiceCode());
        response.setServiceName(service.getServiceName());
        response.setDescription(service.getDescription());
        response.setActive(service.isActive());
        response.setDepartmentCode(service.getDepartment().getDepartmentCode());
        response.setDepartmentName(service.getDepartment().getName());
        return response;
    }
}
