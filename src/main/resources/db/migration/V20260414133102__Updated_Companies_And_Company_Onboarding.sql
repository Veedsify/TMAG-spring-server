ALTER TABLE companies
    DROP CONSTRAINT fk_companies_on_active_plan;

ALTER TABLE company_onboarding_requests
    ADD credit_count INTEGER;

ALTER TABLE company_onboarding_requests
    ALTER COLUMN credit_count SET NOT NULL;

DROP TABLE company_plans CASCADE;

ALTER TABLE companies
    DROP COLUMN active_plan_id;