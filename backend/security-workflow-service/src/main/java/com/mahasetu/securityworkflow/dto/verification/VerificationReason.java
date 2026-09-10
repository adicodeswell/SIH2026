package com.mahasetu.securityworkflow.dto.verification;

public class VerificationReason {
    private String code;
    private String fieldName;
    private String source;

    public VerificationReason() {}

    public VerificationReason(String code, String fieldName, String source) {
        this.code = code;
        this.fieldName = fieldName;
        this.source = source;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
