-- ============================================================
-- Version     : V33
-- Description : Step 7 - StockCount/StockCountLine (Master Spec §6/
--               §11.4) - blind count sheets by rack/bin, variance
--               report, variance posts to stock only on Hospital/Clinic
--               Admin approval (§5 principle 4's approval discipline).
--               "Blind" means the counting UI doesn't surface
--               system_qty while counting - it's still captured at
--               creation so a variance can be computed at submit time.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS stock_counts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id     BIGINT        NOT NULL,
    store_id        BIGINT        NOT NULL,
    count_no        VARCHAR(30)   NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    counted_by_user_id  BIGINT    NOT NULL,
    approved_by_user_id BIGINT,
    count_date      DATE          NOT NULL,
    approved_date   TIMESTAMP,
    created_date    TIMESTAMP     NOT NULL,
    CONSTRAINT uq_scount_no UNIQUE (facility_id, count_no),
    CONSTRAINT fk_scount_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_scount_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE IF NOT EXISTS stock_count_lines (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_count_id  BIGINT        NOT NULL,
    medicine_id     BIGINT        NOT NULL,
    medicine_batch_id BIGINT      NOT NULL,
    system_qty      INT           NOT NULL,
    counted_qty     INT,
    CONSTRAINT fk_scline_count FOREIGN KEY (stock_count_id) REFERENCES stock_counts (id),
    CONSTRAINT fk_scline_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id),
    CONSTRAINT fk_scline_batch FOREIGN KEY (medicine_batch_id) REFERENCES medicine_batches (id)
);

CREATE INDEX IF NOT EXISTS idx_scount_facility_status ON stock_counts (facility_id, status);
