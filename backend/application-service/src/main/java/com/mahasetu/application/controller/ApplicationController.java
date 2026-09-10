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
    private final com.mahasetu.application.integration.ConsentClient consentClient;

    public ApplicationController(ApplicationService applicationService, com.mahasetu.application.integration.ConsentClient consentClient) {
        this.applicationService = applicationService;
        this.consentClient = consentClient;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CITIZEN')")
    public ApplicationResponse createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        return applicationService.createApplication(request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CITIZEN')")
    public List<com.mahasetu.application.dto.CitizenApplicationSummaryResponse> getMyApplications(org.springframework.security.core.Authentication authentication) {
        String citizenId = authentication.getName();
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
            String claim = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (claim != null) citizenId = claim;
        }
        return applicationService.getApplicationsForCitizen(citizenId.toUpperCase());
    }

    @GetMapping("/{id}")
    public ApplicationResponse getApplicationDetails(@PathVariable("id") String id, org.springframework.security.core.Authentication authentication) {
        ApplicationResponse response = applicationService.getApplication(id);
        
        if (authentication != null) {
            boolean isOfficerOrAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OFFICER") || a.getAuthority().equals("ROLE_ADMIN"));
                
            boolean isService = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE"));
            
            if (!isOfficerOrAdmin && !isService) {
                String username = authentication.getName();
                if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
                    String claim = jwtAuth.getToken().getClaimAsString("preferred_username");
                    if (claim != null) username = claim;
                }
                
                if (username != null && !username.equalsIgnoreCase(response.getCitizenId())) {
                    throw new org.springframework.security.access.AccessDeniedException("Cannot access application belonging to another citizen");
                }
            }
            
            if (!isOfficerOrAdmin && !isService) {
                response.setVerificationData(null);
            }
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

    @GetMapping("/{id}/activity")
    public List<com.mahasetu.application.dto.CitizenApplicationActivityResponse> getApplicationActivity(@PathVariable("id") String id, org.springframework.security.core.Authentication authentication) {
        if (authentication != null) {
            boolean isOfficerOrAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OFFICER") || a.getAuthority().equals("ROLE_ADMIN"));
                
            boolean isService = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE"));
            
            if (!isOfficerOrAdmin && !isService) {
                String username = authentication.getName();
                if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
                    String claim = jwtAuth.getToken().getClaimAsString("preferred_username");
                    if (claim != null) username = claim;
                }
                
                ApplicationResponse response = applicationService.getApplication(id);
                if (username != null && !username.equalsIgnoreCase(response.getCitizenId())) {
                    throw new org.springframework.security.access.AccessDeniedException("Cannot access application belonging to another citizen");
                }
            }
        }
        return applicationService.getApplicationActivity(id);
    }

    @GetMapping("/{id}/timeline")
    public List<TimelineEventResponse> getApplicationTimeline(@PathVariable("id") String id) {
        return applicationService.getApplicationTimeline(id);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('CITIZEN')")
    public ApplicationResponse submitApplication(@PathVariable("id") String id, org.springframework.security.core.Authentication authentication) {
        String citizenId = authentication.getName();
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
            String claim = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (claim != null) citizenId = claim;
        }
        return applicationService.submitApplication(id, citizenId.toUpperCase(), consentClient);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public ApplicationResponse updateApplicationStatus(@PathVariable("id") String id, @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return applicationService.updateApplicationStatus(id, request);
    }
}
