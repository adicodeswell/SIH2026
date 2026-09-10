# Citizen Application Lifecycle

## Overview
The Ekikrit platform enforces a strict invariant: **Workflow cannot start before cryptographic consent is granted.**

## 1. Application Creation
* **Endpoint**: `POST /api/v1/applications`
* **State**: `DRAFT`
* **Action**: Citizen creates a pre-submission draft application containing basic details. The backend generates an `applicationNumber` and saves it. The Camunda workflow is **not** started at this stage.

## 2. Cryptographic Consent
* **Endpoint**: `POST /api/v1/consents`
* **State**: `GRANTED`
* **Action**: Citizen explicitly grants data access consent bound directly to the `applicationNumber`. This consent is securely recorded in the Security Workflow service database.

## 3. Application Submission
* **Endpoint**: `POST /api/v1/applications/{applicationNumber}/submit`
* **State**: `SUBMITTED`
* **Action**: 
  1. The Application Service verifies the citizen owns the application.
  2. The Application Service calls the Security Workflow Service (`/internal/v1/consents/check`) to strictly verify that a `GRANTED` consent exists for this exact application.
  3. If consent is verified, the application state transitions from `DRAFT` to `SUBMITTED`.
  4. The Application Service triggers the start of the Camunda workflow.

## 4. Workflow Orchestration
* **Action**: The Camunda engine orchestrates the actual background verification and interoperability requests. The workflow engine will additionally re-verify consent tokens as needed during interactions with external endpoints.

