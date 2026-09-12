-- ============================================================
-- Version     : V21
-- Description : Step 5 - PatientRegistration and OpVisit (Master Spec
--               §7.4/§6). PatientRegistration deliberately does NOT
--               duplicate fee/discount/net/paid/due directly (§6 lists
--               them on this entity) - those live once, on Bill
--               (V24), addressed via encounter_type='REGISTRATION' +
--               encounter_id=this row's id, so a registration's money
--               only ever exists in one place. registration_no is a
--               DocumentNumberService-generated number (reused from the
--               pharmacy module - it was never actually pharmacy-
--               specific, just pharmacy-first).
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS patient_registrations (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id          BIGINT        NOT NULL,
    facility_id         BIGINT        NOT NULL,
    registration_no     VARCHAR(30)   NOT NULL,
    is_re_registration  BOOLEAN       NOT NULL DEFAULT FALSE,
    expiry_date         DATE          NOT NULL,
    registered_at       TIMESTAMP     NOT NULL,
    registered_by_user_id BIGINT      NOT NULL,
    CONSTRAINT uq_preg_no UNIQUE (facility_id, registration_no),
    CONSTRAINT fk_preg_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_preg_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_preg_patient ON patient_registrations (patient_id, expiry_date);

CREATE TABLE IF NOT EXISTS op_visits (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    op_no                VARCHAR(30)   NOT NULL,
    facility_id          BIGINT        NOT NULL,
    patient_id           BIGINT        NOT NULL,
    appointment_id       BIGINT,
    doctor_id            BIGINT        NOT NULL,
    org_type             VARCHAR(20)   NOT NULL DEFAULT 'DIRECT',
    visit_type           VARCHAR(30)   NOT NULL DEFAULT 'NEW',
    weight_kg            DECIMAL(6,2),
    height_cm            DECIMAL(6,2),
    temperature_f        DECIMAL(5,2),
    bp                   VARCHAR(20),
    attendant_name       VARCHAR(150),
    attendant_mobile     VARCHAR(20),
    attendant_relation   VARCHAR(50),
    status               VARCHAR(20)   NOT NULL DEFAULT 'ARRIVED',
    mlc_flag             BOOLEAN       NOT NULL DEFAULT FALSE,
    mlc_police_station   VARCHAR(150),
    mlc_number           VARCHAR(50),
    arrived_ts           TIMESTAMP     NOT NULL,
    consult_start_ts     TIMESTAMP,
    consult_end_ts       TIMESTAMP,
    CONSTRAINT uq_opvisit_no UNIQUE (facility_id, op_no),
    CONSTRAINT fk_opvisit_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_opvisit_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_opvisit_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id)
);

CREATE INDEX IF NOT EXISTS idx_opvisit_facility_date ON op_visits (facility_id, arrived_ts);
CREATE INDEX IF NOT EXISTS idx_opvisit_doctor ON op_visits (doctor_id, arrived_ts);
