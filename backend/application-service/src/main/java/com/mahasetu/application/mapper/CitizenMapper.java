package com.mahasetu.application.mapper;

import com.mahasetu.application.dto.CitizenResponse;
import com.mahasetu.application.dto.CreateCitizenRequest;
import com.mahasetu.application.entity.Citizen;
import org.springframework.stereotype.Component;

@Component
public class CitizenMapper {

    public Citizen toEntity(CreateCitizenRequest request) {
        if (request == null) return null;
        Citizen citizen = new Citizen();
        citizen.setCitizenId(request.getCitizenId());
        citizen.setName(request.getName());
        citizen.setDateOfBirth(request.getDateOfBirth());
        citizen.setMobile(request.getMobile());
        citizen.setEmail(request.getEmail());
        return citizen;
    }

    public CitizenResponse toResponse(Citizen citizen) {
        if (citizen == null) return null;
        CitizenResponse response = new CitizenResponse();
        response.setCitizenId(citizen.getCitizenId());
        response.setName(citizen.getName());
        response.setDateOfBirth(citizen.getDateOfBirth());
        response.setMobile(citizen.getMobile());
        response.setEmail(citizen.getEmail());
        return response;
    }
}
