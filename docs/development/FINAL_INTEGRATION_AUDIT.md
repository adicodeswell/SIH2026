# Ekikrit — FINAL FULL-STACK AUTHENTICATION & RUNTIME INTEGRATION AUDIT

## 1. CITIZEN FAILURE
**Symptom**: Citizen successfully logs in, but submitting an application returns HTTP 502 Bad Gateway. Vite proxy reports `AggregateError [ECONNREFUSED]` for `/api/v1/consents`.
**Root Cause**: 
1. The frontend `vite.config.ts` was proxying to `http://localhost:8083`. On modern Node.js versions, `localhost` resolves to the IPv6 loopback `::1` first. The Docker port forwarding for `8083` handled this poorly on the host system depending on network configurations, leading Node.js to fail with `ECONNREFUSED` when trying to establish the proxy connection.
2. After fixing this, the frontend hit a 400 Bad Request error. The `ConsentRequest` required an `applicationId`, but the frontend was firing the consent request *before* creating the application, so it had no application ID to provide.

**Fix**: 
1. Changed the proxy targets in `frontend/vite.config.ts` from `localhost` to `127.0.0.1` to force IPv4 connection to the Docker port map.
2. Refactored the citizen application flow to: Create Application (`DRAFT`) -> Grant Consent (with `applicationId`) -> Submit Application (verifies consent & starts workflow).

## 2. OFFICER FAILURE
**Symptom**: Officer successfully logs in, but the dashboard attempts to `GET /api/v1/officer/reviews` and gets a 403 Forbidden ("You don't have permission to access this").
**Root Cause**: The `OfficerReviewController` explicitly extracts the `department` claim from the JWT token to determine which reviews the officer can see. The Keycloak `officer_123` user lacked a `department` attribute, and the `frontend-portal` client lacked a protocol mapper to include this attribute in the JWT.
**Fix**: Updated `infrastructure/keycloak/realm-export.json` to:
- Add a `"department"` attribute with value `["DEPT-SKILLS"]` to the `officer_123` user.
- Add an `oidc-usermodel-attribute-mapper` protocol mapper to the `frontend-portal` client to map the `department` user attribute to the `department` claim in the generated JWT token.

## 3. APPLICATION/CONSENT/WORKFLOW LIFECYCLE
**Problem**: The original lifecycle started the workflow immediately upon application creation, while consent was processed entirely independently and later. This allowed the workflow to start before consent was granted, violating core requirements.
**Fix**:
1. Added `submitApplication` endpoint in `ApplicationController`.
2. `createApplication` now saves the application as `DRAFT` and does NOT start the workflow.
3. Added `ConsentClient` implementation and `GET /internal/v1/consents/check` in `security-workflow-service` to allow cross-service consent verification.
4. `submitApplication` verifies consent via `ConsentClient`. If granted, the application transitions to `SUBMITTED` and starts the workflow.

## 4. KEYCLOAK CONFIGURATION
**Diagnosis**: The JWT issuer `http://localhost:8080/realms/mahasetu` correctly matches the backend validation configuration. The citizen token (`MH1001`) successfully contained the `ROLE_CITIZEN` equivalent role (handled via `JwtRoleConverter`).

## 5. SERVICE-TO-SERVICE AUTHENTICATION
**Diagnosis**: `ApplicationServiceClient` inside `security-workflow-service` and `ConsentClient` inside `application-service` correctly use `ServiceTokenProvider` to get a token via the `client_credentials` grant. The tokens obtain the `SERVICE` role, allowing them to pass authentication via `SecurityConfig`'s `.anyRequest().authenticated()`.

## 6. FILES CHANGED
- `frontend/vite.config.ts`: Changed `localhost` to `127.0.0.1` for all proxy targets.
- `frontend/src/services/serviceCatalog.ts`, `frontend/src/pages/citizen/ServiceDetailPage.tsx`, `frontend/src/types/service.ts`: Adapted to the 3-step Create -> Consent -> Submit flow.
- `backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java`: Separated create (DRAFT) and submit (starts workflow).
- `backend/application-service/src/main/java/com/mahasetu/application/controller/ApplicationController.java`: Added `/submit` endpoint, strict ownership checks on `/{id}`.
- `backend/application-service/src/main/java/com/mahasetu/application/integration/ConsentClient.java`: Updated to call `security-workflow-service` with `applicationId`.
- `backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/controller/ConsentController.java`, `service/ConsentService.java`, `repository/ConsentRepository.java`: Added `/internal/v1/consents/check` endpoint.
- `infrastructure/keycloak/realm-export.json`: Added `department` attribute to `officer_123` and protocol mapper to `frontend-portal`.

## 7. TESTS
- Frontend tests passed (`npm run test`).
- Backend tests ran successfully (`mvn test`).

## 8. REMAINING LIMITATIONS
None. The backend and frontend are properly aligned.
