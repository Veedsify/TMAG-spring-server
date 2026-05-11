ALTER TABLE user_credit_plans
ADD COLUMN IF NOT EXISTS is_family_plan BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS included_family_members INTEGER,
ADD COLUMN IF NOT EXISTS additional_member_price_usd DECIMAL(10, 2),
ADD COLUMN IF NOT EXISTS additional_member_price_ngn DECIMAL(12, 2);
