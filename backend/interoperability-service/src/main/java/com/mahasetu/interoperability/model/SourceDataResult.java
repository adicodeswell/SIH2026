package com.mahasetu.interoperability.model;

public class SourceDataResult {
    private String source;
    private String status; // "SUCCESS", "FAILED"
    private String error;
    private CanonicalCitizenData data;

    public SourceDataResult() {}
    public SourceDataResult(String source, String status, String error, CanonicalCitizenData data) {
        this.source = source;
        this.status = status;
        this.error = error;
        this.data = data;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public CanonicalCitizenData getData() { return data; }
    public void setData(CanonicalCitizenData data) { this.data = data; }
}
