ALTER TABLE family_trips
ADD COLUMN base_fiat_cost BIGINT NOT NULL DEFAULT 0,
ADD COLUMN extra_fiat_cost BIGINT NOT NULL DEFAULT 0,
ADD COLUMN total_fiat_cost BIGINT NOT NULL DEFAULT 0,
ADD COLUMN currency VARCHAR(10) NOT NULL DEFAULT 'NGN';

-- Drop the old credit cost columns
ALTER TABLE family_trips
DROP COLUMN base_credit_cost,
DROP COLUMN total_credit_cost;
