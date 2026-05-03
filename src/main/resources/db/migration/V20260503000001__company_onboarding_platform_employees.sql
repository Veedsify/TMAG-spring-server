ALTER TABLE company_onboarding_requests
    ADD COLUMN IF NOT EXISTS platform_employees TEXT DEFAULT '[]';
