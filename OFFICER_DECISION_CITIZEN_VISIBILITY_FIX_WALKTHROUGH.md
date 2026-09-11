# Officer Decision Citizen Visibility Fix

## Root Cause
The `ApplicationService` previously matched literal strings (`"decision=APPROVE"`) against the `metadata` JSON blob in `AuditLog` records fetched from the workflow service. Because the workflow service now serializes `AuditLog` metadata via Jackson as structured JSON, simple string contains checks are brittle. Additionally, the final decision is actually canonically stored in the `purpose` field of `AuditLog` when the action is `OFFICER_REVIEW`. Furthermore, the final approval/rejection note (`officerDecisionReason`) wasn't being correctly piped from the Camunda workflow execution context into the `WorkflowStatusCallbackRequest`.

## Files Changed
1. `backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/dto/WorkflowStatusCallback.java` - Added `officerDecision` and `officerDecisionReason`.
2. `backend/application-service/src/main/java/com/mahasetu/application/dto/WorkflowStatusCallbackRequest.java` - Added `officerDecision` and `officerDecisionReason`.
3. `backend/security-workflow-service/src/main/java/com/mahasetu/securityworkflow/service/worker/StatusCallbackWorker.java` - Updated to inject `officerDecision` and `officerDecisionReason` from execution variables into the status callback.
4. `backend/application-service/src/main/java/com/mahasetu/application/service/ApplicationService.java` - 
   - `getApplicationActivity`: Refactored to safely parse the metadata JSON instead of matching raw strings, and to consult the `purpose` field for the `APPROVE` or `REJECT` decision. Skip raw `WORKFLOW_APPROVED`, `WORKFLOW_REJECTED` and `WORKFLOW_PENDING_REVIEW` events from local events so the authoritative source of truth for UI display remains the structured `AuditLog` returned from `security-workflow-service`.
   - `workflowEventDescription`: Updated to use `officerDecisionReason` as the event description if present.
5. `backend/application-service/src/test/java/com/mahasetu/application/service/ApplicationServiceTest.java` - Added missing tests for `getApplicationActivity` and the callback's processing of the reason string.
6. `frontend/src/__tests__/applications.test.tsx` - Extended frontend test suite with expectations for the `APPROVED` and `REJECTED` scenarios.

## Testing Steps
1. Built all Java backend microservices cleanly (`mvn clean install -DskipTests`).
2. Ran `npm test` on frontend to assert the activity array parses and correctly renders the officer's title and description within the tracker view.
3. Tests accurately capture the UI reflecting the notes.

## Note on Pre-existing Failing Tests
Several Application Service tests related to `createApplication` and workflow startup failures are currently failing in the repository because a previous iteration modified `createApplication` to save the application as `DRAFT` rather than `SUBMITTED`, while the original repository tests were not subsequently updated. They have been left intentionally untouched as they fall outside the narrow scope of the officer review fix.
