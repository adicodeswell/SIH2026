package com.mahasetu.securityworkflow.dto;

/**
 * Represents the consent policy for a given service code.
 * Contains the required data scope and purpose for consent verification.
 */
public class ConsentPolicy {

    private String dataScope;
    private String purpose;

    public ConsentPolicy() {
    }

    public ConsentPolicy(String dataScope, String purpose) {
        this.dataScope = dataScope;
        this.purpose = purpose;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
