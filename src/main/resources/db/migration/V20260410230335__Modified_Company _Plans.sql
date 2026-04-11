ALTER TABLE company_onboarding_requests
    DROP CONSTRAINT company_onboarding_requests_created_company_id_fkey;

DROP TABLE company_onboarding_requests CASCADE;

ALTER TABLE company_plans
    DROP COLUMN price_eur;

ALTER TABLE company_plans
    DROP COLUMN price_gbp;

ALTER TABLE company_plans
    DROP COLUMN price_ngn;

ALTER TABLE company_plans
    DROP COLUMN price_usd;