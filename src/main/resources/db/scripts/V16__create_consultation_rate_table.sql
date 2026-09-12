-- ============================================================
-- Version     : V16
-- Description : Step 4 of the LocDoc-HIMS build order (Master Spec
--               §8.6/§6) - doctor-proposed, Hospital/Clinic Admin-
--               approved consultation fee. This migration and its
--               entity build the propose half only (build order step 4);
--               the approve half ships with step 5.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS consultation_rates (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id             BIGINT         NOT NULL,
    facility_id           BIGINT         NOT NULL,
    org_type              VARCHAR(20)    NOT NULL DEFAULT 'DIRECT',
    day_night_indicator   VARCHAR(10)    NOT NULL DEFAULT 'DAY',
    total_amount          DECIMAL(10,2)  NOT NULL,
    hospital_percent      DECIMAL(5,2),
    effective_from        DATE,
    effective_to          DATE,
    status                VARCHAR(20)    NOT NULL DEFAULT 'PENDING_APPROVAL',
    proposed_by_user_id   BIGINT         NOT NULL,
    approved_by_user_id   BIGINT,
    approved_at           TIMESTAMP,
    rejection_reason      VARCHAR(300),
    created_date          TIMESTAMP      NOT NULL,
    updated_date          TIMESTAMP,
    CONSTRAINT fk_cr_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_cr_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_cr_doctor_facility ON consultation_rates (doctor_id, facility_id);
CREATE INDEX IF NOT EXISTS idx_cr_facility_status ON consultation_rates (facility_id, status);
