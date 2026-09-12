-- ============================================================
-- Version     : V41
-- Description : Package sale & utilization (Master Spec §6/§7.5, screen
--               #11) - restored per §17.7's own note ("restored, was
--               missing"). Built on top of V39/V40's billable_items -
--               "any add-on beyond the package's included list bills
--               separately at the normal rate" only means something
--               once a procedure/service can be billed as a distinct
--               line at all, which V39 just added.
--
--               packages/package_items are the master (what a package
--               contains); package_sales is the billing event (one row
--               per sale, ENCOUNTER_PACKAGE on bills, like V39's
--               procedure_bills); package_utilization is the drawdown
--               ledger - one row per included item per sale, created at
--               sale time with remaining_qty = included_qty, decremented
--               as the patient actually uses each item across visits.
--               Consuming a utilization row does not create a new Bill -
--               it was already paid for in the package sale; only a
--               genuine add-on beyond what the package includes goes
--               through ProcedureBillingService (V39) as its own line.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS packages (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id   BIGINT        NOT NULL,
    name          VARCHAR(150)  NOT NULL,
    code          VARCHAR(30),
    price         DECIMAL(10,2) NOT NULL,
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date  TIMESTAMP     NOT NULL,
    updated_date  TIMESTAMP,
    CONSTRAINT fk_package_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE TABLE IF NOT EXISTS package_items (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id        BIGINT  NOT NULL,
    billable_item_id  BIGINT  NOT NULL,
    included_qty      INT     NOT NULL,
    CONSTRAINT fk_package_item_package FOREIGN KEY (package_id) REFERENCES packages (id),
    CONSTRAINT fk_package_item_billable_item FOREIGN KEY (billable_item_id) REFERENCES billable_items (id)
);

CREATE INDEX IF NOT EXISTS idx_package_item_package ON package_items (package_id);

CREATE TABLE IF NOT EXISTS package_sales (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id         BIGINT     NOT NULL,
    patient_id          BIGINT     NOT NULL,
    package_id          BIGINT     NOT NULL,
    bill_id             BIGINT,
    created_by_user_id  BIGINT     NOT NULL,
    created_date        TIMESTAMP  NOT NULL,
    CONSTRAINT fk_package_sale_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_package_sale_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_package_sale_package FOREIGN KEY (package_id) REFERENCES packages (id),
    CONSTRAINT fk_package_sale_bill FOREIGN KEY (bill_id) REFERENCES bills (id)
);

CREATE INDEX IF NOT EXISTS idx_package_sale_patient ON package_sales (patient_id);
CREATE INDEX IF NOT EXISTS idx_package_sale_facility ON package_sales (facility_id);

CREATE TABLE IF NOT EXISTS package_utilization (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_sale_id     BIGINT  NOT NULL,
    billable_item_id    BIGINT  NOT NULL,
    included_qty        INT     NOT NULL,
    used_qty            INT     NOT NULL DEFAULT 0,
    remaining_qty        INT     NOT NULL,
    CONSTRAINT fk_package_util_sale FOREIGN KEY (package_sale_id) REFERENCES package_sales (id),
    CONSTRAINT fk_package_util_item FOREIGN KEY (billable_item_id) REFERENCES billable_items (id)
);

CREATE INDEX IF NOT EXISTS idx_package_util_sale ON package_utilization (package_sale_id);

INSERT INTO rights (right_code, right_name, description) VALUES
    ('PACKAGE_MASTERS_MANAGE', 'Manage Packages', 'Health-check/corporate package masters and their included items, own facility only (Master Spec §7.2, screen #11)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('PACKAGE_MASTERS_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('PACKAGE_MASTERS_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
