-- ============================================================
-- Version     : V13
-- Description : Step 4 of the LocDoc-HIMS build order (Master Spec
--               §16) - Doctor Module, status phase + consultation-fee
--               proposal. Rights only; the tables/entities these gate
--               follow in V14-V16.
--
--               Per Master Spec §4.1's permission matrix (the
--               authoritative resolution where it differs from §8.1's
--               looser prose): only Hospital/Clinic Admin can *invite* a
--               doctor to a facility ("Add doctor to facility (invite)"
--               is F for Hospital/Clinic Admin, R (accept/decline) for
--               Doctor) - so the doctor side gets no "request/invite"
--               right, only accept/decline/end.
-- Author      : LockDoc App
-- ============================================================

INSERT INTO rights (right_code, right_name, description) VALUES
    ('DOCTOR_FACILITY_MAPPING_MANAGE', 'Manage Own Facility Mappings', 'Doctor: accept/decline/end own facility mapping requests (Master Spec §8.1)'),
    ('FACILITY_DOCTOR_MAPPING_MANAGE', 'Manage Facility Doctor Mappings', 'Hospital/Clinic Admin: invite/end doctor mappings at their own facility (Master Spec §4.1)'),
    ('DOCTOR_STATUS_UPDATE', 'Update Own Live Status', 'Doctor: set own live status per facility/session (Master Spec §8.3)'),
    ('FACILITY_DOCTOR_STATUS_VIEW', 'View Facility Doctor Status', 'Hospital/Clinic Admin: view live status of doctors mapped to their facility (Master Spec §8.3) - backend only until the front-desk day list (build order step 5) consumes it'),
    ('CONSULTATION_FEE_PROPOSE', 'Propose Consultation Fee', 'Doctor: propose own consultation fee per facility, pending Hospital/Clinic Admin approval (Master Spec §8.6)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'DOCTOR'
  AND rt.right_code IN ('DOCTOR_FACILITY_MAPPING_MANAGE', 'DOCTOR_STATUS_UPDATE', 'CONSULTATION_FEE_PROPOSE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('FACILITY_DOCTOR_MAPPING_MANAGE', 'FACILITY_DOCTOR_STATUS_VIEW')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('DOCTOR_FACILITY_MAPPING_MANAGE', 'FACILITY_DOCTOR_MAPPING_MANAGE', 'DOCTOR_STATUS_UPDATE', 'FACILITY_DOCTOR_STATUS_VIEW', 'CONSULTATION_FEE_PROPOSE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
