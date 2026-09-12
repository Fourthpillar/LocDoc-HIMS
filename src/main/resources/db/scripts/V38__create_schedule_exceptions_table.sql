-- ============================================================
-- Version     : V38
-- Description : Availability Planner (Master Spec §8.2, screen #14) -
--               the ScheduleException entity named in §6 but never
--               built: leave / session-cancel / holiday overrides on
--               top of the recurring DoctorSchedule backbone (V15/V37).
--
--               doctor_schedule_id is nullable: a row with it set blocks
--               one specific recurring session on exception_date; a row
--               with it null blocks every session that doctor has at
--               that facility on that date (a whole-day leave/holiday).
--               No DB uniqueness constraint on (doctor, date, schedule) -
--               a NULL doctor_schedule_id can't be deduplicated by a
--               unique index (each NULL sorts distinct in SQL), so the
--               "don't double-block the same thing" check is
--               application-level in DoctorAvailabilityService, same as
--               CounterSession's duplicate-open check (V37).
--
--               Per §8.2 "who can block a session": the doctor blocks
--               their own schedule directly, self-service, no Hospital/
--               Clinic Admin approval gate - DOCTOR_AVAILABILITY_MANAGE
--               is Doctor-only. Hospital/Clinic Admin gets read
--               visibility via the existing DOCTOR_SCHEDULE_MANAGE
--               right instead of a new one, since they already hold
--               that for the schedule templates these exceptions block.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS schedule_exceptions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id           BIGINT       NOT NULL,
    facility_id         BIGINT       NOT NULL,
    doctor_schedule_id  BIGINT,
    exception_date      DATE         NOT NULL,
    exception_type      VARCHAR(20)  NOT NULL,
    reason              VARCHAR(300),
    created_by_user_id  BIGINT       NOT NULL,
    created_date        TIMESTAMP    NOT NULL,
    CONSTRAINT fk_schedule_exception_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_schedule_exception_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_schedule_exception_schedule FOREIGN KEY (doctor_schedule_id) REFERENCES doctor_schedules (id)
);

CREATE INDEX IF NOT EXISTS idx_schedule_exception_doctor_date ON schedule_exceptions (doctor_id, exception_date);
CREATE INDEX IF NOT EXISTS idx_schedule_exception_facility_date ON schedule_exceptions (facility_id, exception_date);

INSERT INTO rights (right_code, right_name, description) VALUES
    ('DOCTOR_AVAILABILITY_MANAGE', 'Manage Own Availability', 'A doctor blocking their own schedule - leave/session-cancel/holiday - and resolving any appointments it affects (Master Spec §8.2, screen #14)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'DOCTOR'
  AND rt.right_code IN ('DOCTOR_AVAILABILITY_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('DOCTOR_AVAILABILITY_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
