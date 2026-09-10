# PHASE 2 WALKTHROUGH — Citizen Service Discovery + Application Creation

## Phase Status

**COMPLETE** ✅

All code written, tests passing, build successful, lint clean, zero backend modifications.

---

## Objective

Build the **Citizen Service Discovery + Application Creation** journey:

1. Citizens browse a searchable catalog of government services.
2. Citizens view service details.
3. Citizens submit an application (with DPDP consent) for a chosen service.
4. Citizens see a success confirmation with their application reference number.

---

## What Was Implemented

### New Files

| File | Purpose |
|------|---------|
| `src/types/service.ts` | TypeScript interfaces matching backend DTOs: `ServiceResponse`, `CreateApplicationRequest`, `ApplicationStatus`, `ApplicationResponse`, `ConsentRequest`, `ConsentResponse` |
| `src/services/serviceCatalog.ts` | API service layer wrapping `applicationApi` and `workflowApi` for service/application/consent calls |
| `src/components/citizen/ServiceCard.tsx` | Reusable service card component with civic design tokens |
| `src/components/citizen/ApplicationForm.tsx` | Application form with citizenId field, DPDP consent checkbox, client-side validation |
| `src/components/citizen/ApplicationSuccess.tsx` | Success confirmation displaying reference number and next steps |
| `src/pages/citizen/ServicesPage.tsx` | Service catalog page with TanStack Query, search/filter, loading/error/empty states |
| `src/pages/citizen/ServiceDetailPage.tsx` | Service detail + application form + `useMutation` for consent→application submission |
| `src/__tests__/services.test.tsx` | 9 new Phase 2 tests |

### Modified Files

| File | Changes |
|------|---------|
| `src/App.tsx` | Added routes: `/citizen/services` and `/citizen/services/:serviceId`. Removed `CitizenServicesPage` placeholder import. |
| `src/pages/citizen/Dashboard.tsx` | Rewritten: removed inline application flow, now routes to `/citizen/services/:serviceCode`. Shows scheme cards linking to detail pages. |
| `src/pages/placeholders/CitizenPlaceholders.tsx` | Removed `CitizenServicesPage` export (now a real page). Kept `ApplicationsPage`, `ApplicationDetailsPage`, `ConsentsPage` as placeholders. |

---

## User Journey

```
┌────────────────────┐
│  Citizen Dashboard  │  /citizen/dashboard
│  (scheme cards)     │
└────────┬───────────┘
         │ "Browse All Services" or scheme card click
         ▼
┌────────────────────┐
│  Services Catalog   │  /citizen/services
│  (search + filter)  │
└────────┬───────────┘
         │ Click a service card
         ▼
┌────────────────────┐
│  Service Detail     │  /citizen/services/:serviceId
│  + Application Form │
└────────┬───────────┘
         │ Fill citizenId, check consent, submit
         ▼
┌────────────────────┐
│  Success Screen     │  (inline on same page)
│  Reference number   │
└────────────────────┘
```

---

## Routes

| Path | Component | Auth Required | Role |
|------|-----------|--------------|------|
| `/citizen/services` | `ServicesPage` | Yes | CITIZEN |
| `/citizen/services/:serviceId` | `ServiceDetailPage` | Yes | CITIZEN |

Both routes are wrapped in `ProtectedRoute` with `requiredRole="CITIZEN"` and nested inside `CitizenLayout`.

---

## Components

### `ServiceCard`
- Displays service name, description, department badge, status indicator.
- Links to the service detail page via `serviceCode`.
- Civic styling: deep navy headers, saffron accents, off-white card backgrounds.

### `ApplicationForm`
- Fields: Citizen ID (text input, required).
- DPDP consent checkbox (required, links to data protection text).
- Client-side validation: both fields must be filled before submit.
- Disabled state during submission with loading spinner.

### `ApplicationSuccess`
- Displays application reference number (`applicationNumber` from API response).
- Shows submitted timestamp.
- Action buttons: "View My Applications" and "Browse More Services".

---

## API Endpoints Used

| Method | URL | Service | Purpose |
|--------|-----|---------|---------|
| `GET` | `/api/v1/services` | application-service | Fetch all available services |
| `GET` | `/api/v1/services/{id}` | application-service | Fetch single service detail |
| `POST` | `/api/v1/consents` | security-workflow-service | Grant DPDP consent before application |
| `POST` | `/api/v1/applications` | application-service | Submit a new application |

---

## Request / Response Contracts

### GET /api/v1/services → ServiceResponse[]
```json
{
  "serviceCode": "SKILL_BENEFIT",
  "serviceName": "Skill Development Benefit",
  "description": "...",
  "active": true,
  "departmentCode": "DEPT_EMP",
  "departmentName": "Employment Department"
}
```

### POST /api/v1/consents
**Request:**
```json
{
  "dataScope": "education,employment,skills",
  "purpose": "Application for SKILL_BENEFIT",
  "requestingDepartmentId": "DEPT_EMP"
}
```
**Response:** `ConsentResponse` with `consentId`, `status`, `createdAt`.

### POST /api/v1/applications
**Request:**
```json
{
  "citizenId": "MH1001",
  "serviceCode": "SKILL_BENEFIT"
}
```
**Response:** `ApplicationResponse` with `applicationNumber`, `status`, `citizenId`, `serviceCode`, `submittedAt`.

---

## Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| Citizen ID | Required, non-empty string | "Citizen ID is required" |
| DPDP Consent | Must be checked (true) | "You must consent to data processing" |

---

## Error Handling

| Scenario | Handling |
|----------|----------|
| Network failure | Toast error + inline error message |
| 401 Unauthorized | Redirect to login (handled by API interceptor) |
| 403 Forbidden | Toast: "You don't have permission" |
| 404 Not Found | "Service not found" message on detail page |
| 409 Conflict | "Application already exists for this service" — handled gracefully |
| 5xx Server Error | Generic "Something went wrong" toast |
| Empty service list | "No services available" empty state with illustration |
| Search no results | "No services match your search" message |

---

## Auth / Role Behavior

- Routes require `CITIZEN` role via `ProtectedRoute`.
- Unauthenticated users → redirected to `/login`.
- Non-CITIZEN roles → redirected to their respective dashboards.
- Citizen ID is pre-populated from `user?.username` when available.
- API calls use the JWT token from `AuthContext` via axios interceptors configured in `api.ts`.

---

## Consent Policy Mappings

Consent data scope is determined by service code, matching the backend's `application.yml` configuration:

| Service Code | Data Scope |
|-------------|------------|
| `SKILL_BENEFIT` | `education,employment,skills` |
| `SCHOLARSHIP` | `education` |
| `SRV-EDU` | `education,health` |

---

## Tests

### Test Files

| File | Tests | Status |
|------|-------|--------|
| `src/__tests__/api.test.ts` | 5 | ✅ PASSED |
| `src/__tests__/routes.test.tsx` | 9 | ✅ PASSED |
| `src/__tests__/services.test.tsx` | 9 | ✅ PASSED |
| **Total** | **23** | **ALL PASSED** |

### Phase 2 Test Cases (`services.test.tsx`)

1. **renders loading state** — Verifies spinner shown while fetching services.
2. **renders services after loading** — Verifies service cards appear with data from API.
3. **filters services by search term** — Verifies search filters the catalog.
4. **renders empty state when no services** — Verifies empty message for zero results.
5. **renders service detail page** — Verifies detail page shows service info.
6. **validates required fields** — Verifies form prevents submission without required fields.
7. **submits application successfully** — Verifies consent→application flow with success screen.
8. **handles 409 conflict error** — Verifies duplicate application error is shown.
9. **renders application form** — Verifies form renders with expected fields and labels.

---

## Test Command + Result

```bash
$ npm test

 RUN  v5.0.0 /home/ankit/SIH2026/frontend

 Test Files  3 passed (3)
      Tests  23 passed (23)
   Start at  15:17:10
   Duration  2.38s
```

---

## Build Command + Result

```bash
$ npm run build

> tsc -b && vite build

vite v8.2.2 building client environment for production...
✓ 2088 modules transformed.

dist/index.html                   0.45 kB │ gzip:   0.29 kB
dist/assets/index-BcZxsUdd.css  57.81 kB │ gzip:  11.03 kB
dist/assets/index-XMzud2uV.js  493.93 kB │ gzip: 154.81 kB

✓ built in 827ms
```

---

## Lint Command + Result

```bash
$ npm run lint

> oxlint

# 0 errors, only pre-existing warnings in officer/Dashboard.tsx and ui components
```

---

## Backend Files Touched

**NONE** ✅

```bash
$ git diff --name-only backend/
# (empty — no backend files modified)
```

All changes are confined to the `frontend/` directory.

---

## Known Limitations

1. **Consent policy mapping is hardcoded in the frontend** — The mapping of service codes to data scopes is duplicated from the backend's `application.yml`. If backend consent policies change, the frontend must be updated manually. A future enhancement could expose this mapping via an API endpoint.

2. **Citizen ID is a free-text field** — The backend requires a valid citizen ID from its database (MH1001, MH2002, MH3003 in seed data). The frontend validates non-empty but cannot verify validity client-side.

3. **No application tracking yet** — After submission, the success screen links to "View My Applications" which currently points to a placeholder page. This will be implemented in Phase 3.

4. **No real-time status updates** — Application status is not polled or subscribed to. The citizen sees the initial SUBMITTED status only.

5. **Search is client-side** — All services are fetched from the API and filtered in the browser. This works for the current small dataset but may need server-side search for scale.

---

## Future Phases

| Phase | Scope |
|-------|-------|
| **Phase 3** | Consent Management + Unified Application Tracking — citizen views all applications with status, manages consent records |
| **Phase 4** | Officer Workflow — officers view, verify, approve/reject applications via the Camunda workflow |
| **Phase 5** | Interoperability Dashboard — cross-department data sharing visibility |

---

## Notes for Next Agent

1. **Backend is FROZEN** — Do not modify any backend code, schemas, or API contracts.
2. **TanStack Query** is configured in `App.tsx` with `retry: 1`, `refetchOnWindowFocus: false`.
3. **Consent must precede application creation** — The `ServiceDetailPage` calls `grantConsent()` then `createApplication()` sequentially using `useMutation`.
4. **Seed citizens**: MH1001 (Rahul Patil), MH2002 (Priya Singh), MH3003 (Amit Sharma).
5. **`verbatimModuleSyntax: true`** — Use `import type` for type-only imports.
6. **`noUnusedLocals: true` / `noUnusedParameters: true`** — Unused imports break the build.
7. **vitest** uses `jsdom` environment with setup at `./src/test-setup.ts`.
8. **Design tokens** are in `index.css` — civic palette (navy, saffron, off-white). No glassmorphism.

---

## Recommended Starting Point for Phase 3

1. Read this walkthrough and `PHASE_1_WALKTHROUGH.md`.
2. Implement `GET /api/v1/applications?citizenId={id}` integration on the citizen side (check if backend supports query param filtering — if not, fetch all and filter client-side).
3. Build `ApplicationsPage` (replace placeholder) — list all citizen applications with status badges.
4. Build `ApplicationDetailsPage` — show full application detail with timeline.
5. Build `ConsentsPage` — list all consent records with revocation capability (if backend supports `DELETE /api/v1/consents/{id}`).
6. Add tests for all new pages.
