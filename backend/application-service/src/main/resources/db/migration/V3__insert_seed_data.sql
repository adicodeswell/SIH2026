-- Seed Departments
INSERT INTO departments (department_code, name, status, created_at) VALUES 
('DEPT_EDU', 'Department of Higher Education', 'ACTIVE', CURRENT_TIMESTAMP),
('DEPT_HLTH', 'Ministry of Health and Family Welfare', 'ACTIVE', CURRENT_TIMESTAMP),
('DEPT_EMP', 'Labor and Employment Ministry', 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT (department_code) DO NOTHING;

-- Seed Citizens
INSERT INTO citizens (citizen_id, name, date_of_birth, mobile, email, created_at, updated_at) VALUES 
('MH1001', 'Rahul Patil', '2000-01-01', '+91-9876543210', 'rahul.patil@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MH2002', 'Priya Singh', '1998-05-15', '+91-8765432109', 'priya.singh@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MH3003', 'Amit Sharma', '2002-11-20', '+91-7654321098', 'amit.sharma@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MH1002', 'Aditya Jha', '2006-07-30', '+91-9821709422', 'adicodeswell@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MH1003', 'Ankit Kumar', '2006-01-16', '+91-9431296664', 'ankitmayank829@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (citizen_id) DO NOTHING;

-- Seed Services (Schemes)
INSERT INTO services (service_code, department_id, service_name, description, active, workflow_key, created_at, updated_at) VALUES 
('SKILL_BENEFIT', 'DEPT_EMP', 'State Youth Tech Scholarship', 'Financial assistance for unemployed youth pursuing technical degrees.', true, 'application-orchestration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SCHOLARSHIP', 'DEPT_EDU', 'Higher Education Merit Scholarship', 'Full tuition coverage for students with exceptional academic records.', true, 'application-orchestration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SRV-EDU', 'DEPT_HLTH', 'Senior Citizen Health Benefit', 'Complete healthcare coverage scheme for citizens above 60 years.', true, 'application-orchestration', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (service_code) DO NOTHING;
