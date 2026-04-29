ALTER TABLE company_onboarding_requests
    ADD COLUMN IF NOT EXISTS team_members_csv_file_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS team_members_csv_path VARCHAR(500),
    ADD COLUMN IF NOT EXISTS team_members_csv_content_type VARCHAR(100);
