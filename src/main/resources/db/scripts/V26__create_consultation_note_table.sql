-- ============================================================
-- Version     : V26
-- Description : Step 6 - ConsultationNote (Master Spec §6/§8.7), the
--               full clinical field set off the real OP card. One row
--               per OP visit, draft-until-completed (auto-saved every
--               ~15s / on blur by the frontend, §8.7's own requirement
--               against losing a long form to a crash or navigation).
-- Author      : LockDoc App
-- ============================================================

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
