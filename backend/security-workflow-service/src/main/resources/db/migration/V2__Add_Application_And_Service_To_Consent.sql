ALTER TABLE consents ADD COLUMN application_id VARCHAR(255);
ALTER TABLE consents ADD COLUMN service_code VARCHAR(255);

-- Update existing records if any, then make them NOT NULL if necessary, but we can leave them nullable for existing rows to avoid breaking existing data.
