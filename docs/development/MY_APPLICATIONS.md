# My Applications & Activity Timeline Integration

## Overview
This document details the complete end-to-end implementation of the "My Applications" feature for citizens. It replaces the old reference-number-only lookup approach with an authenticated dashboard and timeline, unifying application events with security workflow consent/audit events.

## Components Modified

### 1. Backend (`application-service`)
- **`ApplicationRepository`**: Added `findByCitizen_CitizenIdOrderByCreatedAtDesc`.
- **`ApplicationController`**: Added `GET /api/v1/applications/me` and `GET /api/v1/applications/{id}/activity`. Restricted access using JWT citizen ID checks.
- **`ApplicationService`**: Added internal logic to fetch the unified activity list, sorting by `occurredAt` ascending, and normalizing raw event types into citizen-friendly `CitizenApplicationActivityResponse` DTOs.
- **`WorkflowAuditClient`**: New Feign-like RestTemplate client using `ServiceTokenProvider` to authenticate to the workflow service.

### 2. Backend (`security-workflow-service`)
- **`AuditController`**: Exposed `GET /internal/v1/audit/applications/{id}` for internal `ROLE_SERVICE` calls.
- **`AuditService`**: Updated `recordConsentGranted` and `recordConsentRevoked` to correctly capture and persist the `applicationId` rather than `null`.
- **`AuditLogRepository`**: Added `findByApplicationIdOrderByOccurredAtAsc`.

### 3. Frontend
- **Types**: Added `CitizenApplicationSummaryResponse` and `CitizenApplicationActivityResponse`.
- **`ApplicationService`**: Wired up new API endpoints.
- **`ApplicationsPage`**: Transformed into a "My Applications" dashboard using `useQuery` to fetch applications associated with the logged-in citizen. Added robust status badge rendering.
- **`ApplicationDetailsPage`**: Integrated the new timeline endpoint, presenting combined Application + Workflow/Consent events chronologically, with clean icons and civic language formatting. Added detailed formatting using `date-fns`.
- **`Dashboard`**: Added a quick preview widget tracking total applications for the logged-in citizen.

## Security Considerations
- The API securely restricts data based on the citizen's JWT (`preferred_username`), ensuring no citizen can query another citizen's applications.
- System-to-system communication uses the OAuth2 Client Credentials flow via `ServiceTokenProvider` to talk to `security-workflow-service`.
- Internal logs (like raw Camunda tokens or un-normalized strings) are swallowed or transformed so that the citizen only sees actionable status text (e.g. "Application review assigned to a government officer" instead of "OFFICER_CLAIM").
