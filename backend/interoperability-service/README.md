# MahaSetu: Interoperability Service

## Overview
The Interoperability Service is the core backend component for MahaSetu (SIH 2026). Its primary responsibility is to fetch raw, unstructured legacy data from various Maharashtra government departments (Employment, Health, Education, etc.) and securely transform it into a single standardized schema (`CanonicalCitizenData`).

This guarantees that the MahaSetu frontend always receives clean, predictable JSON regardless of how messy the external legacy system is.

## 🏗️ Architecture & Data Flow (Phases 1-5 Complete)

When a request is sent to fetch data for a citizen (e.g., `MH1001`), the following flow executes:

1. **Security & Authentication (Phase 5):** The request passes through the `Spring Security OAuth2 Resource Server`. It strictly requires an `Authorization: Bearer <Keycloak-Service-Token>` header to prevent unauthenticated scraping of citizen data.
2. **Client Request:** The frontend requests data at `/api/v1/interop/fetch/{system}/{citizenId}` (for a single department) or `/api/v1/interop/fetch/all/{citizenId}` (which uses Java's `CompletableFuture` to fetch from all three departments asynchronously in parallel).
3. **Caching (Phase 4):** The `ConnectorRegistry` checks its in-memory Caffeine Cache (`@Cacheable`). If the citizen's data was fetched within the last 10 minutes, it returns immediately without hitting the external system!
4. **Dynamic Routing & Circuit Breaking (Phase 4):** The registry identifies the correct system connector. If the external legacy system crashes or times out, our **Resilience4j Circuit Breaker** intercepts the failure and returns a graceful "SERVICE_UNAVAILABLE" fallback model instead of crashing MahaSetu.
5. **Data Fetching (Phases 1 & 3):** The connector (`RestConnector`, `SoapConnector`, or `CsvConnector`) executes the HTTP request to the external server and returns the unparsed payload wrapped in a `RawExternalResponse`.
6. **Transformation:** The department transformer (`EmploymentTransformer`, `HealthTransformer`, `EducationTransformer`) parses the raw JSON, XML, or CSV and maps it perfectly into the `CanonicalCitizenData` standard.
7. **Unified Response:** The clean `CanonicalCitizenData` object is returned to the frontend.

---

## 📁 Codebase Directory Structure

### `controller/`
* **`VerificationController.java`**: The public-facing REST API for the frontend, supporting single and parallel async fetching.

### `security/`
* **`Spring Security OAuth2 Resource Server.java`**: A strict servlet filter blocking requests without the correct Bearer token.

### `service/`
* **`ConnectorRegistry.java`**: Acts as a traffic cop and shield. Powered by Spring Cache and Resilience4j to dynamically route requests, cache data, and break circuits if legacy systems crash.

### `connector/`
* **`GovernmentSystemConnector.java`**: An interface abstracting all system connectors.
* **`RestConnector.java`**: Fetches standard JSON REST payloads.
* **`SoapConnector.java`**: Fetches legacy SOAP/XML payloads.
* **`CsvConnector.java`**: Fetches raw CSV text dumps.

### `transformer/`
* **`DataTransformer.java`**: An interface abstracting the data mapping logic.
* **`EmploymentTransformer.java`**: Parses Employment JSON into clean Java fields.
* **`HealthTransformer.java`**: Uses Regex/XML parsing for ancient `<soapenv:Envelope>` SOAP responses.
* **`EducationTransformer.java`**: Uses string splitting for CSV dumps.

### `model/`
* **`CanonicalCitizenData.java`**: The single source of truth schema.
* **`ExternalSystem.java`**: Supported government departments enum.
* **`RawExternalResponse.java`**: A container object for unparsed data.

---

## 🚀 How to Run the Complete Stack (Phase 5)

Because we have Dockerized the entire application, running it for the judges is incredibly simple.

**Option 1: Docker Compose (Recommended)**
Navigate to the root `backend` directory (where `docker-compose.yml` lives) and run:
```bash
docker-compose up --build
```
This single command spins up BOTH the `mock-systems` (Port 8091) and the `interoperability-service` (Port 8082).

**Option 2: Manual Maven Build**
1. Open Terminal 1: `cd mock-systems && mvn clean install && java -jar target/mock-systems-1.0.0-SNAPSHOT.jar`
2. Open Terminal 2: `cd interoperability-service && mvn clean install && java -jar target/interoperability-service-1.0.0-SNAPSHOT.jar`

### Example Test (cURL)
*Note: Because of Phase 5 security, you MUST pass the authentication header!*

**Fetch Single System:**
```bash
curl -H "Authorization: Bearer <Keycloak-Service-Token>" http://localhost:8082/api/v1/interop/fetch/EMPLOYMENT_SYSTEM/MH1001
```

**Fetch All Systems (Parallel Async):**
```bash
curl -H "Authorization: Bearer <Keycloak-Service-Token>" http://localhost:8082/api/v1/interop/fetch/all/MH1001
```
