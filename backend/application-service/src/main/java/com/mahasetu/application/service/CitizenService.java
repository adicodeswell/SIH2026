package com.mahasetu.application.service;

import com.mahasetu.application.dto.CitizenResponse;
import com.mahasetu.application.dto.CreateCitizenRequest;
import com.mahasetu.application.entity.Citizen;
import com.mahasetu.application.exception.ResourceNotFoundException;
import com.mahasetu.application.repository.CitizenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CitizenService {

    private final CitizenRepository citizenRepository;

    public CitizenService(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    @Transactional
    public CitizenResponse registerCitizen(CreateCitizenRequest request) {
        return createCitizen(request);
    }

    @Transactional
    public CitizenResponse createCitizen(CreateCitizenRequest request) {
        if (request.getCitizenId() != null && citizenRepository.existsByCitizenId(request.getCitizenId())) {
            throw new com.mahasetu.application.exception.ValidationException("Citizen with ID already exists: " + request.getCitizenId());
        }
        if (request.getMobile() != null && citizenRepository.existsByMobile(request.getMobile())) {
            throw new com.mahasetu.application.exception.ValidationException("Citizen with mobile already exists: " + request.getMobile());
        }

        Citizen citizen = new Citizen();
        citizen.setCitizenId(request.getCitizenId());
        citizen.setName(request.getName());
        citizen.setDateOfBirth(request.getDateOfBirth());
        citizen.setMobile(request.getMobile());
        citizen.setEmail(request.getEmail());

        Citizen savedCitizen = citizenRepository.save(citizen);
        return mapToResponse(savedCitizen);
    }

    @Transactional(readOnly = true)
    public CitizenResponse getCitizen(String id) {
        Citizen citizen = citizenRepository.findByCitizenId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found with id: " + id));
        return mapToResponse(citizen);
    }

    private CitizenResponse mapToResponse(Citizen citizen) {
        CitizenResponse response = new CitizenResponse();
        response.setCitizenId(citizen.getCitizenId());
        response.setName(citizen.getName());
        response.setDateOfBirth(citizen.getDateOfBirth());
        response.setMobile(citizen.getMobile());
        response.setEmail(citizen.getEmail());
        return response;
    }
}
