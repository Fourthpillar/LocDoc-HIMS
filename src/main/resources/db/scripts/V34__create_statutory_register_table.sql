-- ============================================================
-- Version     : V34
-- Description : Step 7 - StatutoryRegisterEntry (Master Spec §6/§15.2) -
--               the entity behind "build at launch" for the drug
--               registers. Auto-created by SalesService (step 6 already
--               captures prescriber_name/prescriber_registration_number
--               on sales_invoice_items for exactly this) whenever a
--               posted sale line's medicine is a scheduled drug.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS statutory_register_entries (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id            BIGINT        NOT NULL,
    register_type          VARCHAR(10)   NOT NULL,
    sales_invoice_item_id  BIGINT        NOT NULL,
    medicine_name          VARCHAR(200)  NOT NULL,
    patient_name           VARCHAR(150),
    qty                    INT           NOT NULL,
    prescriber_name        VARCHAR(150)  NOT NULL,
    prescriber_registration_number VARCHAR(100) NOT NULL,
    sale_date              DATE          NOT NULL,
    created_date           TIMESTAMP     NOT NULL,
    CONSTRAINT uq_sre_sales_item UNIQUE (sales_invoice_item_id),
    CONSTRAINT fk_sre_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_sre_facility_type_date ON statutory_register_entries (facility_id, register_type, sale_date);
