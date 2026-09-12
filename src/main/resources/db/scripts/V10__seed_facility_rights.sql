-- ============================================================
-- Version     : V10
-- Description : Step 2 of the LocDoc-HIMS build order (Master Spec
--               §16) - Super Admin core. Seeds the two rights that
--               gate facility onboarding/verification, following the
--               exact CREATE-right / APPROVE-right split V4 already
--               established for pharmacy purchase orders:
--                 FACILITY_MANAGE  - register/list/view facilities
--                 FACILITY_VERIFY  - verify/reject/suspend/reinstate
--                                    a facility, and manage its
--                                    active-module entitlements
--               Both map only to SUPER_ADMIN - no other role in this
--               system ever manages facilities (Master Spec §4).
-- Author      : LockDoc App
-- ============================================================

INSERT INTO rights (right_code, right_name, description) VALUES
    ('FACILITY_MANAGE', 'Manage Facilities', 'Ability to register and view facilities'),
    ('FACILITY_VERIFY', 'Verify Facilities', 'Ability to verify/reject/suspend/reinstate facilities and manage their module entitlements');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('FACILITY_MANAGE', 'FACILITY_VERIFY')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
