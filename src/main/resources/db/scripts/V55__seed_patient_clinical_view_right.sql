-- ============================================================
-- Version     : V55
-- Description : Lets the front desk read back a finished consultation on the
--               patient's own record screen. The clinical note and prescription
--               already existed, but only under CONSULTATION_RECORD_MANAGE and
--               only for the doctor who wrote them - so the Patient Record could
--               show when someone came and what they paid, and nothing about what
--               was found or prescribed. Reception already prints exactly this
--               content on the OP card (Master Spec §7.4/§9); this right is the
--               read-only half of that, scoped to the caller's own facility.
--               Kept separate from PATIENT_MANAGE on purpose: reading a diagnosis
--               is a different decision from editing a name and address, and a
--               facility that wants the split can revoke one without the other.
-- Author      : LockDoc App
-- ============================================================

INSERT INTO rights (right_code, right_name, description) VALUES
    ('PATIENT_CLINICAL_VIEW', 'View Patient Clinical History',
     'Read-only access to completed consultation notes and prescriptions on the Patient Record screen (Master Spec §7.4/§8.7)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code IN ('RECEPTIONIST', 'HOSPITAL_ADMIN', 'SUPER_ADMIN')
  AND rt.right_code = 'PATIENT_CLINICAL_VIEW'
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
