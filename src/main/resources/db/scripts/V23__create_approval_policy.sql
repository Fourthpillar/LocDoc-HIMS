-- ============================================================
-- Version     : V23
-- Description : Step 5 - ApprovalPolicy (Master Spec §6/§7.5/§15.1) -
--               self-service, Hospital/Clinic Admin sets and edits from
--               their own login, not a platform-level or seeded
--               constant. Only DISCOUNT is actually read by
--               BillingService in this pass - cancellation is
--               unconditionally Pending Approval per §4.1's "request
--               only" framing for Receptionist, no threshold involved -
--               but the table's approval_type is left free-text so a
--               facility can still configure other types (refund,
--               po_value, stock_adjustment - the latter two already
--               exist as unconditional flows elsewhere) without a schema
--               change later.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS approval_policies (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id       BIGINT         NOT NULL,
    approval_type     VARCHAR(30)    NOT NULL,
    threshold_value   DECIMAL(10,2)  NOT NULL,
    updated_date      TIMESTAMP      NOT NULL,
    CONSTRAINT uq_ap_facility_type UNIQUE (facility_id, approval_type),
    CONSTRAINT fk_ap_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);
