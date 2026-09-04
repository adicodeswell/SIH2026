package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.entity.Consent;
import com.mahasetu.securityworkflow.repository.ConsentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConsentService {

    private final ConsentRepository consentRepository;

    public ConsentService(ConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    public Consent grantConsent(String citizenId, ConsentRequest request) {
        Consent consent = new Consent();
        consent.setCitizenId(citizenId);
        consent.setRequestingDepartmentId(request.getRequestingDepartmentId());
        consent.setDataScope(request.getDataScope());
        consent.setPurpose(request.getPurpose());
        consent.setStatus("GRANTED");
        consent.setGrantedAt(LocalDateTime.now());
        // Default expiry to 1 year for example purposes
        consent.setExpiresAt(LocalDateTime.now().plusYears(1));

        return consentRepository.save(consent);
    }

    public void revokeConsent(String citizenId, UUID consentId) {
        Consent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new IllegalArgumentException("Consent not found"));

        if (!consent.getCitizenId().equals(citizenId)) {
            throw new SecurityException("Cannot revoke consent belonging to another citizen");
        }

        consent.setStatus("REVOKED");
        consentRepository.save(consent);
    }

    public List<Consent> getConsents(String citizenId) {
        return consentRepository.findByCitizenId(citizenId);
    }

    public boolean checkConsent(String citizenId, String dataScope, String purpose) {
        Optional<Consent> consentOpt = consentRepository.findByCitizenIdAndDataScopeAndPurposeAndStatus(
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
