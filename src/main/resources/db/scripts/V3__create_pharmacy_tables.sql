-- ============================================================
-- Version     : V3
-- Description : Create pharmacy module tables - master data
--               (patients, suppliers, medicines, medicine
--               batches), document number sequences, purchase
--               orders, purchases (GRN), sales invoices, sales
--               returns, and the stock ledger audit trail.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- PATIENTS (master data, soft-delete via active flag)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patients (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    mrn             VARCHAR(30)  NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    phone           VARCHAR(20),
    gender          VARCHAR(10),
    date_of_birth   DATE,
    address         VARCHAR(300),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date    TIMESTAMP    NOT NULL,
    updated_date    TIMESTAMP,
    CONSTRAINT uq_patients_mrn UNIQUE (mrn)
);

-- ---------------------------------------------------------------
-- SUPPLIERS (master data, soft-delete via active flag)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    contact_person  VARCHAR(150),
    phone           VARCHAR(20),
    email           VARCHAR(150),
    address         VARCHAR(300),
    gstin           VARCHAR(20),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date    TIMESTAMP    NOT NULL,
    updated_date    TIMESTAMP
);

-- ---------------------------------------------------------------
-- MEDICINES (master data, soft-delete via active flag)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicines (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    code              VARCHAR(30)   NOT NULL,
    name              VARCHAR(200)  NOT NULL,
    generic_name      VARCHAR(200),
    manufacturer      VARCHAR(150),
    category          VARCHAR(50),
    uom               VARCHAR(20)   NOT NULL,
    hsn_code          VARCHAR(20),
    tax_percent       DECIMAL(5,2)  NOT NULL DEFAULT 0,
    reorder_level     INT           NOT NULL DEFAULT 0,
    is_schedule_drug  BOOLEAN       NOT NULL DEFAULT FALSE,
    active            BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date      TIMESTAMP     NOT NULL,
    updated_date      TIMESTAMP,
    CONSTRAINT uq_medicines_code UNIQUE (code)
);

-- ---------------------------------------------------------------
-- DOCUMENT_SEQUENCES (pure counter table - no id/created_date)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS document_sequences (
    doc_type     VARCHAR(30) NOT NULL,
    seq_year     INT         NOT NULL,
    prefix       VARCHAR(10) NOT NULL,
    last_number  INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (doc_type, seq_year)
);

-- ---------------------------------------------------------------
-- PURCHASE_ORDERS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchase_orders (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    po_number                VARCHAR(30)  NOT NULL,
    supplier_id              BIGINT       NOT NULL,
    order_date               DATE         NOT NULL,
    expected_delivery_date   DATE,
    status                   VARCHAR(20)  NOT NULL,
    remarks                  VARCHAR(500),
    created_by               BIGINT       NOT NULL,
    approved_by              BIGINT,
    approved_date            TIMESTAMP,
    created_date             TIMESTAMP    NOT NULL,
    updated_date             TIMESTAMP,
    CONSTRAINT uq_purchase_orders_po_number UNIQUE (po_number),
    CONSTRAINT fk_purchase_orders_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
    CONSTRAINT fk_purchase_orders_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_purchase_orders_approved_by FOREIGN KEY (approved_by) REFERENCES users (id)
);

-- ---------------------------------------------------------------
-- PURCHASE_ORDER_ITEMS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id   BIGINT        NOT NULL,
    medicine_id         BIGINT        NOT NULL,
    ordered_qty         INT           NOT NULL,
    received_qty        INT           NOT NULL DEFAULT 0,
    rate                DECIMAL(10,2) NOT NULL,
    tax_percent         DECIMAL(5,2)  NOT NULL DEFAULT 0,
    amount              DECIMAL(12,2) NOT NULL,
    created_date        TIMESTAMP     NOT NULL,
    updated_date        TIMESTAMP,
    CONSTRAINT fk_purchase_order_items_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_order_items_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

-- ---------------------------------------------------------------
-- PURCHASES (GRN header)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchases (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    grn_number                VARCHAR(30)   NOT NULL,
    purchase_order_id         BIGINT,
    supplier_id               BIGINT        NOT NULL,
    purchase_date             DATE          NOT NULL,
    supplier_invoice_number   VARCHAR(50),
    supplier_invoice_date     DATE,
    status                    VARCHAR(20)   NOT NULL,
    remarks                   VARCHAR(500),
    total_amount              DECIMAL(12,2) NOT NULL DEFAULT 0,
    created_by                BIGINT        NOT NULL,
    created_date              TIMESTAMP     NOT NULL,
    updated_date               TIMESTAMP,
    CONSTRAINT uq_purchases_grn_number UNIQUE (grn_number),
    CONSTRAINT fk_purchases_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id),
    CONSTRAINT fk_purchases_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
    CONSTRAINT fk_purchases_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);

-- ---------------------------------------------------------------
-- PURCHASE_ITEMS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchase_items (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_id               BIGINT        NOT NULL,
    purchase_order_item_id    BIGINT,
    medicine_id                BIGINT        NOT NULL,
    batch_no                  VARCHAR(50)   NOT NULL,
    expiry_date                DATE          NOT NULL,
    received_qty               INT           NOT NULL,
    free_qty                   INT           NOT NULL DEFAULT 0,
    rate                       DECIMAL(10,2) NOT NULL,
    tax_percent                 DECIMAL(5,2)  NOT NULL DEFAULT 0,
    mrp                         DECIMAL(10,2) NOT NULL,
    sale_rate                   DECIMAL(10,2) NOT NULL,
    amount                      DECIMAL(12,2) NOT NULL,
    created_date                TIMESTAMP     NOT NULL,
    updated_date                TIMESTAMP,
    CONSTRAINT fk_purchase_items_purchase FOREIGN KEY (purchase_id) REFERENCES purchases (id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_items_purchase_order_item FOREIGN KEY (purchase_order_item_id) REFERENCES purchase_order_items (id),
    CONSTRAINT fk_purchase_items_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

-- ---------------------------------------------------------------
-- MEDICINE_BATCHES
--
-- NOTE: source_purchase_item_id intentionally has NO foreign key
-- constraint (it logically references purchase_items.id) - it is
-- created for traceability only, and adding the constraint would
-- require reordering table creation. Left as a plain nullable
-- column to keep script ordering simple.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicine_batches (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id               BIGINT        NOT NULL,
    batch_no                  VARCHAR(50)   NOT NULL,
    expiry_date                DATE          NOT NULL,
    mrp                         DECIMAL(10,2) NOT NULL,
    purchase_rate                DECIMAL(10,2) NOT NULL,
    sale_rate                     DECIMAL(10,2) NOT NULL,
    quantity_on_hand               INT           NOT NULL DEFAULT 0,
    supplier_id                     BIGINT,
    source_purchase_item_id          BIGINT,
    created_date                      TIMESTAMP     NOT NULL,
    updated_date                       TIMESTAMP,
    CONSTRAINT uq_medicine_batches_medicine_batch UNIQUE (medicine_id, batch_no),
    CONSTRAINT fk_medicine_batches_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id),
    CONSTRAINT fk_medicine_batches_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id)
);

-- ---------------------------------------------------------------
-- SALES_INVOICES
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales_invoices (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number            VARCHAR(30)   NOT NULL,
    patient_id                BIGINT,
    walk_in_customer_name     VARCHAR(150),
    walk_in_customer_phone    VARCHAR(20),
    sale_date                 DATE          NOT NULL,
    payment_mode              VARCHAR(20)   NOT NULL,
    subtotal                  DECIMAL(12,2) NOT NULL,
    tax_amount                DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_amount           DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount               DECIMAL(12,2) NOT NULL,
    amount_paid                 DECIMAL(12,2) NOT NULL DEFAULT 0,
    balance_due                  DECIMAL(12,2) NOT NULL DEFAULT 0,
    status                        VARCHAR(20)   NOT NULL,
    created_by                     BIGINT        NOT NULL,
    created_date                    TIMESTAMP     NOT NULL,
    updated_date                     TIMESTAMP,
    CONSTRAINT uq_sales_invoices_invoice_number UNIQUE (invoice_number),
    CONSTRAINT fk_sales_invoices_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_sales_invoices_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);

-- ---------------------------------------------------------------
-- SALES_INVOICE_ITEMS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales_invoice_items (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    sales_invoice_id          BIGINT        NOT NULL,
    medicine_id               BIGINT        NOT NULL,
    medicine_batch_id         BIGINT        NOT NULL,
    qty                       INT           NOT NULL,
    rate                      DECIMAL(10,2) NOT NULL,
    tax_percent               DECIMAL(5,2)  NOT NULL DEFAULT 0,
    discount_amount           DECIMAL(10,2) NOT NULL DEFAULT 0,
    amount                    DECIMAL(12,2) NOT NULL,
    created_date               TIMESTAMP     NOT NULL,
    updated_date                TIMESTAMP,
    CONSTRAINT fk_sales_invoice_items_invoice FOREIGN KEY (sales_invoice_id) REFERENCES sales_invoices (id) ON DELETE CASCADE,
    CONSTRAINT fk_sales_invoice_items_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id),
    CONSTRAINT fk_sales_invoice_items_batch FOREIGN KEY (medicine_batch_id) REFERENCES medicine_batches (id)
);

-- ---------------------------------------------------------------
-- SALES_RETURNS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales_returns (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_number             VARCHAR(30)   NOT NULL,
    sales_invoice_id          BIGINT        NOT NULL,
    return_date               DATE          NOT NULL,
    reason                    VARCHAR(300),
    total_amount              DECIMAL(12,2) NOT NULL DEFAULT 0,
    status                    VARCHAR(20)   NOT NULL,
    created_by                BIGINT        NOT NULL,
    created_date               TIMESTAMP     NOT NULL,
    updated_date                TIMESTAMP,
    CONSTRAINT uq_sales_returns_return_number UNIQUE (return_number),
    CONSTRAINT fk_sales_returns_invoice FOREIGN KEY (sales_invoice_id) REFERENCES sales_invoices (id),
    CONSTRAINT fk_sales_returns_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);

-- ---------------------------------------------------------------
-- SALES_RETURN_ITEMS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales_return_items (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    sales_return_id           BIGINT        NOT NULL,
    sales_invoice_item_id     BIGINT        NOT NULL,
    medicine_id               BIGINT        NOT NULL,
    medicine_batch_id         BIGINT        NOT NULL,
    qty                       INT           NOT NULL,
    rate                      DECIMAL(10,2) NOT NULL,
    amount                    DECIMAL(12,2) NOT NULL,
    created_date                TIMESTAMP     NOT NULL,
    updated_date                 TIMESTAMP,
    CONSTRAINT fk_sales_return_items_return FOREIGN KEY (sales_return_id) REFERENCES sales_returns (id) ON DELETE CASCADE,
    CONSTRAINT fk_sales_return_items_invoice_item FOREIGN KEY (sales_invoice_item_id) REFERENCES sales_invoice_items (id),
    CONSTRAINT fk_sales_return_items_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id),
    CONSTRAINT fk_sales_return_items_batch FOREIGN KEY (medicine_batch_id) REFERENCES medicine_batches (id)
);

-- ---------------------------------------------------------------
-- STOCK_LEDGER_ENTRIES (audit trail of every stock movement)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS stock_ledger_entries (
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id               BIGINT        NOT NULL,
    medicine_batch_id         BIGINT        NOT NULL,
    txn_type                  VARCHAR(20)   NOT NULL,
    qty_in                    INT           NOT NULL DEFAULT 0,
    qty_out                   INT           NOT NULL DEFAULT 0,
    balance_qty                INT           NOT NULL,
    reference_type              VARCHAR(30)   NOT NULL,
    reference_id                 BIGINT        NOT NULL,
    reference_number               VARCHAR(30)   NOT NULL,
    txn_date                        TIMESTAMP     NOT NULL,
    created_date                     TIMESTAMP     NOT NULL,
    updated_date                      TIMESTAMP,
    CONSTRAINT fk_stock_ledger_entries_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id),
    CONSTRAINT fk_stock_ledger_entries_batch FOREIGN KEY (medicine_batch_id) REFERENCES medicine_batches (id)
);
