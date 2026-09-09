package com.mahasetu.application.controller;

import com.mahasetu.application.dto.ApplicationResponse;
import com.mahasetu.application.dto.CreateApplicationRequest;
import com.mahasetu.application.dto.TimelineEventResponse;
import com.mahasetu.application.dto.UpdateApplicationStatusRequest;
import com.mahasetu.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CITIZEN')")
    public ApplicationResponse createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        return applicationService.createApplication(request);
    }

    @GetMapping("/{id}")
    public ApplicationResponse getApplicationDetails(@PathVariable("id") String id, org.springframework.security.core.Authentication authentication) {
        ApplicationResponse response = applicationService.getApplication(id);
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_OFFICER") || a.getAuthority().equals("ROLE_ADMIN"))) {
            response.setVerificationData(null);
        }
        return response;
    }

    @GetMapping("/{id}/status")
    public ApplicationResponse getApplicationStatus(@PathVariable("id") String id, org.springframework.security.core.Authentication authentication) {
        // According to contract, returns similar structure or just status.
        ApplicationResponse response = applicationService.getApplication(id);
        if (authentication != null && authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_OFFICER") || a.getAuthority().equals("ROLE_ADMIN"))) {
            response.setVerificationData(null);
        }
        return response;
    }

    @GetMapping("/{id}/timeline")
    public List<TimelineEventResponse> getApplicationTimeline(@PathVariable("id") String id) {
        return applicationService.getApplicationTimeline(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public ApplicationResponse updateApplicationStatus(@PathVariable("id") String id, @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return applicationService.updateApplicationStatus(id, request);
    }
}
