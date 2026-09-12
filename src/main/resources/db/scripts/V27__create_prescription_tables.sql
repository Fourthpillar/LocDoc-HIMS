-- ============================================================
-- Version     : V27
-- Description : Step 6 - Prescription/PrescriptionLine (Master Spec
--               §6/§8.7). One Prescription per OP visit; lines replaced
--               wholesale on each draft save (simplest correct model for
--               a form that's edited as a whole, not line-by-line
--               against a server - see PrescriptionService). matched_
--               pharmacy_medicine_id is nullable and populated only when
--               the facility's Pharmacy module is active and a catalogue
--               match is found (§8.7's module-independence requirement).
-- Author      : LockDoc App
-- ============================================================

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
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id             BIGINT        NOT NULL,
    line_order                  INT           NOT NULL DEFAULT 0,
    medicine_name               VARCHAR(200)  NOT NULL,
    generic_name                VARCHAR(200),
    strength                    VARCHAR(50),
    dosage                      VARCHAR(100),
    route                       VARCHAR(50),
    frequency                   VARCHAR(50),
    duration                    VARCHAR(50),
    quantity                    INT,
    refill_flag                 BOOLEAN       NOT NULL DEFAULT FALSE,
    matched_pharmacy_medicine_id BIGINT,
    CONSTRAINT fk_pline_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions (id)
);

CREATE INDEX IF NOT EXISTS idx_pline_prescription ON prescription_lines (prescription_id, line_order);
