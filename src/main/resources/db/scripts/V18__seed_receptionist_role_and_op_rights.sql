-- ============================================================
-- Version     : V18
-- Description : Step 5 of the LocDoc-HIMS build order (Master Spec
--               §16) - OP Module. Seeds RECEPTIONIST (the one role that
--               existed in every prose description of this system but
--               had never actually been created in the database) and
--               every new right the OP Module and its Hospital/Clinic
--               Admin counterparts need. CONSULTATION_FEE_APPROVE is
--               here rather than V13 because step 4 deliberately built
--               only the propose half (§8.6) - this migration is the
--               one that completes it.
-- Author      : LockDoc App
-- ============================================================

INSERT INTO roles (role_code, role_name, description) VALUES
    ('RECEPTIONIST', 'OP Receptionist', 'Front desk - registration, appointments, day list, OP billing; one facility');

INSERT INTO rights (right_code, right_name, description) VALUES
    ('PATIENT_MANAGE', 'Manage Patients', 'Register/edit patient records (Master Spec §7.4)'),
    ('APPOINTMENT_MANAGE', 'Manage Appointments', 'Book/reschedule/cancel appointments, waitlist, view doctor schedules for booking (Master Spec §7.3)'),
    ('OP_VISIT_MANAGE', 'Manage OP Visits', 'Front-desk day list, visit capture/vitals (Master Spec §7.4)'),
    ('OP_BILLING_MANAGE', 'OP Billing', 'Registration/consultation billing, payments, due collection (Master Spec §7.5)'),
    ('OP_DISCOUNT_REQUEST', 'Request OP Discount', 'Request a bill discount, subject to the facility''s approval threshold (Master Spec §7.5)'),
    ('OP_DISCOUNT_APPROVE', 'Approve OP Discount', 'Approve/reject a discount above the facility''s threshold'),
    ('OP_CANCELLATION_REQUEST', 'Request OP Cancellation', 'Request an appointment/bill cancellation - always Pending Approval (Master Spec §4.1)'),
    ('OP_CANCELLATION_APPROVE', 'Approve OP Cancellation', 'Approve/reject a cancellation request'),
    ('RECEPTION_DOCTOR_STATUS_OVERRIDE', 'Override Doctor Status', 'Reception-side correction of a doctor''s live status at their own facility, attributed and audit-logged (Master Spec §8.3)'),
    ('REGISTRATION_FEE_CONFIG_MANAGE', 'Manage Registration Fee Config', 'Set the facility''s registration/re-registration fee and validity period (Master Spec §7.2, self-service)'),
    ('DOCTOR_SCHEDULE_MANAGE', 'Manage Doctor Schedules', 'Create/edit a mapped doctor''s recurring session capacity at the facility (Master Spec §8.2)'),
    ('APPROVAL_POLICY_MANAGE', 'Manage Approval Policies', 'Set the facility''s own discount-approval threshold, self-service (Master Spec §7.5/§15.1)'),
    ('CONSULTATION_FEE_APPROVE', 'Approve Consultation Fee', 'Approve/reject a doctor-proposed consultation fee (Master Spec §8.6) - completes build order step 4');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'RECEPTIONIST'
  AND rt.right_code IN ('PATIENT_MANAGE', 'APPOINTMENT_MANAGE', 'OP_VISIT_MANAGE', 'OP_BILLING_MANAGE',
                         'OP_DISCOUNT_REQUEST', 'OP_CANCELLATION_REQUEST', 'RECEPTION_DOCTOR_STATUS_OVERRIDE',
                         'FACILITY_DOCTOR_STATUS_VIEW');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('PATIENT_MANAGE', 'APPOINTMENT_MANAGE', 'OP_VISIT_MANAGE', 'OP_BILLING_MANAGE',
                         'OP_DISCOUNT_REQUEST', 'OP_DISCOUNT_APPROVE', 'OP_CANCELLATION_REQUEST', 'OP_CANCELLATION_APPROVE',
                         'RECEPTION_DOCTOR_STATUS_OVERRIDE', 'REGISTRATION_FEE_CONFIG_MANAGE', 'DOCTOR_SCHEDULE_MANAGE',
                         'APPROVAL_POLICY_MANAGE', 'CONSULTATION_FEE_APPROVE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('PATIENT_MANAGE', 'APPOINTMENT_MANAGE', 'OP_VISIT_MANAGE', 'OP_BILLING_MANAGE',
                         'OP_DISCOUNT_REQUEST', 'OP_DISCOUNT_APPROVE', 'OP_CANCELLATION_REQUEST', 'OP_CANCELLATION_APPROVE',
                         'RECEPTION_DOCTOR_STATUS_OVERRIDE', 'REGISTRATION_FEE_CONFIG_MANAGE', 'DOCTOR_SCHEDULE_MANAGE',
                         'APPROVAL_POLICY_MANAGE', 'CONSULTATION_FEE_APPROVE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
