ALTER TABLE user_settings
    ALTER COLUMN created_at DROP NOT NULL;

ALTER TABLE company_onboarding_requests
    ALTER COLUMN payment_amount TYPE DECIMAL USING (payment_amount::DECIMAL);

ALTER TABLE user_settings
    ALTER COLUMN updated_at DROP NOT NULL;
