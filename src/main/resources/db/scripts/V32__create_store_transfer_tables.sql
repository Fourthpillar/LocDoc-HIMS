-- ============================================================
-- Version     : V32
-- Description : Step 7 - StoreTransfer/StoreTransferItem (Master Spec
--               §6/§11.4) - inter-store movement, issue -> in-transit ->
--               receipt, with discrepancy captured on receipt.
--               Meaningless with one store, required the moment Indent
--               (V30) makes multi-store real.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS store_transfers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id     BIGINT        NOT NULL,
    transfer_no     VARCHAR(30)   NOT NULL,
    from_store_id   BIGINT        NOT NULL,
    to_store_id     BIGINT        NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'IN_TRANSIT',
    issued_by_user_id   BIGINT    NOT NULL,
    received_by_user_id BIGINT,
    issued_date     TIMESTAMP     NOT NULL,
    received_date   TIMESTAMP,
    discrepancy_notes VARCHAR(500),
    CONSTRAINT uq_transfer_no UNIQUE (facility_id, transfer_no),
    CONSTRAINT fk_transfer_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_transfer_from_store FOREIGN KEY (from_store_id) REFERENCES stores (id),
    CONSTRAINT fk_transfer_to_store FOREIGN KEY (to_store_id) REFERENCES stores (id)
);

CREATE TABLE IF NOT EXISTS store_transfer_items (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_transfer_id BIGINT        NOT NULL,
    medicine_id       BIGINT        NOT NULL,
    batch_no          VARCHAR(50)   NOT NULL,
    expiry_date       DATE          NOT NULL,
    issued_qty        INT           NOT NULL,
    received_qty      INT,
    CONSTRAINT fk_titem_transfer FOREIGN KEY (store_transfer_id) REFERENCES store_transfers (id),
    CONSTRAINT fk_titem_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

CREATE INDEX IF NOT EXISTS idx_transfer_to_store_status ON store_transfers (to_store_id, status);
