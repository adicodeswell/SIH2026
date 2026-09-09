#!/bin/bash

# INTEROPERABILITY SERVICE DTOs
mkdir -p backend/interoperability-service/src/main/java/com/mahasetu/interoperability/dto
cat << 'INNER_EOF' > backend/interoperability-service/src/main/java/com/mahasetu/interoperability/dto/ScopedInteropRequest.java
package com.mahasetu.interoperability.dto;

import java.util.List;

public class ScopedInteropRequest {
    private String citizenId;
    private List<String> allowedScopes;

    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }

    public List<String> getAllowedScopes() { return allowedScopes; }
    public void setAllowedScopes(List<String> allowedScopes) { this.allowedScopes = allowedScopes; }
}
INNER_EOF

cat << 'INNER_EOF' > backend/interoperability-service/src/main/java/com/mahasetu/interoperability/model/SourceDataResult.java
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
INNER_EOF

cat << 'INNER_EOF' > backend/interoperability-service/src/main/java/com/mahasetu/interoperability/model/DataScope.java
package com.mahasetu.interoperability.model;

public enum DataScope {
    EDUCATION,
    EMPLOYMENT,
    SKILLS,
    HEALTH
}
INNER_EOF

# SECURITY WORKFLOW SERVICE DTOs
cat << 'INNER_EOF' > backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/dto/ScopedInteropRequest.java
package com.mahasetu.securityworkflow.dto;

import java.util.List;

public class ScopedInteropRequest {
    private String citizenId;
    private List<String> allowedScopes;
    
    public ScopedInteropRequest() {}
    public ScopedInteropRequest(String citizenId, List<String> allowedScopes) {
        this.citizenId = citizenId;
        this.allowedScopes = allowedScopes;
    }

    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }

    public List<String> getAllowedScopes() { return allowedScopes; }
    public void setAllowedScopes(List<String> allowedScopes) { this.allowedScopes = allowedScopes; }
}
INNER_EOF

cat << 'INNER_EOF' > backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/dto/SourceDataResult.java
package com.mahasetu.securityworkflow.dto;

public class SourceDataResult {
    private String source;
    private String status;
    private String error;
    private CanonicalCitizenData data;

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public CanonicalCitizenData getData() { return data; }
    public void setData(CanonicalCitizenData data) { this.data = data; }
}
INNER_EOF

echo "DTOs created"
