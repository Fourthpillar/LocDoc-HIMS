-- ============================================================
-- Version     : V53
-- Description : The hours a doctor says they will be present at a facility
--               (Master Spec §8.1/§8.2).
--
--               A doctor could ask to practise at a hospital, but the
--               request said nothing about WHEN — the one fact the facility
--               needs in order to answer it. The hours only appeared later,
--               as doctor_schedules that a Hospital/Clinic Admin typed in
--               from memory or a phone call, which meant the receptionist
--               booking patients had no record of what the doctor had
--               actually committed to.
--
--               These rows are the doctor's own statement, attached to the
--               mapping: available while the request is still REQUESTED (so
--               the admin can read them before accepting), and kept after
--               it is ACCEPTED as the standing answer to "when is Dr X in?"
--
--               They are deliberately NOT doctor_schedules. A schedule is
--               the facility's operational decision — it carries capacity,
--               overbooking and an effective date range, and only the
--               facility can set those. These are the input to that
--               decision, which is why an admin creates sessions FROM them
--               rather than the system doing it silently.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS doctor_facility_mapping_hours (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    mapping_id   BIGINT      NOT NULL,
    weekday      VARCHAR(10) NOT NULL,
    start_time   TIME        NOT NULL,
    end_time     TIME        NOT NULL,
    CONSTRAINT fk_mapping_hours_mapping FOREIGN KEY (mapping_id) REFERENCES doctor_facility_mappings (id)
);

CREATE INDEX IF NOT EXISTS idx_mapping_hours_mapping ON doctor_facility_mapping_hours (mapping_id);
