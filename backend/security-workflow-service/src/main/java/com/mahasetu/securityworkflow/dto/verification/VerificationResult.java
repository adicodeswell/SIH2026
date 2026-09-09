package com.mahasetu.securityworkflow.dto.verification;

import java.util.List;

public class VerificationResult {
    private VerificationStatus overallStatus;
    private List<SourceVerificationResult> sourceResults;
    private List<FieldVerificationResult> fieldResults;

    public VerificationResult() {}
    
    public VerificationResult(VerificationStatus overallStatus, List<SourceVerificationResult> sourceResults, List<FieldVerificationResult> fieldResults) {
        this.overallStatus = overallStatus;
        this.sourceResults = sourceResults;
        this.fieldResults = fieldResults;
    }

    public VerificationStatus getOverallStatus() { return overallStatus; }
    public void setOverallStatus(VerificationStatus overallStatus) { this.overallStatus = overallStatus; }
    public List<SourceVerificationResult> getSourceResults() { return sourceResults; }
    public void setSourceResults(List<SourceVerificationResult> sourceResults) { this.sourceResults = sourceResults; }
    public List<FieldVerificationResult> getFieldResults() { return fieldResults; }
    public void setFieldResults(List<FieldVerificationResult> fieldResults) { this.fieldResults = fieldResults; }
}
