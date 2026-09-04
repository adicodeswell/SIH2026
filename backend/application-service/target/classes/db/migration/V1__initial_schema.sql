CREATE TABLE citizens (
    citizen_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    mobile VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE departments (
    department_code VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE services (
    service_code VARCHAR(100) PRIMARY KEY,
    department_id VARCHAR(50) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    workflow_key VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_services_department FOREIGN KEY (department_id) REFERENCES departments(department_code)
);

CREATE TABLE applications (
    application_number VARCHAR(50) PRIMARY KEY,
    citizen_id VARCHAR(50) NOT NULL,
    service_id VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    submitted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_applications_citizen FOREIGN KEY (citizen_id) REFERENCES citizens(citizen_id),
    CONSTRAINT fk_applications_service FOREIGN KEY (service_id) REFERENCES services(service_code)
);

CREATE TABLE application_events (
    id UUID PRIMARY KEY,
    application_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30),
    description TEXT,
    performed_by VARCHAR(255),
    occurred_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_app_events_application FOREIGN KEY (application_id) REFERENCES applications(application_number)
);

CREATE TABLE workflow_instances (
    id UUID PRIMARY KEY,
    application_id VARCHAR(50) NOT NULL UNIQUE,
    workflow_key VARCHAR(255) NOT NULL,
    external_instance_id VARCHAR(255),
    current_step VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_wf_instances_application FOREIGN KEY (application_id) REFERENCES applications(application_number)
);

CREATE TABLE consents (
    id UUID PRIMARY KEY,
    citizen_id VARCHAR(50) NOT NULL,
    requesting_department_id VARCHAR(50) NOT NULL,
    data_scope VARCHAR(255) NOT NULL,
    purpose VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    granted_at TIMESTAMP,
    expires_at TIMESTAMP,
    CONSTRAINT fk_consents_citizen FOREIGN KEY (citizen_id) REFERENCES citizens(citizen_id),
    CONSTRAINT fk_consents_department FOREIGN KEY (requesting_department_id) REFERENCES departments(department_code)
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    application_id VARCHAR(50),
    actor_id VARCHAR(255),
    action VARCHAR(255) NOT NULL,
    resource_type VARCHAR(255) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    purpose TEXT,
    occurred_at TIMESTAMP NOT NULL,
    metadata TEXT,
    CONSTRAINT fk_audit_logs_application FOREIGN KEY (application_id) REFERENCES applications(application_number)
);
