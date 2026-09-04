package com.mahasetu.interoperability.model;

/**
 * A generic wrapper for the raw response returned by an external department.
 * This holds the unparsed JSON string, XML string, or CSV row.
 */
public class RawExternalResponse {
    
    private String rawData;
    private ExternalSystem sourceSystem;

    public RawExternalResponse(String rawData, ExternalSystem sourceSystem) {
        this.rawData = rawData;
        this.sourceSystem = sourceSystem;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public ExternalSystem getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(ExternalSystem sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
}
