-- ============================================================
-- Version     : V24
-- Description : Step 5 - Bill, Payment, Discount, Cancellation (Master
--               Spec §6/§7.5). Bill is the single source of truth for
--               every OP billable event's money (registration and
--               consultation in this pass - procedure billing waits on
--               the Procedure master, build order step 8). BillLine's
--               hospital/doctor commission split is deferred alongside
--               procedure billing - both need the same commission
--               machinery, and neither blocks registration/consultation
--               billing from working correctly.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS bills (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id       BIGINT         NOT NULL,
    patient_id        BIGINT         NOT NULL,
    bill_no           VARCHAR(30)    NOT NULL,
    encounter_type    VARCHAR(20)    NOT NULL,
    encounter_id      BIGINT         NOT NULL,
    gross             DECIMAL(10,2)  NOT NULL,
    discount          DECIMAL(10,2)  NOT NULL DEFAULT 0,
    tax               DECIMAL(10,2)  NOT NULL DEFAULT 0,
    net               DECIMAL(10,2)  NOT NULL,
    paid              DECIMAL(10,2)  NOT NULL DEFAULT 0,
    due               DECIMAL(10,2)  NOT NULL,
    status             VARCHAR(20)   NOT NULL DEFAULT 'UNPAID',
    created_by_user_id BIGINT        NOT NULL,
    created_date       TIMESTAMP     NOT NULL,
    CONSTRAINT uq_bill_no UNIQUE (facility_id, bill_no),
    CONSTRAINT fk_bill_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_bill_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE INDEX IF NOT EXISTS idx_bill_encounter ON bills (encounter_type, encounter_id);
CREATE INDEX IF NOT EXISTS idx_bill_patient ON bills (patient_id);
CREATE INDEX IF NOT EXISTS idx_bill_facility_status ON bills (facility_id, status);

CREATE TABLE IF NOT EXISTS payments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id             BIGINT         NOT NULL,
    facility_id         BIGINT         NOT NULL,
    party_type          VARCHAR(20)    NOT NULL DEFAULT 'PATIENT',
    payment_type        VARCHAR(20)    NOT NULL,
    amount              DECIMAL(10,2)  NOT NULL,
    paid_at             TIMESTAMP      NOT NULL,
    received_by_user_id BIGINT         NOT NULL,
    CONSTRAINT fk_pay_bill FOREIGN KEY (bill_id) REFERENCES bills (id),
    CONSTRAINT fk_pay_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_pay_bill ON payments (bill_id);

CREATE TABLE IF NOT EXISTS discounts (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id              BIGINT         NOT NULL,
    facility_id          BIGINT         NOT NULL,
    discount_kind        VARCHAR(10)    NOT NULL,
    discount_value       DECIMAL(10,2)  NOT NULL,
    amount               DECIMAL(10,2)  NOT NULL,
    reason               VARCHAR(300)   NOT NULL,
    status               VARCHAR(20)    NOT NULL DEFAULT 'APPROVED',
    requested_by_user_id BIGINT         NOT NULL,
    approved_by_user_id  BIGINT,
    approved_at          TIMESTAMP,
    created_date         TIMESTAMP      NOT NULL,
    CONSTRAINT fk_disc_bill FOREIGN KEY (bill_id) REFERENCES bills (id),
    CONSTRAINT fk_disc_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_disc_facility_status ON discounts (facility_id, status);

CREATE TABLE IF NOT EXISTS cancellations (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id          BIGINT         NOT NULL,
    entity_type          VARCHAR(20)    NOT NULL,
    entity_id            BIGINT         NOT NULL,
    reason               VARCHAR(300)   NOT NULL,
    status               VARCHAR(20)    NOT NULL DEFAULT 'PENDING_APPROVAL',
    requested_by_user_id BIGINT         NOT NULL,
    approved_by_user_id  BIGINT,
    approved_at          TIMESTAMP,
    created_date         TIMESTAMP      NOT NULL,
    CONSTRAINT fk_cancel_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_cancel_facility_status ON cancellations (facility_id, status);
CREATE INDEX IF NOT EXISTS idx_cancel_entity ON cancellations (entity_type, entity_id);
