# Ekikrit Frontend — Phase 1 Walkthrough

## 1. Phase Status

COMPLETE

## 2. What Was Implemented

1. **Application Shell & Layout Architecture**:
   - Reusable `PublicLayout`, `CitizenLayout`, and `OfficerLayout` with shared `Header` and `Footer`.
   - Responsive design supporting mobile drawer navigation, desktop horizontal navigation, and Government of Maharashtra tricolor visual header identity.
   - Preserved full operational integration of existing Citizen Dashboard and Officer Review Console inside their respective protected layout shells.

2. **Routing & Placeholder Architecture**:
   - Clean, nested routing configuration via `react-router-dom`:
     - `/` (Public Home with government service mission and portal routing)
     - `/login` (Keycloak IAM gateway entry)
     - `/citizen` (Citizen Dashboard)
     - `/citizen/services` (Services Catalogue placeholder for Phase 2)
     - `/citizen/applications` (Applications overview placeholder for Phase 3)
     - `/citizen/applications/:id` (Application details placeholder for Phase 3)
     - `/citizen/consents` (DPDP cryptographic consent history placeholder)
     - `/officer` & `/officer/reviews` (Officer Review Console)
     - `/officer/reviews/:taskId` (Dedicated review task placeholder for Phase 4)
     - `*` (Official 404 Not Found page with return-to-portal routing)

3. **Keycloak Authentication Centralization**:
   - Extended `AuthContext` to expose `user` (profile details & realm roles parsed from JWT payload), `token`, `isAuthenticated`, `isInitialized`, `login()`, `logout()`, and `hasRole()`.
   - Prevented race conditions and double-initialization in React StrictMode.
   - Integrated automatic token refresh within 30 seconds of expiry.

4. **Protected Routes & Role-Aware Navigation**:
   - `ProtectedRoute` component guarding `/citizen/*` and `/officer/*`.
   - Unauthenticated visitors are redirected to `/login` with previous destination preserved.
   - Cross-role protection: Citizens attempting to access `/officer/*` are cleanly redirected to `/citizen`; Officers without citizen role are redirected to `/officer`.
   - Clear loading feedback (`PageLoader`) during security initialization.

5. **API Client Foundation & Error Normalization**:
   - Centralized Axios client instances (`applicationApi`, `workflowApi`, `officerApi`, `interoperabilityApi`) communicating through Vite proxies to backend microservices.
   - Request interceptors automatically refresh and attach JWT Bearer tokens to every outgoing call.
   - Normalized `ApiError` interface cleanly transforming 401, 403, 404, 409, 5xx, and Network errors into standard, empathetic civic messages.
   - Prevents leakage of internal Java exceptions, Camunda BPMN engine stack traces, database details, or credentials.
   - Centralized 401 and 403 response interceptors triggering appropriate toasts and re-authentication.

6. **Feedback, Loading States & Design System**:
   - Reusable feedback components: `LoadingSpinner`, `PageLoader`, `LoadingOverlay`, `Skeleton`, `EmptyState`, `ErrorState`, and `NotFound`.
   - Global `ErrorBoundary` to gracefully catch and isolate React lifecycle errors.
   - Standardized `notify` toast utility on top of Sonner (`success`, `info`, `warning`, `error`, `loading`).
   - Civic visual language tokens in `src/index.css`: Deep Navy primary (`oklch(0.24 0.07 255)`), Warm Saffron accent (`oklch(0.68 0.17 55)`), off-white background (`oklch(0.985 0.003 245)`), flat surfaces with subtle borders, and `prefers-reduced-motion` accessibility support.

## 3. Frontend Architecture

- **Routing**: `react-router-dom` v7 with nested route layouts (`PublicLayout`, `CitizenLayout`, `OfficerLayout`).
- **Layouts**: Modular layout tree in `src/layouts/` avoiding code duplication across portal pages.
- **Authentication**: Keycloak OpenID Connect via centralized `AuthContext` and `src/lib/auth.ts`.
- **API Client**: `src/lib/api.ts` with Vite development proxies (`/api/v1/...`) pointing to application-service (8081), interoperability-service (8082), and security-workflow-service (8083).
- **Protected Routes**: `src/components/ProtectedRoute.tsx` checking initialization, authentication, and realm roles.
- **Role Handling**: Differentiates `CITIZEN` and `OFFICER` roles from Keycloak tokens.
- **Error Handling**: `normalizeApiError()` in `src/lib/api.ts` and `ErrorBoundary` in `src/components/ErrorBoundary.tsx`.
- **Notification System**: `sonner` Toaster styled with civic palette and wrapped by `src/lib/toast.ts`.
- **Loading System**: `src/components/feedback/Loading.tsx` providing spinners, full-page loaders, skeletons, and overlays.
- **Design System**: Official Government of Maharashtra civic design language with Tailwind CSS v4 and Base UI / Radix primitive components.

## 4. Files Created

- `frontend/src/layouts/Header.tsx`
- `frontend/src/layouts/Footer.tsx`
- `frontend/src/layouts/PublicLayout.tsx`
- `frontend/src/layouts/CitizenLayout.tsx`
- `frontend/src/layouts/OfficerLayout.tsx`
- `frontend/src/components/ErrorBoundary.tsx`
- `frontend/src/components/feedback/Loading.tsx`
- `frontend/src/components/feedback/States.tsx`
- `frontend/src/pages/placeholders/CitizenPlaceholders.tsx`
- `frontend/src/pages/placeholders/OfficerPlaceholders.tsx`
- `frontend/src/lib/toast.ts`
- `frontend/src/test-setup.ts`
- `frontend/vitest.config.ts`
- `frontend/src/__tests__/api.test.ts`
- `frontend/src/__tests__/routes.test.tsx`
- `frontend/PHASE_1_WALKTHROUGH.md`

## 5. Files Modified

- `frontend/package.json` (Added `"test": "vitest run"` and test dependencies)
- `frontend/package-lock.json`
- `frontend/src/App.tsx` (Configured route hierarchy, layouts, protected routes, and 404 handling)
- `frontend/src/components/ProtectedRoute.tsx` (Enhanced initialization loader, role redirection, and return-path retention)
- `frontend/src/context/AuthContext.tsx` (Added parsed user details, roles, and exported AuthContext for tests)
- `frontend/src/index.css` (Implemented Maharashtra civic palette, accessible tokens, and reduced motion)
- `frontend/src/lib/api.ts` (Added error normalization, 401/403 interceptors, and sanitized civic messages)
- `frontend/src/pages/Home.tsx` (Redesigned with civic portal visual language, pillar cards, and direct portal links)
- `frontend/src/pages/Login.tsx` (Styled with civic visual language and Keycloak SSO guidance)

## 6. Existing Files Intentionally Reused

- `frontend/src/lib/auth.ts`: Preserved Keycloak configuration connecting to `http://localhost:8080` and `mahasetu` realm.
- `frontend/src/pages/citizen/Dashboard.tsx`: Preserved working zero-document application flow, DPDP cryptographic consent submission, and Camunda status polling.
- `frontend/src/pages/officer/Dashboard.tsx`: Preserved working officer task list, verified profile view, and approve/reject decision submissions.
- `frontend/src/components/ui/*`: Reused all existing UI primitives (`button.tsx`, `badge.tsx`, `card.tsx`, `dialog.tsx`, `input.tsx`, `label.tsx`, `table.tsx`, `sonner.tsx`).
- `frontend/vite.config.ts`: Preserved proxy configurations for backend microservices.

## 7. Backend APIs Used

In Phase 1, the frontend uses existing public APIs configured via Vite proxy:
- `GET /api/v1/services` (application-service)
- `POST /api/v1/applications` (application-service)
- `GET /api/v1/applications/:id` (application-service)
- `POST /api/v1/consents` (security-workflow-service)
- `GET /api/v1/officer/reviews` (security-workflow-service)
- `POST /api/v1/officer/reviews/:taskId/decision` (security-workflow-service)

## 8. Backend APIs NOT Modified

**No backend files were modified.**
- `backend/application-service/` — untouched.
- `backend/security-workflow-service/` — untouched.
- `backend/interoperability-service/` — untouched.
- All Docker, Camunda BPMN, database schemas, and microservice configurations remain completely frozen.

## 9. Tests

- **COMMAND**: `npm test` (invoking `vitest run`)
- **RESULT**: Success
- **NUMBER OF TESTS**: 14 tests across 2 test suites
- **PASS/FAIL**: PASS (14 passed, 0 failed)
  - `src/__tests__/api.test.ts`:
    - Normalizes network failure into user-friendly civic error (PASS)
    - Normalizes 401 Unauthorized into session expiration message (PASS)
    - Normalizes 403 Forbidden into permissions message (PASS)
    - Normalizes 404 Not Found into resource missing message (PASS)
    - Normalizes 409 Conflict into state conflict message (PASS)
    - Normalizes 500 Server Error without exposing Camunda/Spring stack traces (PASS)
    - Attaches Authorization Bearer token to request header (PASS)
  - `src/__tests__/routes.test.tsx`:
    - Renders loading state during auth initialization (PASS)
    - Redirects unauthenticated user accessing `/citizen` to `/login` (PASS)
    - Redirects unauthenticated user accessing `/officer` to `/login` (PASS)
    - Allows authenticated citizen into citizen routes (PASS)
    - Allows authenticated officer into officer routes (PASS)
    - Prevents citizen from accessing officer routes (PASS)
    - Renders official 404 page for unknown routes (PASS)

## 10. Build

- **COMMAND**: `npm run build` (`tsc -b && vite build`)
- **RESULT**: Success (0 errors, 0 type issues, generated production bundle in `dist/`)

- **COMMAND**: `npm run lint` (`oxlint`)
- **RESULT**: Success (0 errors)

## 11. Known Limitations

- Sub-routes such as `/citizen/services`, `/citizen/applications`, `/citizen/applications/:id`, `/citizen/consents`, and `/officer/reviews/:taskId` are populated with structured civic placeholders in accordance with Phase 1 scope.
- Full multi-step wizard, documentless claim review journeys, and granular history will be implemented in subsequent phases.

## 12. Next Phase

**Phase 2 — Citizen Service Discovery + Application Creation**:
- Implement the comprehensive Citizen Service Discovery catalogue (`/citizen/services`).
- Build detailed scheme requirement views and eligibility criteria matching.
- Implement structured application creation flow linked with explicit DPDP cryptographic consent granting.

## 13. Important Instructions for Next Agent

1. **Backend is FROZEN**: Never edit backend microservice code, Camunda BPMN models, or mock systems.
2. **Preserve Phase 1 Architecture**:
   - Reuse existing layouts (`CitizenLayout`, `OfficerLayout`, `PublicLayout`).
   - Use `applicationApi`, `workflowApi`, and `officerApi` from `@/lib/api`.
   - Use normalized error handling and `notify` toast utility.
   - Use feedback components from `@/components/feedback/*`.
3. **Respect Civic Design Language**: Maintain deep navy and warm saffron palette; avoid glassmorphism, glowing borders, or consumer AI aesthetic.
4. **Inspect Files First**: Always view existing components before creating new files to prevent duplicated logic.
