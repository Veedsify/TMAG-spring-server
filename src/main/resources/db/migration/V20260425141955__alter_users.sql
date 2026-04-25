ALTER TABLE users
    DROP COLUMN doctor_application_status;

ALTER TABLE users
    DROP COLUMN medical_license_number;

ALTER TABLE users
    DROP COLUMN signature_url;

ALTER TABLE users
    DROP COLUMN stamp_url;

ALTER TABLE company_onboarding_requests
    ALTER COLUMN payment_amount TYPE DECIMAL USING (payment_amount::DECIMAL);