-- ============================================================
-- Version     : V37
-- Description : Closing genuine gaps flagged by a post-step-9 backend
--               review - never assigned to any §16 build-order step,
--               so unbuilt despite being named in the data model (§6):
--               ReferralDoctor, PRO, Organization/TPA (Masters, screen
--               #34), and CounterSession (§7.5/§11.5, screens #12/#31 -
--               "identical behaviour to the Pharmacy counter... both
--               share one interaction model", one table not two).
--               Also adds doctors.qualifications for the Doctor profile
--               screen (#19) - §8.9's "qualifications, centrally-
--               verified registration/council number, NMC/ABDM
--               verification badge... specialties" never had a column
--               for the first of those.
--
--               counter_sessions.system_total is computed at close
--               time by summing this facility's CASH payments/pharmacy
--               sales within [opened_at, closed_at] - not accumulated
--               via a live counter_session_id FK on every Bill/
--               SalesInvoice row. That would mean retroactively
--               touching already-built, already-tested billing/sales
--               code across two modules for one reconciliation screen;
--               a time-windowed query gets the same reconciliation
--               outcome without the blast radius.
-- Author      : LockDoc App
-- ============================================================

ALTER TABLE doctors ADD COLUMN IF NOT EXISTS qualifications VARCHAR(300);

CREATE TABLE IF NOT EXISTS referral_doctors (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id       BIGINT       NOT NULL,
    name              VARCHAR(150) NOT NULL,
    contact           VARCHAR(100),
    registration_no   VARCHAR(50),
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date      TIMESTAMP    NOT NULL,
    updated_date      TIMESTAMP,
    CONSTRAINT fk_referral_doctor_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE TABLE IF NOT EXISTS pros (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id       BIGINT       NOT NULL,
    name              VARCHAR(150) NOT NULL,
    contact           VARCHAR(100),
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date      TIMESTAMP    NOT NULL,
    updated_date      TIMESTAMP,
    CONSTRAINT fk_pro_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE TABLE IF NOT EXISTS organizations (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id            BIGINT       NOT NULL,
    name                   VARCHAR(150) NOT NULL,
    org_type               VARCHAR(20)  NOT NULL,
    contract_terms         VARCHAR(500),
    credit_terms           VARCHAR(200),
    authorisation_ref_format VARCHAR(100),
    active                 BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date           TIMESTAMP    NOT NULL,
    updated_date           TIMESTAMP,
    CONSTRAINT fk_organization_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE TABLE IF NOT EXISTS counter_sessions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id         BIGINT        NOT NULL,
    counter_type        VARCHAR(10)   NOT NULL,
    status              VARCHAR(10)   NOT NULL DEFAULT 'OPEN',
    opened_by_user_id   BIGINT        NOT NULL,
    opened_at           TIMESTAMP     NOT NULL,
    closed_by_user_id   BIGINT,
    closed_at           TIMESTAMP,
    declared_cash       DECIMAL(10,2),
    system_total        DECIMAL(10,2),
    variance            DECIMAL(10,2),
    CONSTRAINT fk_counter_session_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_counter_session_facility_type_status ON counter_sessions (facility_id, counter_type, status);

INSERT INTO rights (right_code, right_name, description) VALUES
    ('FACILITY_MASTERS_MANAGE', 'Manage Facility Masters', 'Referral doctors, PRO, Organization/TPA masters, own facility only (Master Spec §7.2, screen #34)'),
    ('COUNTER_SESSION_MANAGE', 'Manage Counter Session', 'Open/close a counter session and reconcile declared cash against system total (Master Spec §7.5/§11.5, screens #12/#31)'),
    ('DOCTOR_PROFILE_MANAGE', 'Manage Own Doctor Profile', 'A doctor editing their own qualifications/specialties (Master Spec §8.9, screen #19) - registration number and verification status stay Super-Admin-only');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'RECEPTIONIST'
  AND rt.right_code IN ('COUNTER_SESSION_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'PHARMACIST'
  AND rt.right_code IN ('COUNTER_SESSION_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'DOCTOR'
  AND rt.right_code IN ('DOCTOR_PROFILE_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('FACILITY_MASTERS_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('FACILITY_MASTERS_MANAGE', 'COUNTER_SESSION_MANAGE', 'DOCTOR_PROFILE_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
