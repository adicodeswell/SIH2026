package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.entity.Consent;
import com.mahasetu.securityworkflow.exception.ValidationException;
import com.mahasetu.securityworkflow.repository.ConsentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConsentService {

    private final ConsentRepository consentRepository;
    private final AuditService auditService;

    public ConsentService(ConsentRepository consentRepository, AuditService auditService) {
        this.consentRepository = consentRepository;
        this.auditService = auditService;
    }

    public Consent grantConsent(String citizenId, ConsentRequest request) {
        if (citizenId == null || citizenId.trim().isEmpty()) {
            throw new ValidationException("citizenId must not be null or blank");
        }
        if (request == null) {
            throw new ValidationException("ConsentRequest must not be null");
        }
        if (request.getDataScope() == null || request.getDataScope().trim().isEmpty()) {
            throw new ValidationException("dataScope must not be null or blank");
        }
        if (request.getPurpose() == null || request.getPurpose().trim().isEmpty()) {
            throw new ValidationException("purpose must not be null or blank");
        }
        if (request.getRequestingDepartmentId() == null || request.getRequestingDepartmentId().trim().isEmpty()) {
            throw new ValidationException("requestingDepartmentId must not be null or blank");
        }

        Consent consent = new Consent();
        consent.setCitizenId(citizenId);
        consent.setRequestingDepartmentId(request.getRequestingDepartmentId().trim());
        consent.setDataScope(request.getDataScope().trim());
        consent.setPurpose(request.getPurpose().trim());
        consent.setStatus("GRANTED");
        consent.setGrantedAt(LocalDateTime.now());
        // Default expiry to 1 year for example purposes
        consent.setExpiresAt(LocalDateTime.now().plusYears(1));

        Consent saved = consentRepository.save(consent);
        auditService.recordConsentGranted(citizenId, saved.getId(), saved.getRequestingDepartmentId(),
                saved.getDataScope(), saved.getPurpose());
        return saved;
    }

    public void revokeConsent(String citizenId, UUID consentId) {
        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new IllegalArgumentException("Consent not found"));

        if (!consent.getCitizenId().equals(citizenId)) {
            throw new SecurityException("Cannot revoke consent belonging to another citizen");
        }

        boolean wasGranted = "GRANTED".equalsIgnoreCase(consent.getStatus());
        consent.setStatus("REVOKED");
        consentRepository.save(consent);

        if (wasGranted) {
            auditService.recordConsentRevoked(citizenId, consentId);
        }
    }

    public List<Consent> getConsents(String citizenId) {
        return consentRepository.findByCitizenId(citizenId);
    }

    public boolean checkConsent(String citizenId, String dataScope, String purpose) {
        Optional<Consent> consentOpt = consentRepository.findFirstByCitizenIdAndDataScopeAndPurposeAndStatusOrderByGrantedAtDesc(
                citizenId, dataScope, purpose, "GRANTED");

        if (consentOpt.isEmpty()) {
            return false;
        }

        Consent consent = consentOpt.get();
        if (consent.getExpiresAt() != null && consent.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }
}
