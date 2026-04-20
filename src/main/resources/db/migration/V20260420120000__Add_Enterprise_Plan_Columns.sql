-- Add NGN pricing and enterprise-tier metadata columns
ALTER TABLE user_credit_plans
    ADD COLUMN base_price_ngn    DECIMAL(12, 2),
    ADD COLUMN is_company_plan   BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN signup_range_label VARCHAR(20),
    ADD COLUMN service_level      VARCHAR(20);

-- Back-fill NGN prices for existing individual plans
UPDATE user_credit_plans SET base_price_ngn = 0.00      WHERE code = 'ESSENTIAL';
UPDATE user_credit_plans SET base_price_ngn = 50000.00  WHERE code = 'STANDARD';
UPDATE user_credit_plans SET base_price_ngn = 100000.00 WHERE code = 'PREMIUM';

-- Insert the 6 enterprise company plans
INSERT INTO user_credit_plans
    (code, display_name, base_price_usd, base_price_ngn, description, is_default, is_company_plan, signup_range_label, service_level, created_at, updated_at)
VALUES
    ('ENTERPRISE_SILVER',
     'Enterprise Silver',
     50.00, 50000.00,
     'Fully personalised travel health report across 14 clinical decision trees. Ideal for teams of up to 100.',
     FALSE, TRUE, '0-100', 'STANDARD', NOW(), NOW()),

    ('ENTERPRISE_PLUS',
     'Enterprise Plus',
     100.00, 100000.00,
     'Everything in Standard plus Pre-Travel Checklist, Medication Packing List, and Doctor-Ready Clinical Summary Letter. For teams up to 100.',
     FALSE, TRUE, '0-100', 'PREMIUM', NOW(), NOW()),

    ('ENTERPRISE_GOLD',
     'Enterprise Gold',
     50.00, 50000.00,
     'Fully personalised travel health report across 14 clinical decision trees. Built for mid-size teams of 100–500.',
     FALSE, TRUE, '100-500', 'STANDARD', NOW(), NOW()),

    ('ENTERPRISE_ELITE',
     'Enterprise Elite',
     100.00, 100000.00,
     'Everything in Standard plus Pre-Travel Checklist, Medication Packing List, and Doctor-Ready Clinical Summary Letter. For teams of 100–500.',
     FALSE, TRUE, '100-500', 'PREMIUM', NOW(), NOW()),

    ('ENTERPRISE_PLATINUM',
     'Enterprise Platinum',
     50.00, 50000.00,
     'Fully personalised travel health report across 14 clinical decision trees. Designed for large organisations with 500+ members.',
     FALSE, TRUE, '>500', 'STANDARD', NOW(), NOW()),

    ('ENTERPRISE_SIGNATURE',
     'Enterprise Signature',
     100.00, 100000.00,
     'Everything in Standard plus Pre-Travel Checklist, Medication Packing List, and Doctor-Ready Clinical Summary Letter. For 500+ member organisations.',
     FALSE, TRUE, '>500', 'PREMIUM', NOW(), NOW());
