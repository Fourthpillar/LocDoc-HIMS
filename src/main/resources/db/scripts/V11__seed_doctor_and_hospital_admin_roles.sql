-- ============================================================
-- Version     : V11
-- Description : Step 3 of the LocDoc-HIMS build order (Master Spec
--               §16) - identity extensions. Seeds the two roles that
--               didn't exist in this codebase at all until now:
--
--                 DOCTOR          - the module the earlier v0.3 HIMS
--                                   documents explicitly excluded;
--                                   this build restores it as core
--                                   (Master Spec §8). A Doctor's
--                                   User.facility_id is NULL, same as
--                                   Super Admin's - not because a
--                                   doctor is cross-facility like
--                                   Super Admin, but because a doctor
--                                   practises at zero-or-more
--                                   facilities via DoctorFacilityMapping
--                                   (built in a later step), never
--                                   exactly one the way every other
--                                   role is (Master Spec §4).
--                 HOSPITAL_ADMIN  - seeded fresh, not "renamed" from
--                                   anything - no generic ADMIN role
--                                   ever existed in this codebase to
--                                   rename. Given no rights initially
--                                   except the one already-working
--                                   endpoint that unambiguously
--                                   belongs to it today
--                                   (PHARMACY_PURCHASE_ORDER_APPROVE,
--                                   per Master Spec §4's permission
--                                   matrix) - everything else this
--                                   role needs (approve queue,
--                                   masters, add-doctor, etc.) gets
--                                   its right added in the step that
--                                   actually builds that endpoint,
--                                   not speculatively here.
--
--               Also adds DOCTOR_VERIFY (mapped to SUPER_ADMIN only),
--               mirroring the FACILITY_VERIFY pattern V10 established -
--               Super Admin verifies a self-registered doctor before
--               their account can log in (Master Spec §10).
-- Author      : LockDoc App
-- ============================================================

INSERT INTO roles (role_code, role_name, description) VALUES
    ('DOCTOR', 'Doctor', 'Practising clinician - own availability, live status, prescriptions; no facility administration'),
    ('HOSPITAL_ADMIN', 'Hospital/Clinic Admin', 'Facility operations/finance manager - scoped to exactly one facility');

INSERT INTO rights (right_code, right_name, description) VALUES
    ('DOCTOR_VERIFY', 'Verify Doctors', 'Ability to verify/reject a self-registered doctor, activating their login');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code = 'DOCTOR_VERIFY'
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code = 'PHARMACY_PURCHASE_ORDER_APPROVE';
