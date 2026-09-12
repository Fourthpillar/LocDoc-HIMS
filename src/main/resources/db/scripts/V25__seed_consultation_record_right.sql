-- ============================================================
-- Version     : V25
-- Description : Step 6 of the LocDoc-HIMS build order (Master Spec
--               §16/§8.7) - the Doctor Module's Consultation Workspace.
--               One new right: authoring a ConsultationNote/Prescription
--               is exclusively the doctor's own action (Master Spec
--               §4.1: "Author prescription" - F for Doctor, none for
--               every other role).
-- Author      : LockDoc App
-- ============================================================

INSERT INTO rights (right_code, right_name, description) VALUES
    ('CONSULTATION_RECORD_MANAGE', 'Author Consultation Record', 'Doctor: author/draft/complete the clinical note and prescription for a visit (Master Spec §8.7)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'DOCTOR'
  AND rt.right_code = 'CONSULTATION_RECORD_MANAGE'
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code = 'CONSULTATION_RECORD_MANAGE'
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
