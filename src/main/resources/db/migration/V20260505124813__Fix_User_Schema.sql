ALTER TABLE user_settings
    ALTER COLUMN created_at DROP NOT NULL;

ALTER TABLE company_onboarding_requests
    ALTER COLUMN payment_amount TYPE DECIMAL USING (payment_amount::DECIMAL);

ALTER TABLE user_settings
    ALTER COLUMN updated_at DROP NOT NULL;

CREATE INDEX idx_companies_code ON companies (company_code);

CREATE INDEX idx_user_settings_user ON user_settings (user_id);

CREATE INDEX idx_users_email ON users (email);

CREATE INDEX idx_users_username ON users (username);