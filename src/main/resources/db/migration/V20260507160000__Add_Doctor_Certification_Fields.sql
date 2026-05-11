-- Add practicing license and travel medicine certificate URL fields
-- to both doctor_applications and user_settings tables.

ALTER TABLE doctor_applications
    ADD COLUMN practicing_license_url VARCHAR(1024);

ALTER TABLE doctor_applications
    ADD COLUMN travel_medicine_certificate_url VARCHAR(1024);

ALTER TABLE user_settings
    ADD COLUMN practicing_license_url VARCHAR(1024);

ALTER TABLE user_settings
    ADD COLUMN travel_medicine_certificate_url VARCHAR(1024);
