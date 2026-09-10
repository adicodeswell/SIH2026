ALTER TABLE consents ADD COLUMN IF NOT EXISTS application_id VARCHAR(255);
ALTER TABLE consents ADD COLUMN IF NOT EXISTS service_code VARCHAR(255);

-- Update existing records if any, then make them NOT NULL if necessary, but we can leave them nullable for existing rows to avoid breaking existing data.
