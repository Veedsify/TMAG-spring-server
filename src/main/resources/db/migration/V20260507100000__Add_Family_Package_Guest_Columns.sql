ALTER TABLE family_package_purchases
    ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE family_package_purchases
    ADD COLUMN IF NOT EXISTS guest_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS guest_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS guest_phone VARCHAR(50);
