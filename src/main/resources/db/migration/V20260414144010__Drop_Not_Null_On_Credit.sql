ALTER TABLE credit_purchases
    ALTER COLUMN price_per_credit DROP NOT NULL;

ALTER TABLE credit_purchases
    ALTER COLUMN user_id DROP NOT NULL;