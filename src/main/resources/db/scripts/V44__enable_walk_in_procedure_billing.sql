-- ============================================================
-- Version     : V44
-- Description : Unregistered walk-in procedure billing (Master Spec
--               §7.5 - "Procedures billable to an unregistered walk-in
--               (name/age/gender/mobile captured inline, performer may
--               be 'Non-Doctor')... a flag on the procedure line, not
--               tied to a doctor_id at all"). V39 deliberately scoped
--               ProcedureBill to registered patients only, flagging this
--               exact gap in its own migration comment as needing
--               Bill.patient_id nullable - a wider blast radius than
--               that pass wanted, since Bill is the single billing
--               engine for every OP encounter type, not one narrow flow.
--
--               Audited every direct b.getPatient()/bill.patient
--               dereference in the codebase before this migration
--               (BillResponse, ProcedureBillResponse, and
--               BillRepository.searchDue's JPQL, now LEFT JOIN) - those
--               are the only places outside this migration that needed
--               a code change to tolerate a null patient; every other
--               patient-carrying entity (Appointment, OpVisit,
--               PatientRegistration, PackageSale, AppointmentWaitlist)
--               is untouched and stays NOT NULL.
-- Author      : LockDoc App
-- ============================================================

ALTER TABLE bills ALTER COLUMN patient_id SET NULL;
ALTER TABLE procedure_bills ALTER COLUMN patient_id SET NULL;

ALTER TABLE procedure_bills ADD COLUMN IF NOT EXISTS walk_in_name VARCHAR(150);
ALTER TABLE procedure_bills ADD COLUMN IF NOT EXISTS walk_in_age INT;
ALTER TABLE procedure_bills ADD COLUMN IF NOT EXISTS walk_in_gender VARCHAR(10);
ALTER TABLE procedure_bills ADD COLUMN IF NOT EXISTS walk_in_mobile VARCHAR(20);
