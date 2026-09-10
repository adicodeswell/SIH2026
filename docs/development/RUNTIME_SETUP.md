# Local Development Runtime Setup

## Architecture Overview
The EKIKRIT backend consists of several microservices that communicate with each other and rely on common infrastructure components.

### Infrastructure
* **PostgreSQL:** Running on port `5432` internally (`5433` mapped to host). Contains the `mahasetu` database which serves both `application-service` and `security-workflow-service` via distinct Flyway schemas.
* **Keycloak:** Running on port `8080`. Used for authentication and authorization. It automatically imports the `mahasetu` realm on startup.

### Services
* **Application Service:** Running on port `8081`. Handles citizen applications and interfaces with Keycloak and the Security Workflow Service.
* **Security Workflow Service:** Running on port `8083`. Handles approvals, interoperability requests, and Camunda BPMN flows.
* **Interoperability Service:** Running on port `8082`.
* **Mock Systems:** Running on port `8091`.
* **Frontend:** Running on port `3000` via Nginx (and `5173` via Vite during local dev).

## Running the Stack
The authoritative configuration for the stack is `docker-compose.yml` in the root directory.

To start the full stack:
```bash
docker compose up -d --build
```

### Dependencies & Startup Order
1. `postgres` starts up.
2. `keycloak`, `mock-systems`, and `interoperability-service` start.
3. `application-service` and `security-workflow-service` start up *only after* `postgres` is `service_healthy`. They also depend on `keycloak` being started.

## Configuration Details
* **Database Connection:** Services use the local `postgres` container instead of remote NeonDB databases during local development. The environment variables `APP_DB_URL` and `WORKFLOW_DB_URL` are overridden in `docker-compose.yml`.
* **Keycloak Connection:** Services use `http://keycloak:8080` for internal backend-to-backend communication (e.g., token verification), while the frontend and issuer URIs use `http://localhost:8080`.
* **Frontend Proxy:** The Vite development server (`localhost:5173`) proxies `/api/v1/applications` to `http://127.0.0.1:8081` and `/api/v1/services` to `http://127.0.0.1:8081`.

## Troubleshooting
* **502 Bad Gateway (Frontend):** Ensure the `application-service` is fully healthy (`curl http://127.0.0.1:8081/actuator/health`). Check if it failed to start due to database or Keycloak connectivity issues.
* **Database Migration Errors:** Both services use Flyway. They share the `mahasetu` database but use separate schema history tables. If you encounter migration issues, you can clear the database volume and restart the `postgres` container.
