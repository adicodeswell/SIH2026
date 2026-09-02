package com.mahasetu.application.mapper;

import com.mahasetu.application.dto.ServiceResponse;
import com.mahasetu.application.entity.Service;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public ServiceResponse toResponse(Service service) {
        if (service == null) return null;
        ServiceResponse response = new ServiceResponse();
        response.setServiceCode(service.getServiceCode());
        response.setServiceName(service.getServiceName());
        response.setDescription(service.getDescription());
        response.setActive(service.isActive());
        if (service.getDepartment() != null) {
            response.setDepartmentCode(service.getDepartment().getDepartmentCode());
            response.setDepartmentName(service.getDepartment().getName());
        }
        return response;
    }
}
