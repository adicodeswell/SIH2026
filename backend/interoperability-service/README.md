# MahaSetu: Interoperability Service

## Overview
The Interoperability Service is the core backend component for MahaSetu (SIH 2026). Its primary responsibility is to fetch raw, unstructured legacy data from various Maharashtra government departments (Employment, Health, Education, etc.) and securely transform it into a single standardized schema (`CanonicalCitizenData`).

This guarantees that the MahaSetu frontend always receives clean, predictable JSON regardless of how messy the external legacy system is.

## 🏗️ Architecture & Data Flow

When a request is sent to fetch data for a citizen (e.g., `MH1001`), the following flow executes:

1. **Client Request:** The frontend makes a request to `VerificationController.java` at `/api/v1/interop/fetch/{system}/{citizenId}`.
2. **Dynamic Routing:** The controller passes the request to the `ConnectorRegistry.java`.
3. **Data Fetching:** The registry identifies the correct system (e.g., `EMPLOYMENT_SYSTEM_REST_CONNECTOR`) and delegates the task to `RestConnector.java`. The connector executes an HTTP request to the external government mock server.
4. **Raw Wrapping:** The mock server responds with raw JSON. The `RestConnector` deliberately avoids parsing this, instead returning it inside a `RawExternalResponse`.
5. **Transformation:** The registry grabs the `RawExternalResponse` and passes it to the `EmploymentTransformer.java`. The transformer parses the raw JSON using Jackson and maps it exactly to the `CanonicalCitizenData` standard.
6. **Unified Response:** The standardized `CanonicalCitizenData` object is returned to the controller, and ultimately back to the frontend.

---

## 📁 Codebase Directory Structure

### `controller/`
* **`VerificationController.java`**: The public-facing REST API for the frontend. It intercepts HTTP requests and forwards them to the registry.

### `service/`
* **`ConnectorRegistry.java`**: Acts as a traffic cop. It uses dependency injection (Maps of beans) to dynamically find the correct `GovernmentSystemConnector` and `DataTransformer` for the requested department.

### `connector/`
* **`GovernmentSystemConnector.java`**: An interface abstracting all system connectors. Ensures our architecture conforms to the Dependency Inversion Principle.
* **`RestConnector.java`**: Implements the above interface specifically for modern REST APIs. Currently hardcoded to target our mock employment system on port 8091.
* **`SoapConnector.java` & `CsvConnector.java`**: Placeholders for Phase 2 integration of legacy SOAP protocols and SFTP CSV drops.

### `transformer/`
* **`DataTransformer.java`**: An interface abstracting the data mapping logic.
* **`EmploymentTransformer.java`**: Parses the messy Employment JSON (e.g., `cit_id`, `emp_status`) into clean Java fields (`citizenId`, `employmentStatus`).

### `model/`
* **`CanonicalCitizenData.java`**: The single source of truth schema.
* **`ExternalSystem.java`**: An Enum containing the list of supported government departments.
* **`RawExternalResponse.java`**: A container object holding the unparsed JSON/XML strings returned by the connectors.
