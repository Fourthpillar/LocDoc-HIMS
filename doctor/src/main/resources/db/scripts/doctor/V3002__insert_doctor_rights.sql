-- ============================================================
-- Version     : V3002
-- Module      : doctor
-- Description : All seed data owned by the doctor module - doctor
--               verification/profile/mapping/status/fee/schedule/
--               consultation/report rights, and their role mappings
--               (including the RECEPTIONIST doctor-status rights,
--               which is why this runs after V2002).
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- Rights
-- ---------------------------------------------------------------
INSERT INTO rights (right_code, right_name, description) VALUES
    ('DOCTOR_VERIFY',                    'Verify Doctors', 'Ability to verify/reject a self-registered doctor, activating their login'),
    ('DOCTOR_PROFILE_MANAGE',            'Manage Own Doctor Profile', 'A doctor editing their own qualifications/specialties (Master Spec §8.9, screen #19) - registration number and verification status stay Super-Admin-only'),
    ('DOCTOR_FACILITY_MAPPING_MANAGE',   'Manage Own Facility Mappings', 'Doctor: accept/decline/end own facility mapping requests (Master Spec §8.1)'),
    ('FACILITY_DOCTOR_MAPPING_MANAGE',   'Manage Facility Doctor Mappings', 'Hospital/Clinic Admin: invite/end doctor mappings at their own facility (Master Spec §4.1)'),
    ('FACILITY_DOCTOR_MAPPING_VIEW',     'View Facility Doctor Mappings', 'Read the facility''s doctor-mapping list for the Approval Queue''s aggregation, without the invite/end rights (Master Spec §17.7 #35)'),
    ('DOCTOR_STATUS_UPDATE',             'Update Own Live Status', 'Doctor: set own live status per facility/session (Master Spec §8.3)'),
    ('FACILITY_DOCTOR_STATUS_VIEW',      'View Facility Doctor Status', 'Hospital/Clinic Admin: view live status of doctors mapped to their facility (Master Spec §8.3) - backend only until the front-desk day list (build order step 5) consumes it'),
    ('RECEPTION_DOCTOR_STATUS_OVERRIDE', 'Override Doctor Status', 'Reception-side correction of a doctor''s live status at their own facility, attributed and audit-logged (Master Spec §8.3)'),
    ('CONSULTATION_FEE_PROPOSE',         'Propose Consultation Fee', 'Doctor: propose own consultation fee per facility, pending Hospital/Clinic Admin approval (Master Spec §8.6)'),
    ('CONSULTATION_FEE_APPROVE',         'Approve Consultation Fee', 'Approve/reject a doctor-proposed consultation fee (Master Spec §8.6) - completes build order step 4'),
    ('DOCTOR_SCHEDULE_MANAGE',           'Manage Doctor Schedules', 'Create/edit a mapped doctor''s recurring session capacity at the facility (Master Spec §8.2)'),
    ('DOCTOR_AVAILABILITY_MANAGE',       'Manage Own Availability', 'A doctor blocking their own schedule - leave/session-cancel/holiday - and resolving any appointments it affects (Master Spec §8.2, screen #14)'),
    ('FREE_REVIEW_POLICY_MANAGE',        'Manage Free-Review Policy', 'Per-doctor max-days/max-visits free-review policy at this facility (Master Spec §7.2)'),
    ('CONSULTATION_RECORD_MANAGE',       'Author Consultation Record', 'Doctor: author/draft/complete the clinical note and prescription for a visit (Master Spec §8.7)'),
    ('DOCTOR_REPORTS_VIEW',              'View Own Reports', 'A doctor viewing their own punctuality/consultation-count/medicines-prescribed reports (Master Spec §17.7 #18)');

-- ---------------------------------------------------------------
-- SUPER_ADMIN <- doctor rights
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('DOCTOR_VERIFY', 'DOCTOR_PROFILE_MANAGE', 'DOCTOR_FACILITY_MAPPING_MANAGE',
                         'FACILITY_DOCTOR_MAPPING_MANAGE', 'FACILITY_DOCTOR_MAPPING_VIEW', 'DOCTOR_STATUS_UPDATE',
                         'FACILITY_DOCTOR_STATUS_VIEW', 'RECEPTION_DOCTOR_STATUS_OVERRIDE', 'CONSULTATION_FEE_PROPOSE',
                         'CONSULTATION_FEE_APPROVE', 'DOCTOR_SCHEDULE_MANAGE', 'DOCTOR_AVAILABILITY_MANAGE',
                         'FREE_REVIEW_POLICY_MANAGE', 'CONSULTATION_RECORD_MANAGE', 'DOCTOR_REPORTS_VIEW');

-- ---------------------------------------------------------------
-- HOSPITAL_ADMIN <- doctor rights
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('FACILITY_DOCTOR_MAPPING_MANAGE', 'FACILITY_DOCTOR_MAPPING_VIEW', 'FACILITY_DOCTOR_STATUS_VIEW',
                         'RECEPTION_DOCTOR_STATUS_OVERRIDE', 'CONSULTATION_FEE_APPROVE', 'DOCTOR_SCHEDULE_MANAGE',
                         'FREE_REVIEW_POLICY_MANAGE');

-- ---------------------------------------------------------------
-- DOCTOR <- doctor rights
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'DOCTOR'
  AND rt.right_code IN ('DOCTOR_PROFILE_MANAGE', 'DOCTOR_FACILITY_MAPPING_MANAGE', 'DOCTOR_STATUS_UPDATE',
                         'CONSULTATION_FEE_PROPOSE', 'DOCTOR_AVAILABILITY_MANAGE', 'CONSULTATION_RECORD_MANAGE',
                         'DOCTOR_REPORTS_VIEW');

-- ---------------------------------------------------------------
-- RECEPTIONIST <- doctor rights
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'RECEPTIONIST'
  AND rt.right_code IN ('FACILITY_DOCTOR_STATUS_VIEW', 'RECEPTION_DOCTOR_STATUS_OVERRIDE');
