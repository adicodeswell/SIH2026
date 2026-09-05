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

    @GetMapping("/internal/v1/consents/check")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<Void> checkConsent(
            @RequestParam String citizenId,
            @RequestParam String dataScope,
            @RequestParam String purpose) {
        
        boolean isValid = consentService.checkConsent(citizenId, dataScope, purpose);
        if (isValid) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/api/v1/consents")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Consent> grantConsent(
            @RequestBody ConsentRequest request,
            Authentication authentication) {
        
        String citizenId = authentication.getName();
        Consent consent = consentService.grantConsent(citizenId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(consent);
    }

    @PostMapping("/api/v1/consents/{id}/revoke")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<Void> revokeConsent(
            @PathVariable UUID id,
            Authentication authentication) {
        
        String citizenId = authentication.getName();
        consentService.revokeConsent(citizenId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/consents")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<List<Consent>> getConsents(Authentication authentication) {
        String citizenId = authentication.getName();
        return ResponseEntity.ok(consentService.getConsents(citizenId));
    }
}
