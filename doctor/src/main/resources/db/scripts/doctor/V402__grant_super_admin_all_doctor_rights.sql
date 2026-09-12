-- ============================================================
-- Version     : V402
-- Description : Super Admin holds every doctor-module right.
--
--               V401 already granted the four facility-side rights and
--               DOCTOR_VERIFY. This adds the one that was left out —
--               RECEPTION_DOCTOR_STATUS_OVERRIDE — so no doctor screen is
--               refused to Super Admin on rights grounds.
--
--               Rights were never the real barrier, and a script alone would
--               not have lifted it: the facility-scoped services resolve the
--               acting facility from the principal, and Super Admin has none
--               (users.facility_id is NULL, which is exactly what makes them
--               cross-facility). That is handled in code, by treating "no
--               facility" as "every facility" for this role rather than as an
--               error — see SecurityUtils.facilityScopeOrAll().
--
--               The four doctor-side rights (DOCTOR_PROFILE_MANAGE,
--               DOCTOR_FACILITY_MAPPING_MANAGE, DOCTOR_STATUS_UPDATE,
--               CONSULTATION_FEE_PROPOSE) are deliberately NOT granted here.
--               Those endpoints resolve "which doctor am I" from the signed-in
--               user's doctor record, and Super Admin has none; granting the
--               rights would light up screens that could only then fail. A
--               Super Admin who also practises needs a doctor account of their
--               own, which is the honest way to hold both.
-- Author      : LockDoc App
-- ============================================================

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('DOCTOR_VERIFY', 'FACILITY_DOCTOR_MAPPING_MANAGE', 'FACILITY_DOCTOR_STATUS_VIEW',
                        'CONSULTATION_FEE_APPROVE', 'FREE_REVIEW_POLICY_MANAGE', 'RECEPTION_DOCTOR_STATUS_OVERRIDE')
  AND NOT EXISTS (SELECT 1 FROM role_rights x WHERE x.role_id = r.id AND x.right_id = rt.id);
