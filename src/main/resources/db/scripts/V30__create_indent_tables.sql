-- ============================================================
-- Version     : V30
-- Description : Step 7 - Indent/IndentLine (Master Spec §6/§11.3), the
--               step before a Purchase Order this document's earlier
--               revisions underspecified. Raised by a sub-store against
--               the facility's main store (or another store), states
--               DRAFT -> SUBMITTED -> APPROVED -> PARTIALLY_ISSUED ->
--               ISSUED / REJECTED / CLOSED.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS indents (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id         BIGINT        NOT NULL,
    indent_no           VARCHAR(30)   NOT NULL,
    from_store_id       BIGINT        NOT NULL,
    to_store_id         BIGINT        NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    remarks             VARCHAR(500),
    requested_by_user_id BIGINT       NOT NULL,
    approved_by_user_id BIGINT,
    approved_date       TIMESTAMP,
    created_date        TIMESTAMP     NOT NULL,
    updated_date        TIMESTAMP,
    CONSTRAINT uq_indent_no UNIQUE (facility_id, indent_no),
    CONSTRAINT fk_indent_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_indent_from_store FOREIGN KEY (from_store_id) REFERENCES stores (id),
    CONSTRAINT fk_indent_to_store FOREIGN KEY (to_store_id) REFERENCES stores (id)
);

CREATE TABLE IF NOT EXISTS indent_lines (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    indent_id       BIGINT        NOT NULL,
    medicine_id     BIGINT        NOT NULL,
    requested_qty   INT           NOT NULL,
    approved_qty    INT,
    issued_qty      INT           NOT NULL DEFAULT 0,
    CONSTRAINT fk_iline_indent FOREIGN KEY (indent_id) REFERENCES indents (id),
    CONSTRAINT fk_iline_medicine FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

CREATE INDEX IF NOT EXISTS idx_indent_facility_status ON indents (facility_id, status);
CREATE INDEX IF NOT EXISTS idx_indent_to_store ON indents (to_store_id, status);
