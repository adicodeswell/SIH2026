package com.mahasetu.securityworkflow.dto.verification;

import java.util.Map;

public class FieldVerificationResult {
    private String fieldName;
    private FieldVerificationStatus status;
    private Map<String, String> providedValues;

    public FieldVerificationResult() {}
    public FieldVerificationResult(String fieldName, FieldVerificationStatus status, Map<String, String> providedValues) {
        this.fieldName = fieldName;
        this.status = status;
        this.providedValues = providedValues;
    }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public FieldVerificationStatus getStatus() { return status; }
    public void setStatus(FieldVerificationStatus status) { this.status = status; }
    public Map<String, String> getProvidedValues() { return providedValues; }
    public void setProvidedValues(Map<String, String> providedValues) { this.providedValues = providedValues; }
}
