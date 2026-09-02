# MahaSetu: Mock Systems

## Overview
The Mock Systems service is a dedicated Spring Boot application designed to simulate the various outdated, messy, and disorganized legacy systems used by the Government of Maharashtra.

Since we cannot connect to real government databases during the SIH Hackathon, this service provides the raw data endpoints that our `interoperability-service` must ingest, parse, and standardize.

## 🏗️ Architecture & Protocols Simulated
This single Spring Boot app currently runs on **Port 8091** and exposes 3 distinct API flavors to prove our system can handle anything.

### 1. The Employment Department (Modern REST)
* **Endpoint:** `GET /employment/{citizenId}`
* **Format:** JSON
* **Description:** Simulates a somewhat modern legacy system. It returns standard JSON but uses non-standard field names (like `cit_id` instead of `citizenId`).

### 2. The Health Department (Legacy SOAP)
* **Endpoint:** `GET /health/{citizenId}`
* **Format:** XML
* **Description:** Simulates an ancient SOAP web service from 2005. It returns a verbose, messy XML `<soapenv:Envelope>` wrapper. Our `interoperability-service` will eventually need a `SoapConnector.java` to parse this.

### 3. The Education Department (CSV/FTP Drop)
* **Endpoint:** `GET /education/{citizenId}`
* **Format:** text/csv
* **Description:** Simulates a system that doesn't even have a real API, but instead just drops raw CSV files onto an FTP server. It returns a raw comma-separated string.

---

## 🚀 How to Run & Test
The service is fully operational. To start it, run:
```bash
cd backend/mock-systems
mvn clean install
java -jar target/mock-systems-1.0.0-SNAPSHOT.jar
```

You can test the endpoints manually using cURL:
```bash
curl http://localhost:8091/employment/MH1001
curl http://localhost:8091/health/MH1001
curl http://localhost:8091/education/MH1001
```
