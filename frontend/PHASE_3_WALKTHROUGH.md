# Phase 3 Walkthrough

## Phase 3 Objective
Implement the Citizen Application Management Experience.
Goals included:
1. My Applications tracker.
2. Application Details with Status and Verification Data.
3. Application Status/Timeline.
4. Consent Management (view and revoke).

## Phase 3 Status
**PARTIAL (with blockages handled via UI alternatives)**

## Features Implemented
- **Application Tracker**: Instead of a list of applications (which the backend does not support), built a tracker where citizens can search for their application by Reference Number (e.g., `MH-2024-XXXX`).
- **Application Details**: Displays application details including service code, submission date, status badge, applicant ID, and verification data (if available and authorized).
- **Application Timeline**: Renders a vertical timeline of all events associated with the application fetched from the backend.
- **Consent Management**: Displays a list of active and historical data-sharing consents. Allows the user to revoke active consents after confirmation.

## Routes Added/Changed
- `/citizen/applications`: Replaced placeholder with `CitizenApplicationsPage` (Application Tracker).
- `/citizen/applications/:id`: Replaced placeholder with `CitizenApplicationDetailsPage` (Details and Timeline).
- `/citizen/consents`: Replaced placeholder with `CitizenConsentsPage` (List and Revoke Consents).

## Components Created
- `frontend/src/pages/citizen/ApplicationsPage.tsx`
- `frontend/src/pages/citizen/ApplicationDetailsPage.tsx`
- `frontend/src/pages/citizen/ConsentsPage.tsx`

## Components Modified
- `frontend/src/App.tsx`: Updated routes to import the new real pages instead of placeholders.
- `frontend/src/types/service.ts`: Added `TimelineEventResponse` interface.

## API Functions Created
- `src/services/applicationService.ts`:
  - `getApplicationById(id)`: Calls `GET /api/v1/applications/{id}`
  - `getApplicationTimeline(id)`: Calls `GET /api/v1/applications/{id}/timeline`
- `src/services/consentService.ts`:
  - `getConsents()`: Calls `GET /api/v1/consents`
  - `revokeConsent(id)`: Calls `POST /api/v1/consents/{id}/revoke`

## Actual Backend Endpoints Consumed
- `GET /api/v1/applications/{id}` (application-service)
- `GET /api/v1/applications/{id}/timeline` (application-service)
- `GET /api/v1/consents` (security-workflow-service)
- `POST /api/v1/consents/{id}/revoke` (security-workflow-service)

## Backend Limitations Discovered
- **No Application Listing API**: The backend `ApplicationController` and `ApplicationRepository` do NOT have any endpoint or query to list all applications by a `citizenId` or for the current authenticated user. 
  - *What was requested*: An "Application List Page".
  - *What was done instead*: Built an "Application Tracker" where citizens must enter their specific Application Reference Number to view its details. Documented this limitation in the UI with a clear notice to the user.

## Important Architectural Decisions
- Removed `shadcn/ui` `Alert` component usage in the new pages to avoid complex component generation, relying instead on standard Tailwind CSS classes matching the civic design language.
- Maintained backend contracts faithfully. Did not add fake status transitions or mock applications on the frontend.

## Security Considerations
- The API clients automatically attach the Keycloak JWT.
- Did not implement frontend filtering of applications since there is no collection API to filter anyway.
- Ensure that the revocation action requires explicit user confirmation.

## Tests Added
- `src/__tests__/applications.test.tsx`: Tests for the tracker page, successful application detail loading, timeline rendering, and "not found" error handling.
- `src/__tests__/consents.test.tsx`: Tests for loading consents, displaying the empty state, listing consents, and confirming the revoke action.

## Test Results (VERIFIED BY RUNNING)
```
 Test Files  5 passed (5)
      Tests  30 passed (30)
```

## Build Result (VERIFIED BY RUNNING)
`npm run build` completed successfully. No errors.

## Lint Result (VERIFIED BY RUNNING)
`npm run lint` completed successfully with 0 errors (only 7 existing warnings from Phase 2).

## What was intentionally NOT implemented
- A full list of applications under "My Applications" (due to missing backend API).
- Any modifications to the backend Java code (frozen architecture rule).

## Known Limitations
- Citizens must know their exact Application Reference Number to track their application.

## What Phase 4 should do
- Implement the **Officer Workflow** experience.
- Officers should be able to view, verify, and approve/reject applications via the Camunda workflow.
- Replaces placeholders for `/officer` and `/officer/reviews/:taskId`.

## Recommended starting files/areas for Phase 4
- `frontend/src/pages/officer/Dashboard.tsx` (Current placeholders for officer reviews).
- `frontend/src/services/officerService.ts` (To be created for `/api/v1/officer` endpoints if they exist).
- Inspect backend `OfficerReviewController` to determine exact capabilities.
