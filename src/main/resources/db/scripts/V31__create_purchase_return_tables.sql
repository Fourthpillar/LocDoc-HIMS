-- ============================================================
-- Version     : V31
-- Description : Step 7 - PurchaseReturn/PurchaseReturnItem (Master Spec
--               §6/§11.3) - a debit note against an already-accepted GRN
--               batch (damaged, expired, wrong supply, rate dispute) -
--               distinct from cancelling a purchase (which only undoes a
--               same-day mistake, PurchaseService.cancel).
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS purchase_returns (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id     BIGINT         NOT NULL,
    store_id        BIGINT         NOT NULL,
    return_no       VARCHAR(30)    NOT NULL,
    purchase_id     BIGINT         NOT NULL,
    supplier_id     BIGINT         NOT NULL,
    reason          VARCHAR(300)   NOT NULL,
    status          VARCHAR(20)    NOT NULL DEFAULT 'POSTED',
    total_amount    DECIMAL(12,2)  NOT NULL,
    created_by      BIGINT         NOT NULL,
    created_date    TIMESTAMP      NOT NULL,
    CONSTRAINT uq_preturn_no UNIQUE (facility_id, return_no),
    CONSTRAINT fk_preturn_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_preturn_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT fk_preturn_purchase FOREIGN KEY (purchase_id) REFERENCES purchases (id),
    CONSTRAINT fk_preturn_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id)
);

CREATE TABLE IF NOT EXISTS purchase_return_items (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_return_id BIGINT        NOT NULL,
    purchase_item_id  BIGINT         NOT NULL,
    medicine_id       BIGINT         NOT NULL,
    batch_no          VARCHAR(50)    NOT NULL,
    returned_qty      INT            NOT NULL,
    rate              DECIMAL(10,2)  NOT NULL,
    amount            DECIMAL(12,2)  NOT NULL,
    CONSTRAINT fk_pritem_return FOREIGN KEY (purchase_return_id) REFERENCES purchase_returns (id),
    CONSTRAINT fk_pritem_purchase_item FOREIGN KEY (purchase_item_id) REFERENCES purchase_items (id),
    CONSTRAINT fk_pritem_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

CREATE INDEX IF NOT EXISTS idx_preturn_supplier ON purchase_returns (supplier_id, created_date);
CREATE INDEX IF NOT EXISTS idx_pritem_purchase_item ON purchase_return_items (purchase_item_id);
