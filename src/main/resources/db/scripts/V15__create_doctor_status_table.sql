-- ============================================================
-- Version     : V15
-- Description : Step 4 of the LocDoc-HIMS build order (Master Spec
--               §8.3/§6) - live doctor status. Append-only event log
--               (matches AuditLog's own pattern) - every change is a new
--               row, never an update; "current status" is the latest row
--               for a given (doctor, facility, session_date).
-- Author      : LockDoc App
-- ============================================================

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

-- Lookup shape matches the two read patterns DoctorStatusService actually
-- runs: "my status today across every facility" and "a facility's doctors'
-- status today" - both filter on session_date first.
CREATE INDEX IF NOT EXISTS idx_ds_lookup ON doctor_statuses (doctor_id, facility_id, session_date, created_date);
CREATE INDEX IF NOT EXISTS idx_ds_facility_date ON doctor_statuses (facility_id, session_date);
