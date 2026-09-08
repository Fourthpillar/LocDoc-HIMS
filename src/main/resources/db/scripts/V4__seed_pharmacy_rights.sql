-- ============================================================
-- Version     : V4
-- Description : Seed pharmacy module rights, map them to the
--               existing SUPER_ADMIN role, and add a new
--               PHARMACIST role mapped to all pharmacy rights.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- Pharmacy rights (permissions)
-- ---------------------------------------------------------------
INSERT INTO rights (right_code, right_name, description) VALUES
    ('PHARMACY_MEDICINE_MANAGE',        'Manage Medicines',            'Ability to create/update/deactivate medicines'),
    ('PHARMACY_SUPPLIER_MANAGE',        'Manage Suppliers',            'Ability to create/update/deactivate suppliers'),
    ('PHARMACY_PATIENT_MANAGE',         'Manage Patients',             'Ability to create/update/deactivate patients'),
    ('PHARMACY_PURCHASE_ORDER_CREATE',  'Create Purchase Order',       'Ability to create and view purchase orders'),
    ('PHARMACY_PURCHASE_ORDER_APPROVE', 'Approve Purchase Order',      'Ability to approve/cancel purchase orders'),
    ('PHARMACY_PURCHASE_CREATE',        'Create Purchase (GRN)',       'Ability to create/cancel purchases (goods receipt)'),
    ('PHARMACY_SALE_CREATE',            'Create Sale',                 'Ability to create/cancel sales invoices'),
    ('PHARMACY_SALE_RETURN_CREATE',     'Create Sales Return',         'Ability to create/cancel sales returns'),
    ('PHARMACY_INVENTORY_READ',         'View Inventory',              'Ability to view stock/inventory levels'),
    ('PHARMACY_REPORT_READ',            'View Pharmacy Reports',       'Ability to view pharmacy reports');

-- ---------------------------------------------------------------
-- Map every pharmacy right to the existing SUPER_ADMIN role
-- ---------------------------------------------------------------
INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN (
    'PHARMACY_MEDICINE_MANAGE', 'PHARMACY_SUPPLIER_MANAGE', 'PHARMACY_PATIENT_MANAGE',
    'PHARMACY_PURCHASE_ORDER_CREATE', 'PHARMACY_PURCHASE_ORDER_APPROVE', 'PHARMACY_PURCHASE_CREATE',
    'PHARMACY_SALE_CREATE', 'PHARMACY_SALE_RETURN_CREATE', 'PHARMACY_INVENTORY_READ', 'PHARMACY_REPORT_READ'
  )
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

-- ---------------------------------------------------------------
-- New PHARMACIST role, mapped to all pharmacy rights
-- ---------------------------------------------------------------
INSERT INTO roles (role_code, role_name, description) VALUES
    ('PHARMACIST', 'Pharmacist', 'Pharmacy module role with full access to pharmacy operations');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'PHARMACIST'
  AND rt.right_code IN (
    'PHARMACY_MEDICINE_MANAGE', 'PHARMACY_SUPPLIER_MANAGE', 'PHARMACY_PATIENT_MANAGE',
    'PHARMACY_PURCHASE_ORDER_CREATE', 'PHARMACY_PURCHASE_ORDER_APPROVE', 'PHARMACY_PURCHASE_CREATE',
    'PHARMACY_SALE_CREATE', 'PHARMACY_SALE_RETURN_CREATE', 'PHARMACY_INVENTORY_READ', 'PHARMACY_REPORT_READ'
  );
