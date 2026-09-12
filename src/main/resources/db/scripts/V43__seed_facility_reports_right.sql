-- ============================================================
-- Version     : V43
-- Description : Facility reports & audit log, incl. MLC register (Master
--               Spec §17.7 #38, Hospital/Clinic Admin). One right for
--               both halves of this one screen, matching how the spec
--               names it as a single row, not two.
--
--               Scope note: "Facility reports" here is the two
--               concretely-named deliverables the spec actually calls
--               out for this screen - the MLC register (§7.5, "the flag
--               alone isn't the deliverable, the register is") and a
--               facility-scoped view of the audit trail AuditLogService
--               already writes (its own bounded-action-set javadoc still
--               applies unchanged - this doesn't add new write-sites,
--               only a new facility-scoped read). It is not a new,
--               unnamed catalogue of additional reports invented for
--               this pass - OP Reports (#12a) and Pharmacy Reports
--               already cover this facility's operational reporting.
-- Author      : LockDoc App
-- ============================================================

INSERT INTO rights (right_code, right_name, description) VALUES
    ('FACILITY_REPORTS_VIEW', 'View Facility Reports & Audit Log', 'MLC register and this facility''s own slice of the audit trail (Master Spec §17.7 #38)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('FACILITY_REPORTS_VIEW')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('FACILITY_REPORTS_VIEW')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
