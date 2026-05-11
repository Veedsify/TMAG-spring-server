CREATE TABLE IF NOT EXISTS family_trip_member_questionnaires (
    id BIGSERIAL PRIMARY KEY,
    family_trip_member_id BIGINT NOT NULL REFERENCES family_trip_members(id),
    travel_plan_id BIGINT REFERENCES travel_plans(id),
    responses_json TEXT,
    source VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_ftmq_member_id ON family_trip_member_questionnaires(family_trip_member_id);
CREATE INDEX idx_ftmq_travel_plan_id ON family_trip_member_questionnaires(travel_plan_id);
