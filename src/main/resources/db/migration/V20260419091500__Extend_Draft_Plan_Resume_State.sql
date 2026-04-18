ALTER TABLE draft_plans
    ADD COLUMN show_verify BOOLEAN DEFAULT FALSE;

ALTER TABLE draft_plans
    ADD COLUMN show_intro BOOLEAN DEFAULT TRUE;

ALTER TABLE draft_plans
    ADD COLUMN risk_consent_given BOOLEAN DEFAULT FALSE;
