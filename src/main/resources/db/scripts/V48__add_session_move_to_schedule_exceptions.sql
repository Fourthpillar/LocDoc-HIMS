-- Drag-to-reschedule on the Availability Planner (Master Spec §8.2, §17.7 #14):
-- "dragging a session block to a new time slot proposes the change and runs
-- it through the same affected-appointments-surfaced flow as any other
-- session block." A move is a third ScheduleException type alongside
-- LEAVE/SESSION_CANCEL/HOLIDAY, scoped to one occurrence (doctor_schedule_id
-- is required, never a whole-day move) and carrying the proposed new time
-- for that date only - it does not touch the recurring DoctorSchedule
-- template itself.
ALTER TABLE schedule_exceptions ADD COLUMN new_start_time TIME;
ALTER TABLE schedule_exceptions ADD COLUMN new_end_time TIME;
