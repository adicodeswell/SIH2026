package com.mahasetu.application.service;

import com.mahasetu.application.dto.CitizenResponse;
import com.mahasetu.application.dto.CreateCitizenRequest;
import com.mahasetu.application.entity.Citizen;
import com.mahasetu.application.exception.ResourceNotFoundException;
import com.mahasetu.application.exception.ValidationException;
import com.mahasetu.application.repository.CitizenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CitizenServiceTest {

    @Mock
    private CitizenRepository citizenRepository;

    @InjectMocks
    private CitizenService citizenService;

    private Citizen citizen;

    @BeforeEach
    void setUp() {
        citizen = new Citizen();
        citizen.setCitizenId("MH1001");
        citizen.setName("Rahul Sharma");
        citizen.setDateOfBirth(LocalDate.of(1995, 5, 15));
        citizen.setMobile("9876543210");
        citizen.setEmail("rahul@example.com");
    }

    @Test
    void testRegisterCitizen_Success() {
        CreateCitizenRequest req = new CreateCitizenRequest();
        req.setCitizenId("MH1001");
        req.setName("Rahul Sharma");
        req.setDateOfBirth(LocalDate.of(1995, 5, 15));
        req.setMobile("9876543210");
        req.setEmail("rahul@example.com");

        when(citizenRepository.existsByCitizenId("MH1001")).thenReturn(false);
        when(citizenRepository.existsByMobile("9876543210")).thenReturn(false);
        when(citizenRepository.save(any(Citizen.class))).thenReturn(citizen);

        CitizenResponse res = citizenService.registerCitizen(req);

        assertNotNull(res);
        assertEquals("MH1001", res.getCitizenId());
        assertEquals("Rahul Sharma", res.getName());
    }

    @Test
    void testRegisterCitizen_DuplicateId() {
        CreateCitizenRequest req = new CreateCitizenRequest();
        req.setCitizenId("MH1001");

        when(citizenRepository.existsByCitizenId("MH1001")).thenReturn(true);

        assertThrows(ValidationException.class, () -> citizenService.registerCitizen(req));
    }

    @Test
    void testGetCitizen_NotFound() {
        when(citizenRepository.findByCitizenId("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> citizenService.getCitizen("UNKNOWN"));
    }
}
