CREATE TABLE family_package_purchases (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  package_type VARCHAR(20) NOT NULL,           -- ONE_TRIP | TWO_TRIP
  trips_allowed INT NOT NULL,                   -- 1 or 2
  trips_used INT NOT NULL DEFAULT 0,
  amount_paid_minor BIGINT NOT NULL,            -- in kobo / cents
  currency VARCHAR(3) NOT NULL,                 -- NGN | USD
  payment_provider VARCHAR(40),
  payment_reference VARCHAR(255),
  status VARCHAR(30) NOT NULL,                  -- PENDING | ACTIVE | EXHAUSTED | REFUNDED
  expires_at TIMESTAMP NULL,                    -- optional validity window
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_family_purchases_user_status
  ON family_package_purchases(user_id, status);

CREATE TABLE family_trips (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  family_package_purchase_id BIGINT NULL REFERENCES family_package_purchases(id),
  status VARCHAR(30) NOT NULL,                  -- DRAFT | PAYMENT_REQUIRED | QUEUED | PROCESSING | COMPLETED | FAILED
  destination VARCHAR(255) NOT NULL,
  country VARCHAR(255) NOT NULL,
  duration INT NOT NULL,
  purpose VARCHAR(255),
  trip_type VARCHAR(30),                        -- one_way | return | multi_stop | transit
  trip_details_json TEXT,
  base_credit_cost INT NOT NULL DEFAULT 1,
  extra_member_count INT NOT NULL DEFAULT 0,
  total_credit_cost INT NOT NULL DEFAULT 1,
  submitted_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_family_trips_user_status ON family_trips(user_id, status);

CREATE TABLE family_trip_members (
  id BIGSERIAL PRIMARY KEY,
  family_trip_id BIGINT NOT NULL REFERENCES family_trips(id),
  travel_plan_id BIGINT NULL REFERENCES travel_plans(id),
  relationship VARCHAR(30) NOT NULL,            -- MAIN_APPLICANT | SPOUSE | CHILD | ADDITIONAL_ADULT | ADDITIONAL_CHILD
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  member_email VARCHAR(255) NULL,               -- where their login code is sent
  date_of_birth DATE NULL,
  age_at_departure INT NULL,
  included_in_base BOOLEAN NOT NULL DEFAULT FALSE,
  questionnaire_responses_json TEXT,
  questionnaire_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  sort_order INT NOT NULL DEFAULT 0,

  -- Auth fields (see §7)
  login_code VARCHAR(8) NULL,
  login_code_consumed_at TIMESTAMP NULL,
  session_token_hash VARCHAR(128) NULL,
  session_expires_at TIMESTAMP NULL,
  failed_login_attempts INT NOT NULL DEFAULT 0,
  locked_until TIMESTAMP NULL,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL
);

CREATE INDEX idx_family_members_trip ON family_trip_members(family_trip_id);
CREATE UNIQUE INDEX idx_family_members_session_hash
  ON family_trip_members(session_token_hash)
  WHERE session_token_hash IS NOT NULL;
CREATE INDEX idx_family_members_email_lookup
  ON family_trip_members(member_email);

ALTER TABLE travel_plans
  ADD COLUMN family_trip_id BIGINT NULL REFERENCES family_trips(id),
  ADD COLUMN family_trip_member_id BIGINT NULL REFERENCES family_trip_members(id),
  ADD COLUMN traveller_display_name VARCHAR(255) NULL,
  ADD COLUMN traveller_relationship VARCHAR(30) NULL;

CREATE INDEX idx_travel_plans_family_trip ON travel_plans(family_trip_id);
