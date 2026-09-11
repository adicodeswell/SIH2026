INSERT INTO citizens (citizen_id, name, date_of_birth, mobile, email, created_at, updated_at) VALUES 
('MH1002', 'Aditya Jha', '2006-07-30', '+91-9821709422', 'adicodeswell@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MH1003', 'Ankit Kumar', '2006-01-16', '+91-9431296664', 'ankitmayank829@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (citizen_id) DO NOTHING;
