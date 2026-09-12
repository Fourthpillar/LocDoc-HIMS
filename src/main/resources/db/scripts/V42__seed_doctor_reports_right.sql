-- ============================================================
-- Version     : V42
-- Description : Doctor Reports (Master Spec §17.7 #18: punctuality,
--               consultation count, medicines prescribed) - a new right
--               rather than reusing DOCTOR_PROFILE_MANAGE, since that
--               one's description is specifically about editing
--               qualifications/specialties (V37), not viewing reports.
-- Author      : LockDoc App
-- ============================================================

INSERT INTO rights (right_code, right_name, description) VALUES
    ('DOCTOR_REPORTS_VIEW', 'View Own Reports', 'A doctor viewing their own punctuality/consultation-count/medicines-prescribed reports (Master Spec §17.7 #18)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'DOCTOR'
  AND rt.right_code IN ('DOCTOR_REPORTS_VIEW')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('DOCTOR_REPORTS_VIEW')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
