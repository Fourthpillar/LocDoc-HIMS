-- ============================================================
-- Version     : V29
-- Description : Step 7 of the LocDoc-HIMS build order (Master Spec
--               §16/§11.3-§11.4) - Pharmacy completion. New rights for
--               store management (genuinely needed now - indent/
--               transfer are "meaningless with one store", per
--               StoreResolutionService's own pre-existing comment),
--               indent, inter-store transfer, purchase return, and
--               physical stock count. Approval rights follow the same
--               pattern V11 already established for
--               PHARMACY_PURCHASE_ORDER_APPROVE - granted to
--               HOSPITAL_ADMIN, the facility's own approval authority
--               (§5 principle 4).
-- Author      : LockDoc App
-- ============================================================

INSERT INTO rights (right_code, right_name, description) VALUES
    ('PHARMACY_STORE_MANAGE',           'Manage Stores',              'Create/edit stores within a facility (Master Spec §6/§11.2)'),
    ('PHARMACY_INDENT_CREATE',          'Create Indent',              'Raise/submit an indent from one store against another (Master Spec §11.3)'),
    ('PHARMACY_INDENT_APPROVE',         'Approve Indent',             'Approve/reject/issue an indent'),
    ('PHARMACY_TRANSFER_CREATE',        'Create Inter-store Transfer','Issue/receive stock moving between stores (Master Spec §11.4)'),
    ('PHARMACY_PURCHASE_RETURN_CREATE', 'Create Purchase Return',     'Debit note against an already-accepted GRN batch (Master Spec §11.3)'),
    ('PHARMACY_STOCK_COUNT_CREATE',     'Create Stock Count',         'Raise a physical stock count and enter counted quantities (Master Spec §11.4)'),
    ('PHARMACY_STOCK_COUNT_APPROVE',    'Approve Stock Count',        'Approve a stock count''s variance, posting it to stock');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'PHARMACIST'
  AND rt.right_code IN ('PHARMACY_STORE_MANAGE', 'PHARMACY_INDENT_CREATE', 'PHARMACY_TRANSFER_CREATE',
                         'PHARMACY_PURCHASE_RETURN_CREATE', 'PHARMACY_STOCK_COUNT_CREATE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

-- Hospital/Clinic Admin: approval authority plus the same read/oversight
-- rights §4.1 already describes it having across pharmacy generally -
-- never previously granted because nothing needed them until this step.
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('PHARMACY_STORE_MANAGE', 'PHARMACY_INDENT_APPROVE', 'PHARMACY_STOCK_COUNT_APPROVE',
                         'PHARMACY_INVENTORY_READ', 'PHARMACY_REPORT_READ')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('PHARMACY_STORE_MANAGE', 'PHARMACY_INDENT_CREATE', 'PHARMACY_INDENT_APPROVE',
                         'PHARMACY_TRANSFER_CREATE', 'PHARMACY_PURCHASE_RETURN_CREATE',
                         'PHARMACY_STOCK_COUNT_CREATE', 'PHARMACY_STOCK_COUNT_APPROVE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
