-- ============================================================
-- Version     : V54
-- Description : What an appointment is actually for (Master Spec §7.3/§7.5).
--
--               Every appointment was implicitly a consultation, because
--               that is the only thing check-in could bill. But a patient
--               books a slot for a dressing, an ECG or a scan just as
--               often, and the desk had nowhere to say so: the doctor's
--               session filled up with procedure visits that then billed a
--               consultation fee nobody had agreed to.
--
--                 CONSULTATION -> the doctor's fee is raised on arrival
--                 PROCEDURE    -> no consultation fee; the procedure is
--                                 billed from its own charge master, and
--                                 (§7.5) only once any consultation the
--                                 patient already owes for has been paid
--
--               CONSULTATION is the historically correct backfill: every
--               existing row was booked when that was the only meaning.
-- Author      : LockDoc App
-- ============================================================

ALTER TABLE appointments ADD COLUMN purpose VARCHAR(20) NOT NULL DEFAULT 'CONSULTATION';

CREATE INDEX IF NOT EXISTS idx_appointments_purpose ON appointments (facility_id, purpose);
