-- ============================================================
-- Version     : V22
-- Description : Step 5 - Appointment and AppointmentWaitlist (Master
--               Spec §7.3/§6). Capacity + overbook enforcement (§6
--               invariant 5) happens in AppointmentService via a
--               facility-level lock (H2/most RDBMS: SELECT ... FOR
--               UPDATE) around the booked-count check, not a DB
--               constraint - the "confirmed bookings per
--               (schedule_id, session_date) never exceed capacity +
--               overbook allowance" rule spans a COUNT, which a column
--               CHECK constraint can't express.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS appointments (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id           BIGINT        NOT NULL,
    doctor_id             BIGINT        NOT NULL,
    doctor_schedule_id    BIGINT        NOT NULL,
    patient_id            BIGINT        NOT NULL,
    appointment_ts        TIMESTAMP     NOT NULL,
    status                VARCHAR(20)   NOT NULL DEFAULT 'BOOKED',
    channel               VARCHAR(20)   NOT NULL DEFAULT 'FRONT_DESK',
    cancel_reason         VARCHAR(300),
    created_date          TIMESTAMP     NOT NULL,
    CONSTRAINT fk_appt_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_appt_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_appt_schedule FOREIGN KEY (doctor_schedule_id) REFERENCES doctor_schedules (id),
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE INDEX IF NOT EXISTS idx_appt_schedule_date ON appointments (doctor_schedule_id, appointment_ts);
CREATE INDEX IF NOT EXISTS idx_appt_facility_date ON appointments (facility_id, appointment_ts);
CREATE INDEX IF NOT EXISTS idx_appt_patient ON appointments (patient_id);

CREATE TABLE IF NOT EXISTS appointment_waitlist (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id           BIGINT        NOT NULL,
    doctor_schedule_id    BIGINT        NOT NULL,
    session_date          DATE          NOT NULL,
    patient_id            BIGINT        NOT NULL,
    joined_at             TIMESTAMP     NOT NULL,
    promoted_at           TIMESTAMP,
    promoted_appointment_id BIGINT,
    CONSTRAINT fk_wait_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_wait_schedule FOREIGN KEY (doctor_schedule_id) REFERENCES doctor_schedules (id),
    CONSTRAINT fk_wait_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE INDEX IF NOT EXISTS idx_wait_schedule_date ON appointment_waitlist (doctor_schedule_id, session_date, joined_at);
