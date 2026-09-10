CREATE TABLE IF NOT EXISTS consents (
    id UUID PRIMARY KEY,
    citizen_id VARCHAR(255) NOT NULL,
    requesting_department_id VARCHAR(255) NOT NULL,
    data_scope VARCHAR(255) NOT NULL,
    purpose VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    granted_at TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    application_id VARCHAR(255),
    action VARCHAR(255) NOT NULL,
    actor_id VARCHAR(255),
    resource_type VARCHAR(255) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    purpose TEXT,
    metadata TEXT,
    occurred_at TIMESTAMP NOT NULL
);
