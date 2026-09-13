-- ============================================================
-- Version     : V3001
-- Module      : doctor
-- Description : All DDL owned by the doctor module - live doctor
--               status, consultation notes, prescriptions and
--               schedule exceptions (Availability Planner).
--
--               Builds on common (V1xxx) and outpatient (V2xxx):
--               consultation records hang off op_visits and schedule
--               exceptions block doctor_schedules.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- DOCTOR_STATUSES - append-only event log; "current status" is the
-- latest row for (doctor, facility, session_date).
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctor_statuses (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id       BIGINT        NOT NULL,
    facility_id     BIGINT        NOT NULL,
    session_date    DATE          NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    source          VARCHAR(20)   NOT NULL DEFAULT 'SELF',
    set_by_user_id  BIGINT        NOT NULL,
    created_date    TIMESTAMP     NOT NULL,
    CONSTRAINT fk_ds_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_ds_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_ds_lookup ON doctor_statuses (doctor_id, facility_id, session_date, created_date);
CREATE INDEX IF NOT EXISTS idx_ds_facility_date ON doctor_statuses (facility_id, session_date);

-- ---------------------------------------------------------------
-- CONSULTATION_NOTES - one per OP visit, draft until completed
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_notes (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    op_visit_id               BIGINT        NOT NULL,
    doctor_id                 BIGINT        NOT NULL,
    facility_id               BIGINT        NOT NULL,
    chief_complaint           VARCHAR(2000),
    past_history              VARCHAR(2000),
    family_history            VARCHAR(2000),
    nutritional_history       VARCHAR(2000),
    developmental_history     VARCHAR(2000),
    examination_findings      VARCHAR(2000),
    provisional_diagnosis     VARCHAR(2000),
    investigations_ordered    VARCHAR(2000),
    treatment_plan            VARCHAR(2000),
    patient_family_education  VARCHAR(2000),
    follow_up_plan            VARCHAR(2000),
    admit_to                  VARCHAR(200),
    is_draft                  BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date              TIMESTAMP     NOT NULL,
    updated_date              TIMESTAMP     NOT NULL,
    CONSTRAINT uq_cnote_visit UNIQUE (op_visit_id),
    CONSTRAINT fk_cnote_visit FOREIGN KEY (op_visit_id) REFERENCES op_visits (id),
    CONSTRAINT fk_cnote_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_cnote_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_cnote_doctor ON consultation_notes (doctor_id);

-- ---------------------------------------------------------------
-- PRESCRIPTIONS / PRESCRIPTION_LINES - one prescription per OP visit;
-- lines are replaced wholesale on each draft save. Medicines are free
-- text (no catalogue reference).
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prescriptions (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    op_visit_id    BIGINT      NOT NULL,
    doctor_id      BIGINT      NOT NULL,
    facility_id    BIGINT      NOT NULL,
    is_draft       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_date   TIMESTAMP   NOT NULL,
    updated_date   TIMESTAMP   NOT NULL,
    CONSTRAINT uq_presc_visit UNIQUE (op_visit_id),
    CONSTRAINT fk_presc_visit FOREIGN KEY (op_visit_id) REFERENCES op_visits (id),
    CONSTRAINT fk_presc_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_presc_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE TABLE IF NOT EXISTS prescription_lines (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id   BIGINT        NOT NULL,
    line_order        INT           NOT NULL DEFAULT 0,
    medicine_name     VARCHAR(200)  NOT NULL,
    generic_name      VARCHAR(200),
    strength          VARCHAR(50),
    dosage            VARCHAR(100),
    route             VARCHAR(50),
    frequency         VARCHAR(50),
    duration          VARCHAR(50),
    quantity          INT,
    refill_flag       BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_pline_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions (id)
);

CREATE INDEX IF NOT EXISTS idx_pline_prescription ON prescription_lines (prescription_id, line_order);

-- ---------------------------------------------------------------
-- SCHEDULE_EXCEPTIONS - leave / session-cancel / holiday / move on
-- top of doctor_schedules. doctor_schedule_id NULL blocks every
-- session that day; a MOVE carries new_start_time/new_end_time for
-- that one occurrence. Duplicate checks are application-level.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS schedule_exceptions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id           BIGINT       NOT NULL,
    facility_id         BIGINT       NOT NULL,
    doctor_schedule_id  BIGINT,
    exception_date      DATE         NOT NULL,
    exception_type      VARCHAR(20)  NOT NULL,
    new_start_time      TIME,
    new_end_time        TIME,
    reason              VARCHAR(300),
    created_by_user_id  BIGINT       NOT NULL,
    created_date        TIMESTAMP    NOT NULL,
    CONSTRAINT fk_schedule_exception_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_schedule_exception_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_schedule_exception_schedule FOREIGN KEY (doctor_schedule_id) REFERENCES doctor_schedules (id)
);

CREATE INDEX IF NOT EXISTS idx_schedule_exception_doctor_date ON schedule_exceptions (doctor_id, exception_date);
CREATE INDEX IF NOT EXISTS idx_schedule_exception_facility_date ON schedule_exceptions (facility_id, exception_date);
