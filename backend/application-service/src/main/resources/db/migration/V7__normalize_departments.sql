-- Create canonical DEPT-SKILLS if it doesn't exist
INSERT INTO departments (department_code, name, status, created_at) 
VALUES ('DEPT-SKILLS', 'Department of Skills and Employment', 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT (department_code) DO NOTHING;

-- Normalize SKILL_BENEFIT to DEPT-SKILLS
UPDATE services SET department_id = 'DEPT-SKILLS' WHERE service_code = 'SKILL_BENEFIT';

-- Normalize EDU services to DEPT-EDU
UPDATE services SET department_id = 'DEPT-EDU' WHERE service_code IN ('SCHOLARSHIP', 'SRV-EDU');
