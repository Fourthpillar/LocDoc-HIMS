-- ============================================================
-- Version     : V401
-- Description : Roles and rights for the doctor module.
--
--               Three actors touch this module, and the rights split along
--               exactly those lines:
--                 DOCTOR         — their own profile, mappings, status, and
--                                  proposing what they charge
--                 HOSPITAL_ADMIN — the facility's side: inviting doctors,
--                                  approving rates, setting the free-review
--                                  policy, seeing who is on duty
--                 SUPER_ADMIN    — verifying a self-registered doctor, the one
--                                  check that happens once and centrally
--
--               A doctor cannot approve their own rate and an admin cannot
--               verify a doctor's council registration; that separation is the
--               point of splitting DOCTOR_* from FACILITY_* rights rather than
--               granting one "doctor management" right to everybody.
--
--               RECEPTION_DOCTOR_STATUS_OVERRIDE is seeded here but granted to
--               no role yet: the receptionist role arrives with the outpatient
--               module's own merge, and the right it needs should already exist
--               when it does.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- Roles
-- ---------------------------------------------------------------
INSERT INTO roles (role_code, role_name, description)
SELECT 'DOCTOR', 'Doctor', 'Practising clinician - own profile, facility mappings, live status and proposed consultation fees'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_code = 'DOCTOR');

INSERT INTO roles (role_code, role_name, description)
SELECT 'HOSPITAL_ADMIN', 'Hospital/Clinic Admin', 'Facility operations manager - scoped to exactly one facility'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_code = 'HOSPITAL_ADMIN');

-- ---------------------------------------------------------------
-- Rights
-- ---------------------------------------------------------------
INSERT INTO rights (right_code, right_name, description)
SELECT * FROM (
    SELECT 'DOCTOR_VERIFY' AS c, 'Verify Doctors' AS n, 'Verify or reject a self-registered doctor, activating their login' AS d UNION ALL
    SELECT 'DOCTOR_PROFILE_MANAGE', 'Manage Own Doctor Profile', 'A doctor editing their own profile - specialties, qualifications' UNION ALL
    SELECT 'DOCTOR_FACILITY_MAPPING_MANAGE', 'Manage Own Facility Mappings', 'A doctor requesting facilities, answering invitations, stating consulting hours' UNION ALL
    SELECT 'DOCTOR_STATUS_UPDATE', 'Update Own Live Status', 'A doctor setting their own on-duty status for a facility and day' UNION ALL
    SELECT 'CONSULTATION_FEE_PROPOSE', 'Propose Consultation Fee', 'A doctor proposing what they charge at a facility' UNION ALL
    SELECT 'FACILITY_DOCTOR_MAPPING_MANAGE', 'Manage Facility Doctor Mappings', 'A facility inviting doctors and answering their requests' UNION ALL
    SELECT 'FACILITY_DOCTOR_STATUS_VIEW', 'View Doctor Status', 'Seeing which doctors are on duty at the facility today' UNION ALL
    SELECT 'CONSULTATION_FEE_APPROVE', 'Approve Consultation Fee', 'A facility approving or rejecting a proposed consultation fee' UNION ALL
    SELECT 'FREE_REVIEW_POLICY_MANAGE', 'Manage Free-Review Policy', 'Setting how long and for how many visits a follow-up is free' UNION ALL
    SELECT 'RECEPTION_DOCTOR_STATUS_OVERRIDE', 'Override Doctor Status', 'Front desk correcting a doctor status on the doctor''s behalf'
) seed
WHERE NOT EXISTS (SELECT 1 FROM rights WHERE right_code = seed.c);

-- ---------------------------------------------------------------
-- Grants
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'DOCTOR'
  AND rt.right_code IN ('DOCTOR_PROFILE_MANAGE', 'DOCTOR_FACILITY_MAPPING_MANAGE', 'DOCTOR_STATUS_UPDATE', 'CONSULTATION_FEE_PROPOSE')
  AND NOT EXISTS (SELECT 1 FROM role_rights x WHERE x.role_id = r.id AND x.right_id = rt.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('FACILITY_DOCTOR_MAPPING_MANAGE', 'FACILITY_DOCTOR_STATUS_VIEW', 'CONSULTATION_FEE_APPROVE', 'FREE_REVIEW_POLICY_MANAGE')
  AND NOT EXISTS (SELECT 1 FROM role_rights x WHERE x.role_id = r.id AND x.right_id = rt.id);

-- Super Admin verifies doctors, and keeps the facility-side rights too so a
-- single-tenant dev database is usable without inventing a second login.
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('DOCTOR_VERIFY', 'FACILITY_DOCTOR_MAPPING_MANAGE', 'FACILITY_DOCTOR_STATUS_VIEW',
                        'CONSULTATION_FEE_APPROVE', 'FREE_REVIEW_POLICY_MANAGE')
  AND NOT EXISTS (SELECT 1 FROM role_rights x WHERE x.role_id = r.id AND x.right_id = rt.id);
