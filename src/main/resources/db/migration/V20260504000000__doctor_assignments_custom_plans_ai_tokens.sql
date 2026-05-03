ALTER TABLE users
    ADD COLUMN IF NOT EXISTS bio TEXT;

ALTER TABLE user_credit_plans
    ALTER COLUMN code TYPE VARCHAR(100);

ALTER TABLE user_credit_plans
    ADD COLUMN IF NOT EXISTS visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    ADD COLUMN IF NOT EXISTS assigned_company_id BIGINT,
    ADD COLUMN IF NOT EXISTS plan_count INTEGER;

ALTER TABLE user_credit_plans
    DROP CONSTRAINT IF EXISTS user_credit_plans_code_key;

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_credit_plans_public_code
    ON user_credit_plans(code)
    WHERE deleted_at IS NULL AND visibility = 'PUBLIC';

CREATE INDEX IF NOT EXISTS idx_user_credit_plans_company_visibility
    ON user_credit_plans(assigned_company_id, visibility)
    WHERE deleted_at IS NULL;

ALTER TABLE generated_plans
    ADD COLUMN IF NOT EXISTS plan_generation_tokens_used INTEGER,
    ADD COLUMN IF NOT EXISTS summary_generation_tokens_used INTEGER;

ALTER TABLE ai_request_logs
    ADD COLUMN IF NOT EXISTS plan_generation_tokens_used INTEGER,
    ADD COLUMN IF NOT EXISTS summary_generation_tokens_used INTEGER;

CREATE TABLE IF NOT EXISTS doctor_applications (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    medical_license_number VARCHAR(255) NOT NULL,
    bio TEXT,
    profile_picture_url VARCHAR(500),
    signature_url VARCHAR(500) NOT NULL,
    stamp_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMP,
    created_user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_doctor_applications_status_created
    ON doctor_applications(status, created_at)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_doctor_applications_email
    ON doctor_applications(email)
    WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS travel_plan_doctor_assignments (
    id BIGSERIAL PRIMARY KEY,
    travel_plan_id BIGINT NOT NULL REFERENCES travel_plans(id),
    doctor_id BIGINT NOT NULL REFERENCES users(id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_travel_plan_doctor_assignment UNIQUE (travel_plan_id, doctor_id)
);

CREATE INDEX IF NOT EXISTS idx_travel_plan_doctor_assignments_plan
    ON travel_plan_doctor_assignments(travel_plan_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_travel_plan_doctor_assignments_doctor
    ON travel_plan_doctor_assignments(doctor_id)
    WHERE deleted_at IS NULL;
