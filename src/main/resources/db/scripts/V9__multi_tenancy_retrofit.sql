-- ============================================================
-- Version     : V9
-- Description : Multi-tenancy retrofit - Step 1 of the LocDoc-HIMS
--               build order (see LocDoc-HIMS-Master-Spec.md §16).
--
--               Before this script, nothing in the schema carried a
--               facility_id - the system was implicitly single-tenant,
--               directly contradicting the "multi-tenant from the data
--               model up" architecture principle (Master Spec §5.1).
--               This script:
--
--                 1. Introduces FACILITIES, FACILITY_MODULES and STORES.
--                 2. Adds facility_id to every existing tenant-scoped
--                    table (users, patients, medicines, suppliers,
--                    purchase_orders, purchases, sales_invoices,
--                    sales_returns), and store_id additionally to the
--                    stock-bearing tables (medicine_batches,
--                    stock_ledger_entries, purchase_orders, purchases,
--                    sales_invoices).
--                 3. Seeds one bootstrap facility + store and backfills
--                    every existing row against them, so this migration
--                    is safe to run against the existing demo data
--                    (V5/V6 seed suppliers/medicines/batches/ledger).
--                 4. Makes facility_id NOT NULL everywhere it is set
--                    except users.facility_id, which stays nullable -
--                    that column is null specifically for Super Admin,
--                    the one role scoped across every facility rather
--                    than to one (Master Spec §4). FP_USER (SUPER_ADMIN)
--                    is deliberately excluded from the users backfill
--                    below for exactly this reason.
--                 5. Widens what were globally-unique document/code
--                    columns (medicine code, patient MRN, PO/GRN/
--                    invoice/return numbers) to unique-per-facility,
--                    and document_sequences' composite key gains
--                    facility_id, since number series are per-facility
--                    per-financial-year (Master Spec §6).
--
--               Line-item tables (purchase_order_items, purchase_items,
--               sales_invoice_items, sales_return_items) deliberately do
--               NOT get their own facility_id - they are reached through
--               their already-scoped parent header, which is the
--               standard aggregate-root pattern and avoids redundant
--               columns that would need to stay in sync with the parent.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- FACILITIES
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS facilities (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(200)  NOT NULL,
    type                VARCHAR(20)   NOT NULL,
    address             VARCHAR(300),
    geo_lat             DECIMAL(10,7),
    geo_lng             DECIMAL(10,7),
    licence_number      VARCHAR(100),
    verification_status VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    active              BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP     NOT NULL,
    updated_date        TIMESTAMP
);

-- ---------------------------------------------------------------
-- FACILITY_MODULES (Facility.activeModules @ElementCollection)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS facility_modules (
    facility_id  BIGINT       NOT NULL,
    module_code  VARCHAR(20)  NOT NULL,
    PRIMARY KEY (facility_id, module_code),
    CONSTRAINT fk_facility_modules_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

-- ---------------------------------------------------------------
-- STORES
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS stores (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id   BIGINT        NOT NULL,
    name          VARCHAR(150)  NOT NULL,
    code          VARCHAR(30)   NOT NULL,
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date  TIMESTAMP     NOT NULL,
    updated_date  TIMESTAMP,
    CONSTRAINT fk_stores_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT uq_stores_facility_code UNIQUE (facility_id, code)
);

-- ---------------------------------------------------------------
-- USER_STORE_SCOPE - schema only in this migration. A Pharmacist may
-- be scoped to one or more stores within their facility (Master Spec
-- §4); enforcing it is real, separate work that lands with the
-- multi-store Pharmacy-completion features (build order step 7), not
-- here - a facility with one store has nothing to scope yet.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_store_scope (
    user_id   BIGINT NOT NULL,
    store_id  BIGINT NOT NULL,
    PRIMARY KEY (user_id, store_id),
    CONSTRAINT fk_user_store_scope_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_store_scope_store FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------
-- Bootstrap facility + store, to backfill every existing row against.
-- ---------------------------------------------------------------
INSERT INTO facilities (name, type, verification_status, active, created_date, updated_date)
SELECT 'Default Facility (migration bootstrap)', 'PHARMACY', 'VERIFIED', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM facilities WHERE name = 'Default Facility (migration bootstrap)');

INSERT INTO facility_modules (facility_id, module_code)
SELECT f.id, 'PHARMACY' FROM facilities f
WHERE f.name = 'Default Facility (migration bootstrap)'
  AND NOT EXISTS (SELECT 1 FROM facility_modules fm WHERE fm.facility_id = f.id AND fm.module_code = 'PHARMACY');

INSERT INTO stores (facility_id, name, code, active, created_date, updated_date)
SELECT f.id, 'Main Store', 'MAIN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM facilities f
WHERE f.name = 'Default Facility (migration bootstrap)'
  AND NOT EXISTS (SELECT 1 FROM stores s WHERE s.facility_id = f.id AND s.code = 'MAIN');

-- ---------------------------------------------------------------
-- USERS - facility_id stays NULLABLE (Super Admin has none). Backfill
-- excludes anyone holding SUPER_ADMIN, so FP_USER is left untouched.
-- ---------------------------------------------------------------
ALTER TABLE users ADD COLUMN IF NOT EXISTS facility_id BIGINT;

UPDATE users SET facility_id = (SELECT id FROM facilities WHERE name = 'Default Facility (migration bootstrap)')
WHERE facility_id IS NULL
  AND id NOT IN (
      SELECT ur.user_id FROM user_roles ur JOIN roles r ON r.id = ur.role_id WHERE r.role_code = 'SUPER_ADMIN'
  );

ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS fk_users_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);

-- ---------------------------------------------------------------
-- PATIENTS
-- ---------------------------------------------------------------
ALTER TABLE patients ADD COLUMN IF NOT EXISTS facility_id BIGINT;
UPDATE patients SET facility_id = (SELECT id FROM facilities WHERE name = 'Default Facility (migration bootstrap)') WHERE facility_id IS NULL;
ALTER TABLE patients ALTER COLUMN facility_id BIGINT NOT NULL;
ALTER TABLE patients ADD CONSTRAINT IF NOT EXISTS fk_patients_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);

ALTER TABLE patients DROP CONSTRAINT IF EXISTS uq_patients_mrn;
ALTER TABLE patients ADD CONSTRAINT uq_patients_facility_mrn UNIQUE (facility_id, mrn);

-- ---------------------------------------------------------------
-- MEDICINES
-- ---------------------------------------------------------------
ALTER TABLE medicines ADD COLUMN IF NOT EXISTS facility_id BIGINT;
UPDATE medicines SET facility_id = (SELECT id FROM facilities WHERE name = 'Default Facility (migration bootstrap)') WHERE facility_id IS NULL;
ALTER TABLE medicines ALTER COLUMN facility_id BIGINT NOT NULL;
ALTER TABLE medicines ADD CONSTRAINT IF NOT EXISTS fk_medicines_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);

ALTER TABLE medicines DROP CONSTRAINT IF EXISTS uq_medicines_code;
ALTER TABLE medicines ADD CONSTRAINT uq_medicines_facility_code UNIQUE (facility_id, code);

-- ---------------------------------------------------------------
-- SUPPLIERS
-- ---------------------------------------------------------------
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS facility_id BIGINT;
UPDATE suppliers SET facility_id = (SELECT id FROM facilities WHERE name = 'Default Facility (migration bootstrap)') WHERE facility_id IS NULL;
ALTER TABLE suppliers ALTER COLUMN facility_id BIGINT NOT NULL;
ALTER TABLE suppliers ADD CONSTRAINT IF NOT EXISTS fk_suppliers_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);

-- ---------------------------------------------------------------
-- MEDICINE_BATCHES - facility_id + store_id
-- ---------------------------------------------------------------
ALTER TABLE medicine_batches ADD COLUMN IF NOT EXISTS facility_id BIGINT;
ALTER TABLE medicine_batches ADD COLUMN IF NOT EXISTS store_id BIGINT;

UPDATE medicine_batches SET
    facility_id = (SELECT id FROM facilities WHERE name = 'Default Facility (migration bootstrap)'),
    store_id = (SELECT s.id FROM stores s JOIN facilities f ON f.id = s.facility_id
                WHERE f.name = 'Default Facility (migration bootstrap)' AND s.code = 'MAIN')
WHERE facility_id IS NULL;

ALTER TABLE medicine_batches ALTER COLUMN facility_id BIGINT NOT NULL;
ALTER TABLE medicine_batches ALTER COLUMN store_id BIGINT NOT NULL;
ALTER TABLE medicine_batches ADD CONSTRAINT IF NOT EXISTS fk_medicine_batches_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);
ALTER TABLE medicine_batches ADD CONSTRAINT IF NOT EXISTS fk_medicine_batches_store FOREIGN KEY (store_id) REFERENCES stores (id);

-- Was (medicine_id, batch_no) - widened to include store_id, since the
-- same supplier batch number can legitimately land in two different
-- stores of the same facility as two separate stock records.
ALTER TABLE medicine_batches DROP CONSTRAINT IF EXISTS uq_medicine_batches_medicine_batch;
ALTER TABLE medicine_batches ADD CONSTRAINT uq_medicine_batches_store_medicine_batch UNIQUE (store_id, medicine_id, batch_no);

-- ---------------------------------------------------------------
-- STOCK_LEDGER_ENTRIES - facility_id + store_id
-- ---------------------------------------------------------------
ALTER TABLE stock_ledger_entries ADD COLUMN IF NOT EXISTS facility_id BIGINT;
ALTER TABLE stock_ledger_entries ADD COLUMN IF NOT EXISTS store_id BIGINT;

UPDATE stock_ledger_entries SET
    facility_id = (SELECT id FROM facilities WHERE name = 'Default Facility (migration bootstrap)'),
    store_id = (SELECT s.id FROM stores s JOIN facilities f ON f.id = s.facility_id
                WHERE f.name = 'Default Facility (migration bootstrap)' AND s.code = 'MAIN')
WHERE facility_id IS NULL;

ALTER TABLE stock_ledger_entries ALTER COLUMN facility_id BIGINT NOT NULL;
ALTER TABLE stock_ledger_entries ALTER COLUMN store_id BIGINT NOT NULL;
ALTER TABLE stock_ledger_entries ADD CONSTRAINT IF NOT EXISTS fk_stock_ledger_entries_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);
ALTER TABLE stock_ledger_entries ADD CONSTRAINT IF NOT EXISTS fk_stock_ledger_entries_store FOREIGN KEY (store_id) REFERENCES stores (id);

-- ---------------------------------------------------------------
-- PURCHASE_ORDERS - facility_id + store_id
-- ---------------------------------------------------------------
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS facility_id BIGINT;
ALTER TABLE purchase_orders ADD COLUMN IF NOT EXISTS store_id BIGINT;

UPDATE purchase_orders SET
    facility_id = (SELECT id FROM facilities WHERE name = 'Default Facility (migration bootstrap)'),
    store_id = (SELECT s.id FROM stores s JOIN facilities f ON f.id = s.facility_id
                WHERE f.name = 'Default Facility (migration bootstrap)' AND s.code = 'MAIN')
WHERE facility_id IS NULL;

ALTER TABLE purchase_orders ALTER COLUMN facility_id BIGINT NOT NULL;
ALTER TABLE purchase_orders ALTER COLUMN store_id BIGINT NOT NULL;
ALTER TABLE purchase_orders ADD CONSTRAINT IF NOT EXISTS fk_purchase_orders_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);
ALTER TABLE purchase_orders ADD CONSTRAINT IF NOT EXISTS fk_purchase_orders_store FOREIGN KEY (store_id) REFERENCES stores (id);

ALTER TABLE purchase_orders DROP CONSTRAINT IF EXISTS uq_purchase_orders_po_number;
ALTER TABLE purchase_orders ADD CONSTRAINT uq_purchase_orders_facility_po_number UNIQUE (facility_id, po_number);

-- ---------------------------------------------------------------
-- PURCHASES (GRN) - facility_id + store_id
-- ---------------------------------------------------------------
ALTER TABLE purchases ADD COLUMN IF NOT EXISTS facility_id BIGINT;
ALTER TABLE purchases ADD COLUMN IF NOT EXISTS store_id BIGINT;

UPDATE purchases SET
    facility_id = (SELECT id FROM facilities WHERE name = 'Default Facility (migration bootstrap)'),
    store_id = (SELECT s.id FROM stores s JOIN facilities f ON f.id = s.facility_id
                WHERE f.name = 'Default Facility (migration bootstrap)' AND s.code = 'MAIN')
WHERE facility_id IS NULL;

ALTER TABLE purchases ALTER COLUMN facility_id BIGINT NOT NULL;
ALTER TABLE purchases ALTER COLUMN store_id BIGINT NOT NULL;
ALTER TABLE purchases ADD CONSTRAINT IF NOT EXISTS fk_purchases_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);
ALTER TABLE purchases ADD CONSTRAINT IF NOT EXISTS fk_purchases_store FOREIGN KEY (store_id) REFERENCES stores (id);

ALTER TABLE purchases DROP CONSTRAINT IF EXISTS uq_purchases_grn_number;
ALTER TABLE purchases ADD CONSTRAINT uq_purchases_facility_grn_number UNIQUE (facility_id, grn_number);

-- ---------------------------------------------------------------
-- SALES_INVOICES - facility_id + store_id
-- ---------------------------------------------------------------
ALTER TABLE sales_invoices ADD COLUMN IF NOT EXISTS facility_id BIGINT;
ALTER TABLE sales_invoices ADD COLUMN IF NOT EXISTS store_id BIGINT;

UPDATE sales_invoices SET
    facility_id = (SELECT id FROM facilities WHERE name = 'Default Facility (migration bootstrap)'),
    store_id = (SELECT s.id FROM stores s JOIN facilities f ON f.id = s.facility_id
                WHERE f.name = 'Default Facility (migration bootstrap)' AND s.code = 'MAIN')
WHERE facility_id IS NULL;

ALTER TABLE sales_invoices ALTER COLUMN facility_id BIGINT NOT NULL;
ALTER TABLE sales_invoices ALTER COLUMN store_id BIGINT NOT NULL;
ALTER TABLE sales_invoices ADD CONSTRAINT IF NOT EXISTS fk_sales_invoices_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);
ALTER TABLE sales_invoices ADD CONSTRAINT IF NOT EXISTS fk_sales_invoices_store FOREIGN KEY (store_id) REFERENCES stores (id);

ALTER TABLE sales_invoices DROP CONSTRAINT IF EXISTS uq_sales_invoices_invoice_number;
ALTER TABLE sales_invoices ADD CONSTRAINT uq_sales_invoices_facility_invoice_number UNIQUE (facility_id, invoice_number);

-- ---------------------------------------------------------------
-- SALES_RETURNS - facility_id only (store is reached via the parent
-- sales_invoice; facility_id is denormalized per invariant 6, always
-- set equal to the parent invoice's facility_id)
-- ---------------------------------------------------------------
ALTER TABLE sales_returns ADD COLUMN IF NOT EXISTS facility_id BIGINT;

UPDATE sales_returns sr SET facility_id = (
    SELECT si.facility_id FROM sales_invoices si WHERE si.id = sr.sales_invoice_id
)
WHERE facility_id IS NULL;

ALTER TABLE sales_returns ALTER COLUMN facility_id BIGINT NOT NULL;
ALTER TABLE sales_returns ADD CONSTRAINT IF NOT EXISTS fk_sales_returns_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);

ALTER TABLE sales_returns DROP CONSTRAINT IF EXISTS uq_sales_returns_return_number;
ALTER TABLE sales_returns ADD CONSTRAINT uq_sales_returns_facility_return_number UNIQUE (facility_id, return_number);

-- ---------------------------------------------------------------
-- DOCUMENT_SEQUENCES - facility_id joins the composite primary key.
-- Number series are per-facility, per-financial-year, gapless
-- (Master Spec §6) - this table's whole PK now expresses that.
-- ---------------------------------------------------------------
ALTER TABLE document_sequences ADD COLUMN IF NOT EXISTS facility_id BIGINT;
UPDATE document_sequences SET facility_id = (SELECT id FROM facilities WHERE name = 'Default Facility (migration bootstrap)') WHERE facility_id IS NULL;
ALTER TABLE document_sequences ALTER COLUMN facility_id BIGINT NOT NULL;

ALTER TABLE document_sequences DROP PRIMARY KEY;
ALTER TABLE document_sequences ADD PRIMARY KEY (facility_id, doc_type, seq_year);
ALTER TABLE document_sequences ADD CONSTRAINT IF NOT EXISTS fk_document_sequences_facility FOREIGN KEY (facility_id) REFERENCES facilities (id);
