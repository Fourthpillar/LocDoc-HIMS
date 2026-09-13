-- ============================================================
-- Version     : V2002
-- Module      : outpatient
-- Description : All seed data owned by the outpatient module - the
--               RECEPTIONIST role, every OP/masters/billing/reports/
--               data-protection right, and their role mappings.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- Roles
-- ---------------------------------------------------------------
INSERT INTO roles (role_code, role_name, description) VALUES
    ('RECEPTIONIST', 'OP Receptionist', 'Front desk - registration, appointments, day list, OP billing; one facility');

-- ---------------------------------------------------------------
-- Rights
-- ---------------------------------------------------------------
INSERT INTO rights (right_code, right_name, description) VALUES
    ('PATIENT_MANAGE',                 'Manage Patients', 'Register/edit patient records (Master Spec §7.4)'),
    ('APPOINTMENT_MANAGE',             'Manage Appointments', 'Book/reschedule/cancel appointments, waitlist, view doctor schedules for booking (Master Spec §7.3)'),
    ('OP_VISIT_MANAGE',                'Manage OP Visits', 'Front-desk day list, visit capture/vitals (Master Spec §7.4)'),
    ('OP_BILLING_MANAGE',              'OP Billing', 'Registration/consultation billing, payments, due collection (Master Spec §7.5)'),
    ('OP_DISCOUNT_REQUEST',            'Request OP Discount', 'Request a bill discount, subject to the facility''s approval threshold (Master Spec §7.5)'),
    ('OP_DISCOUNT_APPROVE',            'Approve OP Discount', 'Approve/reject a discount above the facility''s threshold'),
    ('OP_CANCELLATION_REQUEST',        'Request OP Cancellation', 'Request an appointment/bill cancellation - always Pending Approval (Master Spec §4.1)'),
    ('OP_CANCELLATION_APPROVE',        'Approve OP Cancellation', 'Approve/reject a cancellation request'),
    ('REGISTRATION_FEE_CONFIG_MANAGE', 'Manage Registration Fee Config', 'Set the facility''s registration/re-registration fee and validity period (Master Spec §7.2, self-service)'),
    ('APPROVAL_POLICY_MANAGE',         'Manage Approval Policies', 'Set the facility''s own discount-approval threshold, self-service (Master Spec §7.5/§15.1)'),
    ('COUNTER_SESSION_MANAGE',         'Manage Counter Session', 'Open/close a counter session and reconcile declared cash against system total (Master Spec §7.5/§11.5, screens #12/#31)'),
    ('FACILITY_MASTERS_MANAGE',        'Manage Facility Masters', 'Referral doctors, PRO, Organization/TPA masters, own facility only (Master Spec §7.2, screen #34)'),
    ('AREA_MASTERS_MANAGE',            'Manage Area Master', 'Country/State/City/Area master for patient registration, own facility only (Master Spec §7.2)'),
    ('PROCEDURE_MASTERS_MANAGE',       'Manage Procedure/Service Charges', 'Procedure and service charge masters, own facility only, org-type-dimensioned rates (Master Spec §7.2)'),
    ('PACKAGE_MASTERS_MANAGE',         'Manage Packages', 'Health-check/corporate package masters and their included items, own facility only (Master Spec §7.2, screen #11)'),
    ('COMMISSION_BASIS_MANAGE',        'Manage Commission Basis', 'Set referral-doctor/PRO commission terms, self-service (Master Spec §6)'),
    ('FACILITY_REPORTS_VIEW',          'View Facility Reports & Audit Log', 'MLC register and this facility''s own slice of the audit trail (Master Spec §17.7 #38)'),
    ('PATIENT_CONSENT_MANAGE',         'Manage Patient Consent', 'Capture and search treatment/data-processing consent records (Master Spec §18.2, screen #38a)'),
    ('DATA_PROTECTION_ERASURE_MANAGE', 'Manage Erasure Requests', 'Intake and resolve DPDP erasure/grievance requests (Master Spec §18.4/§18.6, screen #38b)');

-- ---------------------------------------------------------------
-- SUPER_ADMIN <- outpatient rights
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('PATIENT_MANAGE', 'APPOINTMENT_MANAGE', 'OP_VISIT_MANAGE',
                         'OP_BILLING_MANAGE', 'OP_DISCOUNT_REQUEST', 'OP_DISCOUNT_APPROVE',
                         'OP_CANCELLATION_REQUEST', 'OP_CANCELLATION_APPROVE', 'REGISTRATION_FEE_CONFIG_MANAGE',
                         'APPROVAL_POLICY_MANAGE', 'COUNTER_SESSION_MANAGE', 'FACILITY_MASTERS_MANAGE',
                         'AREA_MASTERS_MANAGE', 'PROCEDURE_MASTERS_MANAGE', 'PACKAGE_MASTERS_MANAGE',
                         'COMMISSION_BASIS_MANAGE', 'FACILITY_REPORTS_VIEW', 'PATIENT_CONSENT_MANAGE',
                         'DATA_PROTECTION_ERASURE_MANAGE');

-- ---------------------------------------------------------------
-- HOSPITAL_ADMIN <- outpatient rights
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('PATIENT_MANAGE', 'APPOINTMENT_MANAGE', 'OP_VISIT_MANAGE',
                         'OP_BILLING_MANAGE', 'OP_DISCOUNT_REQUEST', 'OP_DISCOUNT_APPROVE',
                         'OP_CANCELLATION_REQUEST', 'OP_CANCELLATION_APPROVE', 'REGISTRATION_FEE_CONFIG_MANAGE',
                         'APPROVAL_POLICY_MANAGE', 'FACILITY_MASTERS_MANAGE', 'AREA_MASTERS_MANAGE',
                         'PROCEDURE_MASTERS_MANAGE', 'PACKAGE_MASTERS_MANAGE', 'COMMISSION_BASIS_MANAGE',
                         'FACILITY_REPORTS_VIEW', 'PATIENT_CONSENT_MANAGE', 'DATA_PROTECTION_ERASURE_MANAGE');

-- ---------------------------------------------------------------
-- RECEPTIONIST <- outpatient rights
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'RECEPTIONIST'
  AND rt.right_code IN ('PATIENT_MANAGE', 'APPOINTMENT_MANAGE', 'OP_VISIT_MANAGE',
                         'OP_BILLING_MANAGE', 'OP_DISCOUNT_REQUEST', 'OP_CANCELLATION_REQUEST',
                         'COUNTER_SESSION_MANAGE', 'PATIENT_CONSENT_MANAGE');
