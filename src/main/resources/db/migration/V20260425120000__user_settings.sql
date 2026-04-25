-- Create user_settings table
CREATE TABLE user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    medical_license_number VARCHAR(255),
    signature_url VARCHAR(500),
    stamp_url VARCHAR(500),
    doctor_application_status VARCHAR(50) DEFAULT 'NONE',
    consent_version INTEGER DEFAULT 1,
    consent_accepted_at TIMESTAMP,
    consent_accepted_by_version INTEGER,
    consent_ip VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

-- Migrate doctor data from users table
INSERT INTO user_settings (user_id, medical_license_number, signature_url, stamp_url,
    doctor_application_status, consent_version, created_at, updated_at)
SELECT id, medical_license_number, signature_url, stamp_url,
    doctor_application_status, 1, NOW(), NOW()
FROM users
WHERE medical_license_number IS NOT NULL
   OR signature_url IS NOT NULL
   OR stamp_url IS NOT NULL
   OR (doctor_application_status IS NOT NULL AND doctor_application_status != 'NONE');

-- Create settings for all other users (defaults)
INSERT INTO user_settings (user_id, consent_version, created_at, updated_at)
SELECT id, 1, NOW(), NOW()
FROM users
WHERE id NOT IN (SELECT user_id FROM user_settings);

-- Indexes
CREATE INDEX idx_user_settings_consent_version
    ON user_settings(consent_accepted_by_version) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_settings_doctor_status
    ON user_settings(doctor_application_status) WHERE deleted_at IS NULL;
