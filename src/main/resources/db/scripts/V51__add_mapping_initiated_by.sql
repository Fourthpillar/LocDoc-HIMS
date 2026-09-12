-- ============================================================
-- Version     : V51
-- Description : Doctor-initiated facility requests (Master Spec §8.1).
--               Until now the doctor-facility relationship was invite-only:
--               a Hospital/Clinic Admin invited, the doctor accepted or
--               declined, and there was no way for a doctor to approach a
--               facility at all. The relationship is meant to be openable
--               from either side, so a mapping now records WHO opened it.
--
--               Why this column is load-bearing rather than cosmetic:
--               STATUS_REQUESTED previously had exactly one meaning,
--               "waiting on the doctor". Once a doctor can also create a
--               REQUESTED row, that status alone no longer says who owes a
--               response — and without the distinction the doctor's own
--               accept() endpoint would happily approve a request the
--               doctor themselves raised. initiated_by is what keeps the
--               two-sided confirmation honest:
--
--                 REQUESTED + FACILITY -> awaiting the doctor's response
--                 REQUESTED + DOCTOR   -> awaiting the facility's response
--
--               Existing rows were all created by the invite path, so
--               'FACILITY' is the historically correct backfill, not just
--               a convenient default.
-- Author      : LockDoc App
-- ============================================================

ALTER TABLE doctor_facility_mappings ADD COLUMN initiated_by VARCHAR(20) NOT NULL DEFAULT 'FACILITY';

CREATE INDEX IF NOT EXISTS idx_doctor_facility_mapping_status_initiator
    ON doctor_facility_mappings (status, initiated_by);
