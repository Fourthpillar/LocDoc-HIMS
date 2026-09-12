-- ============================================================
-- Version     : V35
-- Description : Step 8 - Hospital/Clinic Admin module additions
--               (Master Spec §12, §16 step 8): commission-basis
--               self-service, Manage Users (#33a), Facility profile
--               (#33b), supplier acceptance (#37 - gates PO creation),
--               and the rights the unified Approval Queue (#35) needs
--               beyond what steps 4/5/7 already seeded (discount/
--               cancellation/consultation-fee approve rights already
--               exist from V13/V18).
--
--               users.must_change_password: backs "reset a user's
--               forced-password-change flag" (#33a) - a facility admin
--               creates a Receptionist/Pharmacist account with a
--               temporary password and flips this on; enforcement of
--               the actual forced-change-on-login redirect is a login
--               concern, out of scope for this pass same as the rest
--               of this migration's columns being additive-only.
--
--               suppliers.accepted: Master Spec §12/§31a - "a supplier
--               must be accepted by Hospital/Clinic Admin at this
--               facility before a Pharmacist can raise a PO against
--               them." Defaults FALSE for newly-created suppliers
--               going forward (PurchaseOrderService now checks this),
--               but every supplier already in this database was
--               already being used to raise POs before this column
--               existed - backfilled TRUE below so this migration
--               doesn't retroactively lock out existing data.
--
--               commission_basis: Master Spec §6 - "self-service,
--               Hospital/Clinic Admin sets this per referrer from
--               their own login." party_name is a free-text label
--               rather than an FK to ReferralDoctor/PRO - those master
--               tables (§6 Masters, screen #34) are still unbuilt, and
--               inventing a hard dependency on them isn't this
--               migration's job; §34's own Masters screen is future
--               work per §16 step 9's "remaining screen polish."
-- Author      : LockDoc App
-- ============================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS accepted BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE suppliers SET accepted = TRUE WHERE accepted = FALSE;

CREATE TABLE IF NOT EXISTS commission_basis (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id     BIGINT        NOT NULL,
    party_type      VARCHAR(40)   NOT NULL,
    party_name      VARCHAR(150)  NOT NULL,
    basis           VARCHAR(10)   NOT NULL,
    value_amount    DECIMAL(10,2) NOT NULL,
    applies_to      VARCHAR(200),
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date    TIMESTAMP     NOT NULL,
    updated_date    TIMESTAMP,
    CONSTRAINT fk_commission_basis_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_commission_basis_facility ON commission_basis (facility_id, active);

INSERT INTO rights (right_code, right_name, description) VALUES
    ('COMMISSION_BASIS_MANAGE', 'Manage Commission Basis', 'Set referral-doctor/PRO/prescribing-doctor commission terms, self-service (Master Spec §6)'),
    ('FACILITY_USER_MANAGE', 'Manage Facility Users', 'Create/deactivate Receptionist and Pharmacist accounts, reset forced-password-change flag, at own facility only (Master Spec §17.7 #33a)'),
    ('FACILITY_PROFILE_MANAGE', 'Manage Facility Profile', 'Edit own facility''s name/address/licence numbers/geo-coordinates after Super Admin verification (Master Spec §17.7 #33b)'),
    ('PHARMACY_SUPPLIER_ACCEPT', 'Accept Pharmacy Supplier', 'Facility-level supplier activation before a Pharmacist can raise a PO against them (Master Spec §12/§31a)'),
    ('FACILITY_DOCTOR_MAPPING_VIEW', 'View Facility Doctor Mappings', 'Read the facility''s doctor-mapping list for the Approval Queue''s aggregation, without the invite/end rights (Master Spec §17.7 #35)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('COMMISSION_BASIS_MANAGE', 'FACILITY_USER_MANAGE', 'FACILITY_PROFILE_MANAGE',
                         'PHARMACY_SUPPLIER_ACCEPT', 'FACILITY_DOCTOR_MAPPING_VIEW')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('COMMISSION_BASIS_MANAGE', 'FACILITY_USER_MANAGE', 'FACILITY_PROFILE_MANAGE',
                         'PHARMACY_SUPPLIER_ACCEPT', 'FACILITY_DOCTOR_MAPPING_VIEW')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
