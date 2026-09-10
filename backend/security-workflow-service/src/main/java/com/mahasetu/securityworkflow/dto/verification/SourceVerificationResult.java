package com.mahasetu.securityworkflow.dto.verification;

public class SourceVerificationResult {
    private String source;
    private String status;
    private String errorReason;

    public SourceVerificationResult() {}
    public SourceVerificationResult(String source, String status, String errorReason) {
        this.source = source;
        this.status = status;
        this.errorReason = errorReason;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorReason() { return errorReason; }
    public void setErrorReason(String errorReason) { this.errorReason = errorReason; }
}
