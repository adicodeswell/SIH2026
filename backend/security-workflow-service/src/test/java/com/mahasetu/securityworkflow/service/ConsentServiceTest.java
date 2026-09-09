package com.mahasetu.securityworkflow.service;

import com.mahasetu.securityworkflow.client.ApplicationServiceClient;
import com.mahasetu.securityworkflow.dto.ApplicationResponse;
import com.mahasetu.securityworkflow.dto.ConsentRequest;
import com.mahasetu.securityworkflow.dto.DataScope;
import com.mahasetu.securityworkflow.dto.ResolvedConsentPolicy;
import com.mahasetu.securityworkflow.entity.Consent;
import com.mahasetu.securityworkflow.exception.ValidationException;
import com.mahasetu.securityworkflow.repository.ConsentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConsentServiceTest {

    private ConsentRepository consentRepository;
    private AuditService auditService;
    private ConsentPolicyService consentPolicyService;
    private ApplicationServiceClient applicationServiceClient;
    private ConsentService consentService;

    @BeforeEach
    void setUp() {
        consentRepository = mock(ConsentRepository.class);
        auditService = mock(AuditService.class);
        consentPolicyService = mock(ConsentPolicyService.class);
        applicationServiceClient = mock(ApplicationServiceClient.class);
        consentService = new ConsentService(consentRepository, auditService, consentPolicyService, applicationServiceClient);
    }

    private Consent createMockConsent(String scopes, String purpose, String dept, LocalDateTime expiresAt) {
        Consent c = new Consent();
        c.setCitizenId("USER-1");
        c.setApplicationId("A001");
        c.setServiceCode("TEST-SVC");
        c.setDataScope(scopes);
        c.setPurpose(purpose);
        c.setRequestingDepartmentId(dept);
        c.setStatus("GRANTED");
        c.setGrantedAt(LocalDateTime.now().minusDays(1));
        c.setExpiresAt(expiresAt != null ? expiresAt : LocalDateTime.now().plusDays(1));
        return c;
    }

    private ResolvedConsentPolicy createMockPolicy(Set<DataScope> requiredScopes, String purpose, String serviceCode, String dept) {
        return new ResolvedConsentPolicy(serviceCode, requiredScopes, purpose, "dummy-raw", dept);
    }

    @Test
    void testCheckConsentContext_FullCoverage_Valid() {
        Consent consent = createMockConsent("EDUCATION,EMPLOYMENT,SKILLS", "verification", "DEPT-GENERIC", null);
        when(consentRepository.findFirstByCitizenIdAndApplicationIdAndServiceCodeAndStatusOrderByGrantedAtDesc(
                "USER-1", "A001", "TEST-SVC", "GRANTED")).thenReturn(Optional.of(consent));
        
        ResolvedConsentPolicy policy = createMockPolicy(Set.of(DataScope.EDUCATION, DataScope.EMPLOYMENT, DataScope.SKILLS), "verification", "TEST-SVC", "DEPT-GENERIC");
        
        boolean valid = consentService.checkConsentContext("user-1", "A001", "TEST-SVC", policy);
        assertTrue(valid);
    }

    @Test
    void testCheckConsentContext_PartialCoverage_Invalid() {
        Consent consent = createMockConsent("EDUCATION,EMPLOYMENT", "verification", "DEPT-GENERIC", null);
        when(consentRepository.findFirstByCitizenIdAndApplicationIdAndServiceCodeAndStatusOrderByGrantedAtDesc(
                "USER-1", "A001", "TEST-SVC", "GRANTED")).thenReturn(Optional.of(consent));
        
        ResolvedConsentPolicy policy = createMockPolicy(Set.of(DataScope.EDUCATION, DataScope.EMPLOYMENT, DataScope.SKILLS), "verification", "TEST-SVC", "DEPT-GENERIC");
        
        boolean valid = consentService.checkConsentContext("user-1", "A001", "TEST-SVC", policy);
        assertFalse(valid, "Partial coverage should be invalid");
    }

    @Test
    void testCheckConsentContext_ExtraScope_Valid() {
        Consent consent = createMockConsent("EDUCATION,EMPLOYMENT,SKILLS,HEALTH", "verification", "DEPT-GENERIC", null);
        when(consentRepository.findFirstByCitizenIdAndApplicationIdAndServiceCodeAndStatusOrderByGrantedAtDesc(
                "USER-1", "A001", "TEST-SVC", "GRANTED")).thenReturn(Optional.of(consent));
        
        ResolvedConsentPolicy policy = createMockPolicy(Set.of(DataScope.EDUCATION, DataScope.EMPLOYMENT), "verification", "TEST-SVC", "DEPT-GENERIC");
        
        boolean valid = consentService.checkConsentContext("user-1", "A001", "TEST-SVC", policy);
        assertTrue(valid, "Extra scope should be valid if requirements are covered");
    }

    @Test
    void testCheckConsentContext_WrongPurpose_Invalid() {
        Consent consent = createMockConsent("EDUCATION", "scholarship_verification", "DEPT-GENERIC", null);
        when(consentRepository.findFirstByCitizenIdAndApplicationIdAndServiceCodeAndStatusOrderByGrantedAtDesc(
                "USER-1", "A001", "TEST-SVC", "GRANTED")).thenReturn(Optional.of(consent));
        
        ResolvedConsentPolicy policy = createMockPolicy(Set.of(DataScope.EDUCATION), "verification", "TEST-SVC", "DEPT-GENERIC");
        
        boolean valid = consentService.checkConsentContext("user-1", "A001", "TEST-SVC", policy);
        assertFalse(valid, "Wrong purpose must be rejected");
    }

    @Test
    void testCheckConsentContext_WrongDepartment_Invalid() {
        Consent consent = createMockConsent("EDUCATION", "verification", "DEPT-WRONG", null);
        when(consentRepository.findFirstByCitizenIdAndApplicationIdAndServiceCodeAndStatusOrderByGrantedAtDesc(
                "USER-1", "A001", "TEST-SVC", "GRANTED")).thenReturn(Optional.of(consent));
        
        ResolvedConsentPolicy policy = createMockPolicy(Set.of(DataScope.EDUCATION), "verification", "TEST-SVC", "DEPT-GENERIC");
        
        boolean valid = consentService.checkConsentContext("user-1", "A001", "TEST-SVC", policy);
        assertFalse(valid, "Wrong department must be rejected");
    }

    @Test
    void testCheckConsentContext_Expired_Invalid() {
        Consent consent = createMockConsent("EDUCATION", "verification", "DEPT-GENERIC", LocalDateTime.now().minusMinutes(5));
        when(consentRepository.findFirstByCitizenIdAndApplicationIdAndServiceCodeAndStatusOrderByGrantedAtDesc(
                "USER-1", "A001", "TEST-SVC", "GRANTED")).thenReturn(Optional.of(consent));
        
        ResolvedConsentPolicy policy = createMockPolicy(Set.of(DataScope.EDUCATION), "verification", "TEST-SVC", "DEPT-GENERIC");
        
        boolean valid = consentService.checkConsentContext("user-1", "A001", "TEST-SVC", policy);
        assertFalse(valid, "Expired consent must be invalid");
    }

    @Test
    void testCheckConsentContext_WrongApplication_Invalid() {
        when(consentRepository.findFirstByCitizenIdAndApplicationIdAndServiceCodeAndStatusOrderByGrantedAtDesc(
                "USER-1", "A002", "TEST-SVC", "GRANTED")).thenReturn(Optional.empty());
        
        ResolvedConsentPolicy policy = createMockPolicy(Set.of(DataScope.EDUCATION), "verification", "TEST-SVC", "DEPT-GENERIC");
        
        boolean valid = consentService.checkConsentContext("user-1", "A002", "TEST-SVC", policy);
        assertFalse(valid, "Consent for one application cannot authorize another");
    }

    @Test
    void testCheckConsentContext_InvalidPersistedScope_Invalid() {
        Consent consent = createMockConsent("EDUCATION,EMPLOYMENT,EVERYTHING", "verification", "DEPT-GENERIC", null);
        when(consentRepository.findFirstByCitizenIdAndApplicationIdAndServiceCodeAndStatusOrderByGrantedAtDesc(
                "USER-1", "A001", "TEST-SVC", "GRANTED")).thenReturn(Optional.of(consent));
        
        ResolvedConsentPolicy policy = createMockPolicy(Set.of(DataScope.EDUCATION, DataScope.EMPLOYMENT), "verification", "TEST-SVC", "DEPT-GENERIC");
        
        boolean valid = consentService.checkConsentContext("user-1", "A001", "TEST-SVC", policy);
        assertFalse(valid, "Invalid persisted data scope must fail securely");
    }

    @Test
    void testGrantConsent_ClientCannotOverridePolicyAndValidationWorks() {
        ConsentRequest request = new ConsentRequest();
        request.setApplicationId("A001");
        request.setServiceCode("SKILL_BENEFIT");
        request.setDataScope("HEALTH");
        request.setPurpose("random_purpose");
        request.setRequestingDepartmentId("random_dept");
        
        ApplicationResponse mockApp = new ApplicationResponse();
        mockApp.setApplicationNumber("A001");
        mockApp.setCitizenId("user-1");
        mockApp.setServiceCode("SKILL_BENEFIT");
        when(applicationServiceClient.getApplication("A001")).thenReturn(mockApp);
        
        ResolvedConsentPolicy policy = createMockPolicy(Set.of(DataScope.EDUCATION, DataScope.EMPLOYMENT, DataScope.SKILLS), "verification", "SKILL_BENEFIT", "DEPT-SKILLS");
        when(consentPolicyService.getPolicy("SKILL_BENEFIT")).thenReturn(policy);
        
        when(consentRepository.save(any(Consent.class))).thenAnswer(i -> {
            Consent c = i.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        consentService.grantConsent("user-1", request);
        
        ArgumentCaptor<Consent> captor = ArgumentCaptor.forClass(Consent.class);
        verify(consentRepository).save(captor.capture());
        Consent saved = captor.getValue();
        
        assertEquals("USER-1", saved.getCitizenId()); // Identity normalizer
        assertEquals("A001", saved.getApplicationId());
        assertEquals("SKILL_BENEFIT", saved.getServiceCode());
        assertEquals("verification", saved.getPurpose());
        assertEquals("DEPT-SKILLS", saved.getRequestingDepartmentId());
        
        Set<DataScope> grantedScopes = saved.getGrantedScopes();
        assertTrue(grantedScopes.containsAll(Set.of(DataScope.EDUCATION, DataScope.EMPLOYMENT, DataScope.SKILLS)));
    }

    @Test
    void testGrantConsent_WrongApplicationOwner_ThrowsException() {
        ConsentRequest request = new ConsentRequest();
        request.setApplicationId("A001");
        request.setServiceCode("SKILL_BENEFIT");
        
        ApplicationResponse mockApp = new ApplicationResponse();
        mockApp.setCitizenId("USER-2"); // Different owner!
        mockApp.setServiceCode("SKILL_BENEFIT");
        when(applicationServiceClient.getApplication("A001")).thenReturn(mockApp);
        
        assertThrows(SecurityException.class, () -> consentService.grantConsent("USER-1", request));
    }

    @Test
    void testGrantConsent_WrongServiceCodeContext_ThrowsException() {
        ConsentRequest request = new ConsentRequest();
        request.setApplicationId("A001");
        request.setServiceCode("SKILL_BENEFIT"); // Client tries to spoof service
        
        ApplicationResponse mockApp = new ApplicationResponse();
        mockApp.setCitizenId("USER-1");
        mockApp.setServiceCode("SCHOLARSHIP"); // Actual application service
        when(applicationServiceClient.getApplication("A001")).thenReturn(mockApp);
        
        assertThrows(ValidationException.class, () -> consentService.grantConsent("USER-1", request));
    }

    @Test
    void testRevokeConsent_CitizenOwnership_Denied() {
        Consent consent = new Consent();
        consent.setId(UUID.randomUUID());
        consent.setCitizenId("USER-1");
        consent.setStatus("GRANTED");
        
        when(consentRepository.findById(consent.getId())).thenReturn(Optional.of(consent));
        
        assertThrows(SecurityException.class, () -> consentService.revokeConsent("user-2", consent.getId()));
    }
}
