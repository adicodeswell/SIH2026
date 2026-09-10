# Phase 5 Walkthrough

## Phase 5 Objective
Build the Platform Operations and Interoperability experience. Provide visibility into connected government systems, operational metrics, SLA compliance, and audit logs based strictly on the capabilities exposed by the frozen backend.

## Phase 5 Status
**PARTIAL (Limited by backend APIs)**

## Features Implemented
- **Platform Operations Dashboard**: Created `/admin/interoperability` route to host the platform operations UI.
- **Service & Department Catalog**: Implemented a real-time table displaying all active/inactive integrated services fetched from the backend.
- **Documented API Limitations UI**: Gracefully implemented explicitly marked "Data Unavailable" and "Not Implementable" states for Interoperability Status, Connector Status, Audit Logs, and SLA Compliance, preventing the display of fake metrics.

## Routes
- Modified: `App.tsx` added `/admin` protected shell (requires `ROLE_ADMIN`).
- Added: `/admin/interoperability` (Platform Operations dashboard).

## Components Created
- `frontend/src/layouts/AdminLayout.tsx`: The layout shell for the platform administrator.
- `frontend/src/pages/admin/InteroperabilityDashboard.tsx`: The main operations dashboard.

## Services/API Clients Modified
- Reused `applicationApi` (Axios client) from `api.ts` to call the `/api/v1/services` endpoint.

## Exact Backend Endpoints Inspected
- `backend/interoperability-service/.../VerificationController.java` (`POST /api/v1/interop/fetch/scoped`)
- `backend/security-workflow-service/.../OfficerReviewController.java` (`GET /api/v1/officer/reviews`)
- `backend/security-workflow-service/.../WorkflowController.java` (`POST /internal/v1/workflows`)
- `backend/application-service/.../ServiceController.java` (`GET /api/v1/services`)

## Exact Backend Endpoints Consumed
- `GET /api/v1/services` (Returns `List<ServiceResponse>`)

## Request/Response Summary
- **Service Catalog**: `GET /api/v1/services` returns an array containing `serviceCode`, `serviceName`, `description`, `active` status, and department mapping. No request payload is required.

## Role Requirements
- The `/admin` route is protected by `ProtectedRoute` using `allowedRoles={['ADMIN']}`. Only Keycloak identities with the `ADMIN` role can access the dashboard.

## Security Decisions
- Relied on the existing Keycloak JWT and interceptor architecture.
- Instead of exposing raw `interoperability-service` endpoints like `/internal/v1/*` which are properly protected with `ROLE_SERVICE`, the frontend limits itself to endpoints meant for external consumption.

## Interoperability Capabilities Exposed
- **Master Data Only**: The frontend can currently only expose the mapping of Departments to Services. 

## Backend Limitations (Features Intentionally Not Implemented)
- **Audit Viewer**: Not implemented. No `AuditController` or any form of audit query API exists in the frozen backend.
- **Operational Metrics (Processing stats, connector failures)**: Not implemented. No business metrics APIs exist. Actuator endpoints (`/actuator/**`) exist but are meant for JVM infrastructure monitoring, not domain-level UI metrics.
- **SLA Visibility**: Not implemented. SLA targets and processing timestamps are not queryable.
- **No Fake Data**: Absolutely no hardcoded statistics or charts were added. The UI transparently states that the data is unavailable from the backend.

## Tests Added
- `src/__tests__/admin.test.tsx`: Tests the loading state, successful service catalog rendering, empty state handling, error handling, and the visibility of the "Backend API Limitations" messaging.

## Exact Test Results (VERIFIED BY RUNNING)
```
 Test Files  7 passed (7)
      Tests  38 passed (38)
```

## Build Result (VERIFIED BY RUNNING)
```
✓ built in 1.05s
```
(0 errors)

## Lint Result (VERIFIED BY RUNNING)
0 errors (only 4 pre-existing warnings in existing shared components).

## Backend Changes
- **NONE** (Verified via `git diff --name-only backend/`)

## Recommended Phase 6
- Since all core citizen and officer workflows are complete and platform operations have been built to the limits of the current backend, Phase 6 should likely focus on **E2E Testing, Final UI Polish, and Documentation** or expanding backend capabilities if the freeze is lifted.

## Recommended starting files for Phase 6
- `frontend/cypress/` or Playwright setup (if adding E2E tests).
- Global styling adjustments in `frontend/src/index.css`.
