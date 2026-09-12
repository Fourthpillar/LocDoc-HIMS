-- ============================================================
-- Version     : V39
-- Description : Procedure/Service masters and Procedure billing (Master
--               Spec §7.2/§7.5, screen #9 "Procedure/Service Charges" +
--               the OP Billing screen's third billable event) - the
--               direct, named prerequisite Bill.java's own javadoc
--               flagged as deferred ("procedure billing is deferred...
--               pending the Procedure master") and that Package sale &
--               utilization (§17.7 #11) itself depends on: a package's
--               included items are drawn down as the patient uses them,
--               which only means something once a procedure/service can
--               be billed and tracked as a distinct line at all.
--
--               Service and Procedure are one table (billable_items,
--               item_type SERVICE|PROCEDURE) rather than two identical
--               ones - same precedent as CounterSession (V37) unifying
--               OP/Pharmacy tills: §7.2 already names them together
--               ("procedure/service charge masters on the same org-type
--               dimensions") and Bill has one PROCEDURE encounter type
--               for both, not two.
--
--               Scope deliberately cut here, not an oversight:
--               procedure_bills.patient_id is NOT NULL - registered-
--               patient billing only. §7.5's unregistered-walk-in path
--               (inline name/age/gender/mobile capture, no patient_id
--               at all) needs Bill.patient_id to go nullable the same
--               way erasure_requests.patient_id did (V36), which is a
--               much bigger blast radius here since Bill is the single
--               billing engine for every OP encounter type, not one
--               narrow data-protection flow - left for its own pass.
--               performed_by_name is free text, not an Employee_id FK -
--               the Employee/Designation masters this would properly
--               reference (§6) are themselves still unbuilt.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS billable_items (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id       BIGINT        NOT NULL,
    item_type         VARCHAR(10)   NOT NULL,
    name              VARCHAR(150)  NOT NULL,
    code              VARCHAR(30),
    rate_direct       DECIMAL(10,2) NOT NULL,
    rate_organization DECIMAL(10,2),
    rate_tpa          DECIMAL(10,2),
    active            BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date      TIMESTAMP     NOT NULL,
    updated_date      TIMESTAMP,
    CONSTRAINT fk_billable_item_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_billable_item_facility_type ON billable_items (facility_id, item_type);

CREATE TABLE IF NOT EXISTS procedure_bills (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id         BIGINT       NOT NULL,
    patient_id          BIGINT       NOT NULL,
    billable_item_id    BIGINT       NOT NULL,
    doctor_id           BIGINT,
    org_type            VARCHAR(20)  NOT NULL,
    performed_by_name   VARCHAR(150),
    bill_id             BIGINT       NOT NULL,
    created_by_user_id  BIGINT       NOT NULL,
    created_date        TIMESTAMP    NOT NULL,
    CONSTRAINT fk_procedure_bill_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_procedure_bill_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_procedure_bill_item FOREIGN KEY (billable_item_id) REFERENCES billable_items (id),
    CONSTRAINT fk_procedure_bill_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_procedure_bill_bill FOREIGN KEY (bill_id) REFERENCES bills (id)
);

CREATE INDEX IF NOT EXISTS idx_procedure_bill_facility ON procedure_bills (facility_id);

INSERT INTO rights (right_code, right_name, description) VALUES
    ('PROCEDURE_MASTERS_MANAGE', 'Manage Procedure/Service Charges', 'Procedure and service charge masters, own facility only, org-type-dimensioned rates (Master Spec §7.2)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('PROCEDURE_MASTERS_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('PROCEDURE_MASTERS_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
