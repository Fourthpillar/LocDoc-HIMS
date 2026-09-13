-- ============================================================
-- Version     : V1002
-- Module      : common
-- Description : All seed data owned by the common module - the
--               platform roles (SUPER_ADMIN, HOSPITAL_ADMIN, DOCTOR),
--               identity/facility/platform rights and their role
--               mappings, the bootstrap facility, and the two
--               bootstrap users.
--
--   Default credentials (BCrypt-hashed below - change immediately
--   in any real environment):
--     FP_USER          / FP@Admin#123  (SUPER_ADMIN, no facility)
--     BOOTSTRAP_ADMIN  / FP@Admin#123  (HOSPITAL_ADMIN, bootstrap facility)
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- Roles
-- ---------------------------------------------------------------
INSERT INTO roles (role_code, role_name, description) VALUES
    ('SUPER_ADMIN', 'Super Administrator', 'Default super administrator role with full access'),
    ('HOSPITAL_ADMIN', 'Hospital/Clinic Admin', 'Facility operations/finance manager - scoped to exactly one facility'),
    ('DOCTOR', 'Doctor', 'Practising clinician - own availability, live status, prescriptions; no facility administration');

-- ---------------------------------------------------------------
-- Rights
-- ---------------------------------------------------------------
INSERT INTO rights (right_code, right_name, description) VALUES
    ('USER_CREATE',             'Create User', 'Ability to create new users'),
    ('USER_READ',               'View User', 'Ability to view user details'),
    ('USER_UPDATE',             'Update User', 'Ability to update user details'),
    ('USER_DELETE',             'Delete User', 'Ability to delete users'),
    ('ROLE_MANAGE',             'Manage Roles', 'Ability to create/update/delete roles'),
    ('RIGHT_MANAGE',            'Manage Rights', 'Ability to create/update/delete rights'),
    ('SYSTEM_ADMIN',            'System Admin', 'Full administrative access to the system'),
    ('FACILITY_MANAGE',         'Manage Facilities', 'Ability to register and view facilities'),
    ('FACILITY_VERIFY',         'Verify Facilities', 'Ability to verify/reject/suspend/reinstate facilities and manage their module entitlements'),
    ('FACILITY_USER_MANAGE',    'Manage Facility Users', 'Create/deactivate Receptionist accounts, reset forced-password-change flag, at own facility only (Master Spec §17.7 #33a)'),
    ('FACILITY_PROFILE_MANAGE', 'Manage Facility Profile', 'Edit own facility''s name/address/licence numbers/geo-coordinates after Super Admin verification (Master Spec §17.7 #33b)'),
    ('AUDIT_LOG_VIEW',          'View Cross-facility Audit Log', 'Super Admin platform-wide audit trail (Master Spec §10, screen #42)'),
    ('SUPPORT_TICKET_CREATE',   'Raise Support Ticket', 'Raise a support/dispute ticket to Super Admin (Master Spec §10)'),
    ('SUPPORT_TICKET_MANAGE',   'Triage Support Tickets', 'Super Admin triage/resolve of facility-raised support tickets (Master Spec §10, screen #42a)');

-- ---------------------------------------------------------------
-- SUPER_ADMIN <- common rights
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('USER_CREATE', 'USER_READ', 'USER_UPDATE',
                         'USER_DELETE', 'ROLE_MANAGE', 'RIGHT_MANAGE',
                         'SYSTEM_ADMIN', 'FACILITY_MANAGE', 'FACILITY_VERIFY',
                         'FACILITY_USER_MANAGE', 'FACILITY_PROFILE_MANAGE', 'AUDIT_LOG_VIEW',
                         'SUPPORT_TICKET_CREATE', 'SUPPORT_TICKET_MANAGE');

-- ---------------------------------------------------------------
-- HOSPITAL_ADMIN <- common rights
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('FACILITY_USER_MANAGE', 'FACILITY_PROFILE_MANAGE', 'SUPPORT_TICKET_CREATE');

-- ---------------------------------------------------------------
-- DOCTOR <- common rights
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'DOCTOR'
  AND rt.right_code IN ('SUPPORT_TICKET_CREATE');

-- ---------------------------------------------------------------
-- Bootstrap facility - lets BOOTSTRAP_ADMIN exercise facility-side
-- endpoints before Super Admin onboards a real facility.
-- ---------------------------------------------------------------
INSERT INTO facilities (name, type, verification_status, active, created_date, updated_date) VALUES
    ('Default Facility (bootstrap)', 'HOSPITAL', 'VERIFIED', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO facility_modules (facility_id, module_code)
SELECT f.id, m.module_code
FROM facilities f, (VALUES ('OP'), ('DOCTOR')) AS m(module_code)
WHERE f.name = 'Default Facility (bootstrap)';

-- ---------------------------------------------------------------
-- Bootstrap users
-- ---------------------------------------------------------------
INSERT INTO users (username, password, email, full_name, facility_id, enabled, account_non_locked, created_date) VALUES
    ('FP_USER', '$2b$10$wziGg47emGStkmCTSreXIebA9AA3t38z2uNYbWVqdz/6ZVsBktGNq', 'fp_user@lockdoc.local', 'FP Super Admin', NULL, TRUE, TRUE, CURRENT_TIMESTAMP);

INSERT INTO users (username, password, email, full_name, facility_id, enabled, account_non_locked, created_date)
SELECT 'BOOTSTRAP_ADMIN', '$2b$10$wziGg47emGStkmCTSreXIebA9AA3t38z2uNYbWVqdz/6ZVsBktGNq', 'admin@bootstrap-facility.local', 'Bootstrap Facility Admin', f.id, TRUE, TRUE, CURRENT_TIMESTAMP
FROM facilities f
WHERE f.name = 'Default Facility (bootstrap)';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE (u.username = 'FP_USER' AND r.role_code = 'SUPER_ADMIN')
   OR (u.username = 'BOOTSTRAP_ADMIN' AND r.role_code = 'HOSPITAL_ADMIN');
