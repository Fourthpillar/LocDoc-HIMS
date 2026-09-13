-- Dev data only. Names the bootstrap facility and types it as a clinic.
--
-- The migration bootstrap leaves facility 1 called "Default Facility (migration
-- bootstrap)" and typed PHARMACY. That type is not cosmetic: a doctor looking for
-- somewhere to practise only sees HOSPITAL and CLINIC facilities, so on a PHARMACY
-- the entire doctor-side request flow has nothing to find. No endpoint exposes the
-- type (the facility profile screen edits name, address, licence - not type), which
-- is why this is SQL rather than an API call in setup.mjs.
--
-- Safe to re-run.

UPDATE facilities
SET name = 'LocDoc Demo Clinic',
    type = 'CLINIC'
WHERE id = 1
  AND type <> 'CLINIC';

SELECT id, name, type, verification_status, active FROM facilities;
