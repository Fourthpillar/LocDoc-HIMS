-- ============================================================
-- Version     : V400
-- Description : Doctor module DDL — doctor identity, the relationship
--               between a doctor and a facility (with the hours they say
--               they consult there), on-duty status, consultation rates,
--               and the free-review policy.
--
--               Ported from the feature_doctor/main branch, where these
--               arrived incrementally as V12/V14/V15/V16/V45/V51/V53. None
--               of those ever ran against this database, so they are folded
--               into one script at their final shape rather than replayed as
--               seven — the `initiated_by` column and the mapping-hours table
--               are here from the start instead of being bolted on.
--
--               `facilities` and `users` come from common's V102/V100.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- DOCTORS — identity, verified once and centrally. Which facilities a
-- doctor practises at is the mapping below, never a column here.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctors (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT       NOT NULL,
    full_name            VARCHAR(150) NOT NULL,
    registration_number  VARCHAR(100) NOT NULL,
    verification_status  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    specialties          VARCHAR(300),
    qualifications       VARCHAR(300),
    created_date         TIMESTAMP    NOT NULL,
    updated_date         TIMESTAMP,
    CONSTRAINT uq_doctors_user_id UNIQUE (user_id),
    CONSTRAINT uq_doctors_registration_number UNIQUE (registration_number),
    CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ---------------------------------------------------------------
-- DOCTOR ↔ FACILITY — openable from either side, which is why the row
-- records who opened it: REQUESTED alone doesn't say who owes a response.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctor_facility_mappings (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id          BIGINT      NOT NULL,
    facility_id        BIGINT      NOT NULL,
    relationship_type  VARCHAR(20) NOT NULL,
    status             VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    initiated_by       VARCHAR(20) NOT NULL DEFAULT 'FACILITY',
    requested_at       TIMESTAMP   NOT NULL,
    responded_at       TIMESTAMP,
    ended_at           TIMESTAMP,
    CONSTRAINT fk_dfm_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_dfm_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_dfm_doctor ON doctor_facility_mappings (doctor_id);
CREATE INDEX IF NOT EXISTS idx_dfm_facility ON doctor_facility_mappings (facility_id);
CREATE INDEX IF NOT EXISTS idx_dfm_status_initiator ON doctor_facility_mappings (status, initiated_by);

-- ---------------------------------------------------------------
-- STATED CONSULTING HOURS — the doctor's own answer to "when are you
-- here?", attached to the mapping so a facility can read it before
-- accepting, and reception after.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctor_facility_mapping_hours (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    mapping_id  BIGINT      NOT NULL,
    weekday     VARCHAR(10) NOT NULL,
    start_time  TIME        NOT NULL,
    end_time    TIME        NOT NULL,
    CONSTRAINT fk_mapping_hours_mapping FOREIGN KEY (mapping_id) REFERENCES doctor_facility_mappings (id)
);

CREATE INDEX IF NOT EXISTS idx_mapping_hours_mapping ON doctor_facility_mapping_hours (mapping_id);

-- ---------------------------------------------------------------
-- LIVE STATUS — one row per change, per doctor per facility per day;
-- the latest row wins, so the day's history survives.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctor_statuses (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id       BIGINT      NOT NULL,
    facility_id     BIGINT      NOT NULL,
    session_date    DATE        NOT NULL,
    status          VARCHAR(20) NOT NULL,
    source          VARCHAR(20) NOT NULL DEFAULT 'SELF',
    set_by_user_id  BIGINT      NOT NULL,
    created_date    TIMESTAMP   NOT NULL,
    CONSTRAINT fk_ds_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_ds_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_ds_lookup ON doctor_statuses (doctor_id, facility_id, session_date, created_date);
CREATE INDEX IF NOT EXISTS idx_ds_facility_date ON doctor_statuses (facility_id, session_date);

-- ---------------------------------------------------------------
-- CONSULTATION RATES — proposed by the doctor, approved by the facility.
-- Keyed on org type and the day/night window, each with its own history.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_rates (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id            BIGINT        NOT NULL,
    facility_id          BIGINT        NOT NULL,
    org_type             VARCHAR(20)   NOT NULL DEFAULT 'DIRECT',
    day_night_indicator  VARCHAR(10)   NOT NULL DEFAULT 'DAY',
    total_amount         DECIMAL(10,2) NOT NULL,
    hospital_percent     DECIMAL(5,2),
    effective_from       DATE,
    effective_to         DATE,
    status               VARCHAR(20)   NOT NULL DEFAULT 'PENDING_APPROVAL',
    proposed_by_user_id  BIGINT        NOT NULL,
    approved_by_user_id  BIGINT,
    approved_at          TIMESTAMP,
    rejection_reason     VARCHAR(300),
    created_date         TIMESTAMP     NOT NULL,
    updated_date         TIMESTAMP,
    CONSTRAINT fk_cr_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_cr_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_cr_doctor_facility ON consultation_rates (doctor_id, facility_id);
CREATE INDEX IF NOT EXISTS idx_cr_facility_status ON consultation_rates (facility_id, status);

-- ---------------------------------------------------------------
-- FREE-REVIEW POLICY — how long after a paid consultation, and for how
-- many visits, a follow-up with the same doctor is free. One per
-- doctor per facility.
--
-- The source branch also has free_review_links, which record each free
-- visit against the paid one it was counted against. That table points at
-- op_visits, which the outpatient module does not have yet, so it comes
-- across with the OP merge rather than dangling a foreign key here.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS free_review_policies (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id     BIGINT    NOT NULL,
    facility_id   BIGINT    NOT NULL,
    max_days      INT       NOT NULL,
    max_visits    INT       NOT NULL,
    created_date  TIMESTAMP NOT NULL,
    updated_date  TIMESTAMP,
    CONSTRAINT fk_frp_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_frp_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_frp_doctor_facility ON free_review_policies (doctor_id, facility_id);
