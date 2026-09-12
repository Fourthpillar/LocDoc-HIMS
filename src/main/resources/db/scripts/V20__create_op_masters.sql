-- ============================================================
-- Version     : V20
-- Description : Step 5 - the two Hospital/Clinic-Admin-owned masters OP
--               registration and booking need to exist first
--               (Master Spec §7.2/§8.2):
--                 - registration_fee_configs: RegistrationFeeConfig, one
--                   row per facility, self-service.
--                 - doctor_schedules: DoctorSchedule, minimal - just
--                   enough for Appointment.doctor_schedule_id to mean
--                   something and for capacity/overbook enforcement
--                   (§6 invariant 5). The full recurring-template +
--                   leave/block Availability Planner UI (§8.2, §17.7 #14)
--                   is NOT this - that stays a later step. Created by
--                   Hospital/Clinic Admin for a doctor already ACCEPTED
--                   at their facility (DoctorFacilityMapping, step 4).
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS registration_fee_configs (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id            BIGINT         NOT NULL,
    first_fee              DECIMAL(10,2)  NOT NULL,
    re_registration_fee    DECIMAL(10,2)  NOT NULL,
    validity_days          INT            NOT NULL,
    updated_date           TIMESTAMP      NOT NULL,
    CONSTRAINT uq_rfc_facility UNIQUE (facility_id),
    CONSTRAINT fk_rfc_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE TABLE IF NOT EXISTS doctor_schedules (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id            BIGINT        NOT NULL,
    facility_id          BIGINT        NOT NULL,
    weekday              VARCHAR(10)   NOT NULL,
    session_name         VARCHAR(50)   NOT NULL,
    start_time           TIME          NOT NULL,
    end_time             TIME          NOT NULL,
    capacity             INT           NOT NULL,
    overbook_allowance   INT           NOT NULL DEFAULT 0,
    effective_from       DATE          NOT NULL,
    effective_to         DATE,
    active               BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date         TIMESTAMP     NOT NULL,
    CONSTRAINT fk_dsch_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_dsch_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_dsch_doctor_facility ON doctor_schedules (doctor_id, facility_id);
