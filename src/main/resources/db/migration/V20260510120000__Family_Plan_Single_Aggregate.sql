ALTER TABLE family_trips
    ADD COLUMN IF NOT EXISTS travel_plan_id BIGINT NULL REFERENCES travel_plans(id);
