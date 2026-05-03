ALTER TABLE doctor_applications
    ALTER COLUMN medical_license_url DROP NOT NULL,
    ALTER COLUMN identity_document_url DROP NOT NULL,
    ALTER COLUMN cv_or_profile_url DROP NOT NULL;
