INSERT INTO departments (department_code, name, status)
VALUES
    ('DEPT-EDU', 'Education Department', 'ACTIVE'),
    ('DEPT-SKILL', 'Skill Development Department', 'ACTIVE')
ON CONFLICT (department_code) DO NOTHING;

INSERT INTO services (service_code, department_id, service_name, description, active, workflow_key)
VALUES
    ('SRV-EDU', 'DEPT-EDU', 'Education Verification', 'Demo education verification service', TRUE, 'application-orchestration'),
    ('SCHOLARSHIP', 'DEPT-EDU', 'Scholarship Verification', 'Demo scholarship eligibility verification', TRUE, 'application-orchestration'),
    ('SKILL_BENEFIT', 'DEPT-SKILL', 'Skill Benefit Verification', 'Demo skill benefit verification service', TRUE, 'application-orchestration')
ON CONFLICT (service_code) DO NOTHING;
