# Runtime Integration Fix — Security Workflow Service

## Issue Description
During frontend development against the backend stack, testing the Citizen Consent flow (`POST /api/v1/consents`) and the Officer Review flow (`GET /api/v1/officer/reviews`) returned `HTTP 502 Bad Gateway` from the Vite proxy (`localhost:5173`).

## Root Cause Analysis
1. The Vite proxy maps `/api/v1/consents` and `/api/v1/officer` to `http://localhost:8083`.
2. The container mapped to port 8083 is `security-workflow-service`.
3. Running `docker compose ps` revealed that `security-workflow-service` was constantly restarting and in a crash loop (`Exited (1)`).
4. Container logs showed the root cause: a **Flyway Migration Checksum Mismatch**.
   ```
   Migration checksum mismatch for migration version 1
   -> Applied to database : -458049409
   -> Resolved locally    : -1192420395
   ```
5. Further investigation revealed that the `application-service` and `security-workflow-service` share the **same PostgreSQL database** (`mahasetu`).
6. Because both services were configured to use Flyway's default history table name (`flyway_schema_history`), the `application-service`'s migrations (V1 to V6) were recorded in this table.
7. When `security-workflow-service` attempted to run its own `V1` and `V2` migrations, Flyway compared its local V1 checksum against the V1 checksum already stored by `application-service`, resulting in a fatal mismatch.
8. As a secondary issue, the `application-service`'s V1 migration had already created the `consents` and `audit_logs` tables using different constraints (e.g. `varchar(50)` instead of `varchar(255)` for `citizen_id` and foreign key constraints). When `security-workflow-service`'s V2 migration attempted to run, it needed to be idempotent. 

## Resolution

The resolution involved isolating the Flyway state between the services and aligning the Hibernate entities with the actual shared schema.

1. **Isolated Flyway History**:
   - Modified `backend/security-workflow-service/src/main/resources/application.yml`.
   - Set `spring.flyway.table: flyway_schema_history_workflow` to give the workflow service its own migration tracking table in the shared database.
   - Set `spring.flyway.baseline-version: 0` to ensure a clean start.

2. **Made Migrations Idempotent**:
   - Modified `V2__Add_Application_And_Service_To_Consent.sql`.
   - Used `ADD COLUMN IF NOT EXISTS` to ensure the migration doesn't fail if the columns already exist or if the script is re-run.

3. **Aligned Hibernate Entities with Actual Schema**:
   - The shared `mahasetu` database, initialized by `application-service`, defined `citizen_id` and `requesting_department_id` in the `consents` table, and `application_id` in the `audit_logs` table, as `VARCHAR(50)`.
   - The `Consent.java` and `AuditLog.java` entities were previously defaulting to `VARCHAR(255)`.
   - Added `length = 50` to the `@Column` annotations in these entities to prevent Hibernate validation errors (`ddl-auto: validate`).

## Result
- The `security-workflow-service` now starts up successfully (in ~19 seconds).
- `curl -s http://localhost:8083/actuator/health` returns `{"status":"UP"}`.
- The 502 Bad Gateway errors on the Vite frontend proxy are resolved.
- Backend tests pass successfully.
