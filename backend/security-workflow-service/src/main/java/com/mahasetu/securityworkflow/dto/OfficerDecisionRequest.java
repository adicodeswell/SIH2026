package com.mahasetu.securityworkflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload for submitting an officer review decision.
 * Contains the decision (APPROVE or REJECT) and an optional reason.
 * The officer identity is strictly extracted from the authenticated SecurityContext,
 * NOT accepted from the request body.
 */
public class OfficerDecisionRequest {

    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "(?i)^(APPROVE|REJECT)$", message = "Decision must be either APPROVE or REJECT")
    private String decision;

    private String reason;

    public OfficerDecisionRequest() {
    }

    public OfficerDecisionRequest(String decision, String reason) {
        this.decision = decision;
        this.reason = reason;
    }

    public String getDecision() {
        return decision != null ? decision.trim().toUpperCase() : null;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
