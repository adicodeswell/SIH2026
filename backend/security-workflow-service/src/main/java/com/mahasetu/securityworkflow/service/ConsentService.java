package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.client.ApplicationServiceClient;
import com.mahasetu.securityworkflow.dto.ApplicationResponse;
import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.dto.DataScope;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.entity.Consent;
import com.mahasetu.securityworkflow.exception.ValidationException;
import com.mahasetu.securityworkflow.repository.ConsentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsentService {

    private static final Logger log = LoggerFactory.getLogger(ConsentService.class);

    private final ConsentRepository consentRepository;
    private final AuditService auditService;
    private final ConsentPolicyService consentPolicyService;
    private final ApplicationServiceClient applicationServiceClient;

    public ConsentService(ConsentRepository consentRepository, AuditService auditService, ConsentPolicyService consentPolicyService, ApplicationServiceClient applicationServiceClient) {
        this.consentRepository = consentRepository;
        this.auditService = auditService;
        this.consentPolicyService = consentPolicyService;
        this.applicationServiceClient = applicationServiceClient;
    }

    public Consent grantConsent(String citizenId, ConsentRequest request) {
        if (citizenId == null || citizenId.trim().isEmpty()) {
            throw new ValidationException("citizenId must not be null or blank");
        }
        if (request == null) {
            throw new ValidationException("ConsentRequest must not be null");
        }
        if (request.getApplicationId() == null || request.getApplicationId().trim().isEmpty()) {
            throw new ValidationException("applicationId must not be null or blank");
        }
        if (request.getServiceCode() == null || request.getServiceCode().trim().isEmpty()) {
            throw new ValidationException("serviceCode must not be null or blank");
        }

        // VERIFY APPLICATION CONTEXT (Issue 5)
        ApplicationResponse app = null;
        try {
            app = applicationServiceClient.getApplication(request.getApplicationId().trim());
        } catch (Exception e) {
            log.error("Failed to fetch application context for validation: {}", request.getApplicationId(), e);
            throw new ValidationException("Invalid application context");
        }
        
        if (app == null) {
            throw new ValidationException("Application not found");
        }

        if (!citizenId.equalsIgnoreCase(app.getCitizenId())) {
            log.warn("Citizen {} attempted to grant consent for application belonging to {}", citizenId, app.getCitizenId());
            throw new SecurityException("Cannot grant consent for an application you do not own");
        }

        if (!request.getServiceCode().trim().equalsIgnoreCase(app.getServiceCode())) {
            log.warn("Consent request serviceCode '{}' does not match application serviceCode '{}'", request.getServiceCode(), app.getServiceCode());
            throw new ValidationException("Consent service code does not match application context");
        }

        // BACKEND CONTROLLED POLICY RESOLUTION
        ResolvedConsentPolicy policy = consentPolicyService.getPolicy(request.getServiceCode());

        Consent consent = new Consent();
        consent.setCitizenId(citizenId.toUpperCase());
        consent.setApplicationId(request.getApplicationId().trim());
        consent.setServiceCode(request.getServiceCode().trim());
        
        consent.setRequestingDepartmentId(policy.getRequestingDepartmentId());
        
        String scopesString = policy.getRequiredScopes().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
        consent.setDataScope(scopesString);
        
        consent.setPurpose(policy.getPurpose());
        consent.setStatus("GRANTED");
        consent.setGrantedAt(LocalDateTime.now());
        consent.setExpiresAt(LocalDateTime.now().plusYears(1));

        Consent saved = consentRepository.save(consent);
        auditService.recordConsentGranted(citizenId, saved.getId(), saved.getRequestingDepartmentId(),
                saved.getDataScope(), saved.getPurpose(), saved.getApplicationId());
        return saved;
    }

    public void revokeConsent(String citizenId, UUID consentId) {
        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new IllegalArgumentException("Consent not found"));

        if (!consent.getCitizenId().equalsIgnoreCase(citizenId)) {
            throw new SecurityException("Cannot revoke consent belonging to another citizen");
        }

        boolean wasGranted = "GRANTED".equalsIgnoreCase(consent.getStatus());
        consent.setStatus("REVOKED");
        consentRepository.save(consent);

        if (wasGranted) {
            auditService.recordConsentRevoked(citizenId, consentId, consent.getApplicationId());
        }
    }

    
    public boolean hasGrantedConsentForApplication(String applicationId) {
        return consentRepository.findFirstByApplicationIdAndStatusOrderByGrantedAtDesc(applicationId, "GRANTED").isPresent();
    }

    public List<Consent> getConsents(String citizenId) {
        return consentRepository.findByCitizenId(citizenId);
    }

    /**
     * Phase 2 SECURE CONSENT VALIDATION
     */
    public boolean checkConsentContext(String citizenId, String applicationId, String serviceCode, ResolvedConsentPolicy policy) {
        Optional<Consent> consentOpt = consentRepository.findFirstByCitizenIdAndApplicationIdAndServiceCodeAndStatusOrderByGrantedAtDesc(
                citizenId.toUpperCase(), applicationId, serviceCode, "GRANTED");

        if (consentOpt.isEmpty()) {
            log.warn("Consent validation failed: No GRANTED consent found for citizen={}, app={}, service={}", citizenId, applicationId, serviceCode);
            return false;
        }

        Consent consent = consentOpt.get();

        if (consent.getExpiresAt() != null && consent.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Consent validation failed: Consent EXPIRED for app={}", applicationId);
            return false;
        }

        if (!policy.getPurpose().equals(consent.getPurpose())) {
            log.warn("Consent validation failed: Purpose mismatch. Expected {}, found {}", policy.getPurpose(), consent.getPurpose());
            return false;
        }

        if (!policy.getRequestingDepartmentId().equals(consent.getRequestingDepartmentId())) {
            log.warn("Consent validation failed: Department mismatch. Expected {}, found {}", policy.getRequestingDepartmentId(), consent.getRequestingDepartmentId());
            return false;
        }

        Set<DataScope> grantedScopes;
        try {
            grantedScopes = consent.getGrantedScopes(); // Now throws on invalid data
        } catch (IllegalStateException e) {
            log.error("Consent validation failed: Corrupted/invalid data scope persisted in DB for consent={}", consent.getId());
            return false;
        }
        Set<DataScope> requiredScopes = policy.getRequiredScopes();

        if (!grantedScopes.containsAll(requiredScopes)) {
            log.warn("Consent validation failed: Scope coverage incomplete. Required {}, Granted {}", requiredScopes, grantedScopes);
            return false;
        }

        log.info("Consent validation SUCCESS for citizen={}, app={}, service={}", citizenId, applicationId, serviceCode);
        return true;
    }
}
