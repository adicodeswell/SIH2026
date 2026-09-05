# MahaSetu - SIH Problem Statement 26129

**MahaSetu** is an e-governance interoperability and orchestration platform designed for the Government of Maharashtra to unify fragmented legacy department systems, automate cross-departmental workflow orchestration, and provide a seamless Citizen and Officer portal.

> [!TIP]
> **Enterprise Roadmap**: For the complete blueprint to transition this prototype to a state-scale production platform, see [INDUSTRY_READINESS_ROADMAP.md](file:///home/babu/SIH/SIH2026/docs/INDUSTRY_READINESS_ROADMAP.md).

---

## Architecture & Services

| Service | Port | Description |
| :--- | :--- | :--- |
| **Frontend Portal** | `3000` | Responsive SPA for Citizen & Officer portals |
| **Keycloak IAM** | `8080` | OAuth2 / OpenID Connect Identity Provider (Realm: `mahasetu`) |
| **Application Service** | `8081` | Core service catalog and citizen application management |
| **Interoperability Service** | `8082` | Legacy department adapter & protocol normalizer (REST / SOAP / CSV) |
| **Security & Workflow Service** | `8083` | Camunda BPMN workflow orchestration, consent manager, officer review |
| **Mock Legacy Systems** | `8091` | Emulates Employment (REST JSON), Health (SOAP XML), Education (FTP CSV) |
| **PostgreSQL Database** | `5432` / `5433` | Database storage for application and workflow state |

---

## Seed Accounts & Credentials

- **Keycloak Admin Console**: `http://localhost:8080`
  - Username: `admin` | Password: `admin`
- **Citizen Account**:
  - Username / ID: `MH1001` | Password: `citizen123`
- **Officer Account**:
  - Username / ID: `officer_123` | Password: `officer123`

---

## Quick Start (Docker - Recommended)

### 1. Start All Services
```bash
./scripts/start.sh
```
*Or manually:*
```bash
docker compose up -d --build
```

### 2. Access the Applications
- **Citizen Portal**: [http://localhost:3000/citizen/](http://localhost:3000/citizen/)
- **Officer Review Portal**: [http://localhost:3000/officer/](http://localhost:3000/officer/)
- **Keycloak IAM**: [http://localhost:8080](http://localhost:8080)

### 3. View Logs
```bash
docker compose logs -f
```

### 4. Stop All Services
```bash
./scripts/stop.sh
# or: docker compose down
```

---

## Instant Demo Mode (Zero Backend Setup)

If you wish to quickly inspect the UI without running the backend containers:

```bash
cd frontend
python3 -m http.server 3000
```
Then navigate to:
- **Citizen Demo UI**: [http://localhost:3000/citizen/?demo=true](http://localhost:3000/citizen/?demo=true)
- **Officer Demo UI**: [http://localhost:3000/officer/?demo=true](http://localhost:3000/officer/?demo=true)

---

## Local Development (Without Docker Compose)

### 1. Database & Keycloak
Ensure PostgreSQL is running on port 5432 with database `mahasetu` (`postgres:postgres`).
Start Keycloak:
```bash
docker run -d --name mahasetu-keycloak -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -v $(pwd)/infrastructure/keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json:ro \
  quay.io/keycloak/keycloak:24.0 start-dev --import-realm
```

### 2. Build Backend Services
```bash
mvn clean install -DskipTests
```

### 3. Run Microservices
In separate terminal tabs:
- **Mock Systems**: `cd backend/mock-systems && mvn spring-boot:run`
- **Interoperability Service**: `cd backend/interoperability-service && mvn spring-boot:run`
- **Application Service**: `cd backend/application-service && mvn spring-boot:run`
- **Security Workflow Service**: `cd backend/security-workflow-service && mvn spring-boot:run`
- **Frontend**: `cd frontend && python3 -m http.server 3000`
