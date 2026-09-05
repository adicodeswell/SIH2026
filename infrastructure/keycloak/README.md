# Keycloak Configuration for MahaSetu

This directory contains the initial realm import configuration for local and container development.

## Realm: `mahasetu`

- **Import File**: `realm-export.json`
- **Roles**:
  - `SERVICE`: For service-to-service communication (`application-service`, `security-workflow-service`)
  - `CITIZEN`: For citizen portal operations
  - `OFFICER`: For officer task claiming, review, and approval
  - `ADMIN`: For administrative operations

## Service-to-Service Clients

1. **`application-service`**:
   - Grant Type: `client_credentials`
   - Secret: `dev-application-service-secret`
   - Assigned Role: `SERVICE`

2. **`security-workflow-service`**:
   - Grant Type: `client_credentials`
   - Secret: `dev-security-workflow-secret`
   - Assigned Role: `SERVICE`

## Seed Test Users

- **Citizen**: `MH1001` / `citizen123` (Role: `CITIZEN`)
- **Officer**: `officer_123` / `officer123` (Role: `OFFICER`)
