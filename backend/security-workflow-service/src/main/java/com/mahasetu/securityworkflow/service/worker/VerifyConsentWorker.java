package com.mahasetu.securityworkflow.service.worker;

import com.mahasetu.securityworkflow.service.ConsentService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class VerifyConsentWorker implements JavaDelegate {

    private final ConsentService consentService;

    public VerifyConsentWorker(ConsentService consentService) {
        this.consentService = consentService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String citizenId = (String) execution.getVariable("citizenId");
        
        // In a real scenario, this would be derived dynamically from the application type / service code
        // For Phase 5, we hardcode the required dataScope and purpose based on the workflow requirements
        String dataScope = "education,employment,skills";
        String purpose = "verification";

        boolean consentValid = consentService.checkConsent(citizenId, dataScope, purpose);
        
        execution.setVariable("consentValid", consentValid);
    }
}
