-- Removes everything generate.mjs / seed.sql added for dr.regtest's Doctor Reports test data.
-- DEV DATABASE ONLY - never a Flyway migration.
--
-- Seeded rows are identifiable two ways:
--   * a SEED document number: patients.mrn 'PT-SEED-%', op_visits.op_no 'OP-SEED-%', bills.bill_no 'BILL-SEED-%'
--     (notes, prescriptions, lines, ratings and payments hang off those)
--   * a 777-microsecond marker on created_date, for rows with no document number of their own
--     (doctor_schedules, schedule_exceptions, doctor_statuses)
-- Anything the app or a person created has neither, so it is left alone.

DELETE FROM consultation_ratings WHERE op_visit_id IN (SELECT id FROM op_visits WHERE facility_id = 1 AND op_no LIKE 'OP-SEED-%');
DELETE FROM prescription_lines WHERE prescription_id IN (SELECT p.id FROM prescriptions p JOIN op_visits v ON v.id = p.op_visit_id WHERE v.facility_id = 1 AND v.op_no LIKE 'OP-SEED-%');
DELETE FROM prescriptions WHERE op_visit_id IN (SELECT id FROM op_visits WHERE facility_id = 1 AND op_no LIKE 'OP-SEED-%');
DELETE FROM consultation_notes WHERE op_visit_id IN (SELECT id FROM op_visits WHERE facility_id = 1 AND op_no LIKE 'OP-SEED-%');
DELETE FROM payments WHERE bill_id IN (SELECT id FROM bills WHERE facility_id = 1 AND bill_no LIKE 'BILL-SEED-%');
DELETE FROM bills WHERE facility_id = 1 AND bill_no LIKE 'BILL-SEED-%';
DELETE FROM op_visits WHERE facility_id = 1 AND op_no LIKE 'OP-SEED-%';
DELETE FROM patients WHERE facility_id = 1 AND mrn LIKE 'PT-SEED-%';
DELETE FROM doctor_statuses WHERE doctor_id = 97 AND EXTRACT(MICROSECOND FROM created_date) = 777;
DELETE FROM schedule_exceptions WHERE doctor_id = 97 AND EXTRACT(MICROSECOND FROM created_date) = 777;
DELETE FROM doctor_schedules WHERE doctor_id = 97 AND EXTRACT(MICROSECOND FROM created_date) = 777;
