-- ============================================================
-- Version     : V301
-- Description : Seed the outpatient module right and map it to
--               the existing SUPER_ADMIN role.
-- Author      : LockDoc App
-- ============================================================

INSERT INTO rights (right_code, right_name, description) VALUES
    ('OUTPATIENT_PATIENT_MANAGE', 'Manage Patients', 'Ability to create/update/deactivate patients');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code = 'OUTPATIENT_PATIENT_MANAGE'
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
