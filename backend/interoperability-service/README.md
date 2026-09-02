# MahaSetu: Interoperability Service

## Overview
The Interoperability Service is the core backend component for MahaSetu (SIH 2026). Its primary responsibility is to fetch raw, unstructured legacy data from various Maharashtra government departments (Employment, Health, Education, etc.) and securely transform it into a single standardized schema (`CanonicalCitizenData`).

This guarantees that the MahaSetu frontend always receives clean, predictable JSON regardless of how messy the external legacy system is.

## 🏗️ Architecture & Data Flow (Phases 1 & 3)

When a request is sent to fetch data for a citizen (e.g., `MH1001`), the following flow executes:

1. **Client Request:** The frontend makes a request to `VerificationController.java` at `/api/v1/interop/fetch/{system}/{citizenId}`.
2. **Dynamic Routing:** The controller passes the request to the `ConnectorRegistry.java`.
3. **Data Fetching:** The registry identifies the correct system (e.g., `HEALTH_SYSTEM_CONNECTOR`) and delegates the task to the specific protocol connector (`RestConnector`, `SoapConnector`, or `CsvConnector`). 
4. **Raw Wrapping:** The mock server responds with raw data (JSON, XML, or CSV). The connector deliberately avoids parsing this, instead returning it inside a `RawExternalResponse`.
5. **Transformation:** The registry grabs the `RawExternalResponse` and passes it to the corresponding department transformer (e.g., `HealthTransformer.java`). The transformer parses the raw string using JSON, Regex/XML, or CSV splitting and maps it exactly to the `CanonicalCitizenData` standard.
6. **Unified Response:** The standardized `CanonicalCitizenData` object is returned to the controller, and ultimately back to the frontend.

---

## 📁 Codebase Directory Structure

### `controller/`
* **`VerificationController.java`**: The public-facing REST API for the frontend. It intercepts HTTP requests and forwards them to the registry.

### `service/`
* **`ConnectorRegistry.java`**: Acts as a traffic cop. It uses dependency injection (Maps of beans) to dynamically find the correct `GovernmentSystemConnector` and `DataTransformer` for the requested department.

### `connector/`
* **`GovernmentSystemConnector.java`**: An interface abstracting all system connectors. Ensures our architecture conforms to the Dependency Inversion Principle.
* **`RestConnector.java`**: Fetches standard JSON REST payloads (Used for the Employment System).
* **`SoapConnector.java`**: Fetches legacy SOAP/XML payloads (Used for the Health System).
* **`CsvConnector.java`**: Fetches raw CSV text dumps (Used for the Education System).

### `transformer/`
* **`DataTransformer.java`**: An interface abstracting the data mapping logic.
* **`EmploymentTransformer.java`**: Parses the messy Employment JSON (e.g., `cit_id`, `emp_status`) into clean Java fields.
* **`HealthTransformer.java`**: Uses Regex/XML parsing to extract fields from ancient `<soapenv:Envelope>` SOAP responses.
* **`EducationTransformer.java`**: Uses string splitting to extract rows and columns from raw comma-separated CSV dumps.

### `model/`
* **`CanonicalCitizenData.java`**: The single source of truth schema.
* **`ExternalSystem.java`**: An Enum containing the list of supported government departments.
* **`RawExternalResponse.java`**: A container object holding the unparsed JSON/XML strings returned by the connectors.
