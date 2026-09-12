-- ============================================================
-- Version     : V102
-- Description : Facilities — the tenant a facility-scoped record belongs to,
--               and the user's link to one.
--
--               Added for the doctor module, which cannot express its own
--               domain without it: a doctor is mapped TO a facility, is on
--               duty AT one, and charges a consultation rate agreed WITH one.
--               It lands in `common` rather than in the doctor module because
--               outpatient and pharmacy will scope their own records the same
--               way, and a table owned by a feature module cannot be reached
--               across a module boundary.
--
--               Deliberately additive. `users.facility_id` is nullable and
--               nothing existing reads it, so pharmacy and outpatient behave
--               exactly as before: their rows are unscoped until each module
--               takes its own decision about tenancy.
--
--               A user with no facility is one who belongs to none — Super
--               Admin, who works across all of them, and a doctor, whose
--               identity is central and whose facilities are a mapping.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS facilities (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    code          VARCHAR(50)  NOT NULL,
    type          VARCHAR(20)  NOT NULL DEFAULT 'CLINIC',
    address       VARCHAR(300),
    city          VARCHAR(100),
    state         VARCHAR(100),
    pincode       VARCHAR(10),
    phone         VARCHAR(20),
    email         VARCHAR(100),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date  TIMESTAMP    NOT NULL,
    updated_date  TIMESTAMP,
    CONSTRAINT uq_facilities_code UNIQUE (code)
);

ALTER TABLE users ADD COLUMN IF NOT EXISTS facility_id BIGINT;

ALTER TABLE users ADD CONSTRAINT fk_users_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);

CREATE INDEX IF NOT EXISTS idx_users_facility ON users (facility_id);

-- One facility to develop against. Every existing user stays unscoped
-- (facility_id NULL), including FP_USER, who is Super Admin and must remain so.
INSERT INTO facilities (name, code, type, city, state, active, created_date, updated_date)
SELECT 'LockDoc Demo Clinic', 'DEMO-CLINIC', 'CLINIC', 'Chennai', 'Tamil Nadu', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM facilities WHERE code = 'DEMO-CLINIC');
