package com.mahasetu.application.service;

import com.mahasetu.application.dto.ServiceResponse;
import com.mahasetu.application.entity.Department;
import com.mahasetu.application.entity.Service;
import com.mahasetu.application.exception.ResourceNotFoundException;
import com.mahasetu.application.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceCatalogServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceCatalogService serviceCatalogService;

    private Service service;

    @BeforeEach
    void setUp() {
        Department dept = new Department();
        dept.setDepartmentCode("DEPT_ED");
        dept.setName("Education Dept");

        service = new Service();
        service.setServiceCode("SCHOLARSHIP");
        service.setServiceName("Higher Ed Scholarship");
        service.setDepartment(dept);
        service.setActive(true);
    }

    @Test
    void testListActiveServices() {
        when(serviceRepository.findByActiveTrue()).thenReturn(List.of(service));

        List<ServiceResponse> res = serviceCatalogService.listActiveServices();

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals("SCHOLARSHIP", res.get(0).getServiceCode());
    }

    @Test
    void testGetService_NotFound() {
        when(serviceRepository.findByServiceCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> serviceCatalogService.getService("UNKNOWN"));
    }
}
