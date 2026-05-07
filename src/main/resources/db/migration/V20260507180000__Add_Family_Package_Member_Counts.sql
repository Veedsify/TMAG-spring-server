ALTER TABLE family_package_purchases
    ADD COLUMN IF NOT EXISTS additional_members INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_members INT NOT NULL DEFAULT 6;
