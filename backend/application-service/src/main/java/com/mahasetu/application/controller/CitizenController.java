package com.mahasetu.application.controller;

import com.mahasetu.application.dto.CitizenResponse;
import com.mahasetu.application.dto.CreateCitizenRequest;
import com.mahasetu.application.service.CitizenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/citizens")
public class CitizenController {

    private final CitizenService citizenService;

    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CitizenResponse createCitizen(@Valid @RequestBody CreateCitizenRequest request) {
        return citizenService.createCitizen(request);
    }

    @GetMapping("/{id}")
    public CitizenResponse getCitizen(@PathVariable("id") String id) {
        return citizenService.getCitizen(id);
    }
}
