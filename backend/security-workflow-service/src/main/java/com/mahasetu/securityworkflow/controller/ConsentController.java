package com.mahasetu.securityworkflow.controller;

import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.entity.Consent;
import com.mahasetu.securityworkflow.service.ConsentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    private String extractCitizenId(Authentication authentication) {
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
            String citizenId = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (citizenId != null) return citizenId.toUpperCase();
        }
        return authentication.getName().toUpperCase();
    }

    @PostMapping("/api/v1/consents")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Consent> grantConsent(
            @RequestBody ConsentRequest request,
            Authentication authentication) {
        
        String citizenId = extractCitizenId(authentication);

        Consent consent = consentService.grantConsent(citizenId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(consent);
    }

    @PostMapping("/api/v1/consents/{id}/revoke")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Void> revokeConsent(
            @PathVariable UUID id,
            Authentication authentication) {
        
        String citizenId = extractCitizenId(authentication);
        consentService.revokeConsent(citizenId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/consents")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<List<Consent>> getConsents(Authentication authentication) {
        String citizenId = extractCitizenId(authentication);
        return ResponseEntity.ok(consentService.getConsents(citizenId));
    }

    @GetMapping("/internal/v1/consents/check")
    public ResponseEntity<Boolean> checkConsentInternal(@RequestParam String applicationId) {
        boolean hasConsent = consentService.hasGrantedConsentForApplication(applicationId);
        return ResponseEntity.ok(hasConsent);
    }
}
