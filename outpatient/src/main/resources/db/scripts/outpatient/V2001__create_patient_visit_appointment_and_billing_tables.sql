-- ============================================================
-- Version     : V2001
-- Module      : outpatient
-- Description : All DDL owned by the outpatient module - patients,
--               OP masters (areas, referral doctors, PROs,
--               organizations, registration fee config, doctor
--               schedules, procedure/service charges, packages),
--               registration, OP visits, appointments, billing
--               (bills, payments, discounts, cancellations,
--               procedure bills, package sales), counter sessions,
--               free-review links, consultation ratings, commission
--               basis and data protection (consents, erasure
--               requests).
--
--               Builds on common (V1xxx) only. Must not reference any
--               doctor-module (V3xxx) table.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- AREAS - flat Country/State/City/Area master for registration
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS areas (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id   BIGINT       NOT NULL,
    country       VARCHAR(100) NOT NULL,
    state         VARCHAR(100) NOT NULL,
    city          VARCHAR(100) NOT NULL,
    area_name     VARCHAR(100) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date  TIMESTAMP    NOT NULL,
    updated_date  TIMESTAMP,
    CONSTRAINT fk_area_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

-- ---------------------------------------------------------------
-- PATIENTS - one row per patient per facility, soft-delete via
-- active. MRN is unique per facility. area_id is optional
-- (reported as "Unspecified").
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patients (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id     BIGINT       NOT NULL,
    mrn             VARCHAR(30)  NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    phone           VARCHAR(20),
    gender          VARCHAR(10),
    date_of_birth   DATE,
    address         VARCHAR(300),
    allergies       VARCHAR(500),
    area_id         BIGINT,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date    TIMESTAMP    NOT NULL,
    updated_date    TIMESTAMP,
    CONSTRAINT uq_patients_facility_mrn UNIQUE (facility_id, mrn),
    CONSTRAINT fk_patients_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_patient_area FOREIGN KEY (area_id) REFERENCES areas (id)
);

-- ---------------------------------------------------------------
-- REGISTRATION_FEE_CONFIGS - one per facility, self-service.
-- validity_days NULL means registrations never expire.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS registration_fee_configs (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id            BIGINT         NOT NULL,
    first_fee              DECIMAL(10,2)  NOT NULL,
    re_registration_fee    DECIMAL(10,2)  NOT NULL,
    validity_days          INT,
    updated_date           TIMESTAMP      NOT NULL,
    CONSTRAINT uq_rfc_facility UNIQUE (facility_id),
    CONSTRAINT fk_rfc_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

-- ---------------------------------------------------------------
-- DOCTOR_SCHEDULES - a mapped doctor's recurring session capacity at
-- the facility; capacity/overbook enforced in AppointmentService.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctor_schedules (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id            BIGINT        NOT NULL,
    facility_id          BIGINT        NOT NULL,
    weekday              VARCHAR(10)   NOT NULL,
    session_name         VARCHAR(50)   NOT NULL,
    start_time           TIME          NOT NULL,
    end_time             TIME          NOT NULL,
    capacity             INT           NOT NULL,
    overbook_allowance   INT           NOT NULL DEFAULT 0,
    effective_from       DATE          NOT NULL,
    effective_to         DATE,
    active               BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date         TIMESTAMP     NOT NULL,
    CONSTRAINT fk_dsch_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_dsch_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_dsch_doctor_facility ON doctor_schedules (doctor_id, facility_id);

-- ---------------------------------------------------------------
-- Facility masters - REFERRAL_DOCTORS, PROS, ORGANIZATIONS (org/TPA)
-- ---------------------------------------------------------------
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
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id              BIGINT       NOT NULL,
    name                     VARCHAR(150) NOT NULL,
    org_type                 VARCHAR(20)  NOT NULL,
    contract_terms           VARCHAR(500),
    credit_terms             VARCHAR(200),
    authorisation_ref_format VARCHAR(100),
    active                   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date             TIMESTAMP    NOT NULL,
    updated_date             TIMESTAMP,
    CONSTRAINT fk_organization_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

-- ---------------------------------------------------------------
-- PATIENT_REGISTRATIONS - money lives once, on BILLS
-- (encounter_type='REGISTRATION'). expiry_date NULL = never expires.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patient_registrations (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id            BIGINT        NOT NULL,
    facility_id           BIGINT        NOT NULL,
    registration_no       VARCHAR(30)   NOT NULL,
    is_re_registration    BOOLEAN       NOT NULL DEFAULT FALSE,
    expiry_date           DATE,
    registered_at         TIMESTAMP     NOT NULL,
    registered_by_user_id BIGINT        NOT NULL,
    CONSTRAINT uq_preg_no UNIQUE (facility_id, registration_no),
    CONSTRAINT fk_preg_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_preg_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_preg_patient ON patient_registrations (patient_id, expiry_date);

-- ---------------------------------------------------------------
-- OP_VISITS - with optional referral doctor / PRO attribution
-- (commission report) and cancellation reason.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS op_visits (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    op_no                VARCHAR(30)   NOT NULL,
    facility_id          BIGINT        NOT NULL,
    patient_id           BIGINT        NOT NULL,
    appointment_id       BIGINT,
    doctor_id            BIGINT        NOT NULL,
    org_type             VARCHAR(20)   NOT NULL DEFAULT 'DIRECT',
    visit_type           VARCHAR(30)   NOT NULL DEFAULT 'NEW',
    weight_kg            DECIMAL(6,2),
    height_cm            DECIMAL(6,2),
    temperature_f        DECIMAL(5,2),
    bp                   VARCHAR(20),
    attendant_name       VARCHAR(150),
    attendant_mobile     VARCHAR(20),
    attendant_relation   VARCHAR(50),
    referral_doctor_id   BIGINT,
    pro_id               BIGINT,
    status               VARCHAR(20)   NOT NULL DEFAULT 'ARRIVED',
    cancel_reason        VARCHAR(300),
    mlc_flag             BOOLEAN       NOT NULL DEFAULT FALSE,
    mlc_police_station   VARCHAR(150),
    mlc_number           VARCHAR(50),
    arrived_ts           TIMESTAMP     NOT NULL,
    consult_start_ts     TIMESTAMP,
    consult_end_ts       TIMESTAMP,
    CONSTRAINT uq_opvisit_no UNIQUE (facility_id, op_no),
    CONSTRAINT fk_opvisit_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_opvisit_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_opvisit_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_op_visit_referral_doctor FOREIGN KEY (referral_doctor_id) REFERENCES referral_doctors (id),
    CONSTRAINT fk_op_visit_pro FOREIGN KEY (pro_id) REFERENCES pros (id)
);

CREATE INDEX IF NOT EXISTS idx_opvisit_facility_date ON op_visits (facility_id, arrived_ts);
CREATE INDEX IF NOT EXISTS idx_opvisit_doctor ON op_visits (doctor_id, arrived_ts);

-- ---------------------------------------------------------------
-- APPOINTMENTS / APPOINTMENT_WAITLIST. purpose: CONSULTATION raises
-- the doctor's fee on arrival; PROCEDURE bills from the charge master.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS appointments (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id           BIGINT        NOT NULL,
    doctor_id             BIGINT        NOT NULL,
    doctor_schedule_id    BIGINT        NOT NULL,
    patient_id            BIGINT        NOT NULL,
    appointment_ts        TIMESTAMP     NOT NULL,
    purpose               VARCHAR(20)   NOT NULL DEFAULT 'CONSULTATION',
    status                VARCHAR(20)   NOT NULL DEFAULT 'BOOKED',
    channel               VARCHAR(20)   NOT NULL DEFAULT 'FRONT_DESK',
    cancel_reason         VARCHAR(300),
    created_date          TIMESTAMP     NOT NULL,
    CONSTRAINT fk_appt_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_appt_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_appt_schedule FOREIGN KEY (doctor_schedule_id) REFERENCES doctor_schedules (id),
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE INDEX IF NOT EXISTS idx_appt_schedule_date ON appointments (doctor_schedule_id, appointment_ts);
CREATE INDEX IF NOT EXISTS idx_appt_facility_date ON appointments (facility_id, appointment_ts);
CREATE INDEX IF NOT EXISTS idx_appt_patient ON appointments (patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_purpose ON appointments (facility_id, purpose);

CREATE TABLE IF NOT EXISTS appointment_waitlist (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id             BIGINT        NOT NULL,
    doctor_schedule_id      BIGINT        NOT NULL,
    session_date            DATE          NOT NULL,
    patient_id              BIGINT        NOT NULL,
    joined_at               TIMESTAMP     NOT NULL,
    promoted_at             TIMESTAMP,
    promoted_appointment_id BIGINT,
    CONSTRAINT fk_wait_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_wait_schedule FOREIGN KEY (doctor_schedule_id) REFERENCES doctor_schedules (id),
    CONSTRAINT fk_wait_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE INDEX IF NOT EXISTS idx_wait_schedule_date ON appointment_waitlist (doctor_schedule_id, session_date, joined_at);

-- ---------------------------------------------------------------
-- APPROVAL_POLICIES - per-facility thresholds, self-service
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_policies (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id       BIGINT         NOT NULL,
    approval_type     VARCHAR(30)    NOT NULL,
    threshold_value   DECIMAL(10,2)  NOT NULL,
    updated_date      TIMESTAMP      NOT NULL,
    CONSTRAINT uq_ap_facility_type UNIQUE (facility_id, approval_type),
    CONSTRAINT fk_ap_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

-- ---------------------------------------------------------------
-- BILLS - single source of truth for every OP billable event's
-- money. patient_id is nullable for unregistered walk-in procedure
-- bills. refund_status: NONE / PENDING / REFUNDED (orthogonal to
-- status, which stays CANCELLED on a cancelled bill).
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bills (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id        BIGINT         NOT NULL,
    patient_id         BIGINT,
    bill_no            VARCHAR(30)    NOT NULL,
    encounter_type     VARCHAR(20)    NOT NULL,
    encounter_id       BIGINT         NOT NULL,
    gross              DECIMAL(10,2)  NOT NULL,
    discount           DECIMAL(10,2)  NOT NULL DEFAULT 0,
    tax                DECIMAL(10,2)  NOT NULL DEFAULT 0,
    net                DECIMAL(10,2)  NOT NULL,
    paid               DECIMAL(10,2)  NOT NULL DEFAULT 0,
    due                DECIMAL(10,2)  NOT NULL,
    status             VARCHAR(20)    NOT NULL DEFAULT 'UNPAID',
    refund_status      VARCHAR(20)    NOT NULL DEFAULT 'NONE',
    created_by_user_id BIGINT         NOT NULL,
    created_date       TIMESTAMP      NOT NULL,
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

-- ---------------------------------------------------------------
-- COUNTER_SESSIONS - OP till open/close; system_total is computed at
-- close time from CASH payments within [opened_at, closed_at].
-- ---------------------------------------------------------------
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

-- ---------------------------------------------------------------
-- BILLABLE_ITEMS - procedure and service charge master (one table,
-- item_type SERVICE|PROCEDURE), org-type-dimensioned rates.
-- ---------------------------------------------------------------
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

-- ---------------------------------------------------------------
-- PROCEDURE_BILLS - bill_id is nullable because the row is saved once
-- before its Bill exists (same transaction); patient_id is nullable
-- for unregistered walk-ins, captured inline in walk_in_*.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS procedure_bills (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id         BIGINT       NOT NULL,
    patient_id          BIGINT,
    walk_in_name        VARCHAR(150),
    walk_in_age         INT,
    walk_in_gender      VARCHAR(10),
    walk_in_mobile      VARCHAR(20),
    billable_item_id    BIGINT       NOT NULL,
    doctor_id           BIGINT,
    org_type            VARCHAR(20)  NOT NULL,
    performed_by_name   VARCHAR(150),
    bill_id             BIGINT,
    created_by_user_id  BIGINT       NOT NULL,
    created_date        TIMESTAMP    NOT NULL,
    CONSTRAINT fk_procedure_bill_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_procedure_bill_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_procedure_bill_item FOREIGN KEY (billable_item_id) REFERENCES billable_items (id),
    CONSTRAINT fk_procedure_bill_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_procedure_bill_bill FOREIGN KEY (bill_id) REFERENCES bills (id)
);

CREATE INDEX IF NOT EXISTS idx_procedure_bill_facility ON procedure_bills (facility_id);

-- ---------------------------------------------------------------
-- PACKAGES / PACKAGE_ITEMS (master), PACKAGE_SALES (billing event),
-- PACKAGE_UTILIZATION (drawdown ledger per included item per sale)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS packages (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id   BIGINT        NOT NULL,
    name          VARCHAR(150)  NOT NULL,
    code          VARCHAR(30),
    price         DECIMAL(10,2) NOT NULL,
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date  TIMESTAMP     NOT NULL,
    updated_date  TIMESTAMP,
    CONSTRAINT fk_package_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE TABLE IF NOT EXISTS package_items (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id        BIGINT  NOT NULL,
    billable_item_id  BIGINT  NOT NULL,
    included_qty      INT     NOT NULL,
    CONSTRAINT fk_package_item_package FOREIGN KEY (package_id) REFERENCES packages (id),
    CONSTRAINT fk_package_item_billable_item FOREIGN KEY (billable_item_id) REFERENCES billable_items (id)
);

CREATE INDEX IF NOT EXISTS idx_package_item_package ON package_items (package_id);

CREATE TABLE IF NOT EXISTS package_sales (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id         BIGINT     NOT NULL,
    patient_id          BIGINT     NOT NULL,
    package_id          BIGINT     NOT NULL,
    bill_id             BIGINT,
    created_by_user_id  BIGINT     NOT NULL,
    created_date        TIMESTAMP  NOT NULL,
    CONSTRAINT fk_package_sale_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_package_sale_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_package_sale_package FOREIGN KEY (package_id) REFERENCES packages (id),
    CONSTRAINT fk_package_sale_bill FOREIGN KEY (bill_id) REFERENCES bills (id)
);

CREATE INDEX IF NOT EXISTS idx_package_sale_patient ON package_sales (patient_id);
CREATE INDEX IF NOT EXISTS idx_package_sale_facility ON package_sales (facility_id);

CREATE TABLE IF NOT EXISTS package_utilization (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_sale_id     BIGINT  NOT NULL,
    billable_item_id    BIGINT  NOT NULL,
    included_qty        INT     NOT NULL,
    used_qty            INT     NOT NULL DEFAULT 0,
    remaining_qty       INT     NOT NULL,
    CONSTRAINT fk_package_util_sale FOREIGN KEY (package_sale_id) REFERENCES package_sales (id),
    CONSTRAINT fk_package_util_item FOREIGN KEY (billable_item_id) REFERENCES billable_items (id)
);

CREATE INDEX IF NOT EXISTS idx_package_util_sale ON package_utilization (package_sale_id);

-- ---------------------------------------------------------------
-- FREE_REVIEW_LINKS - a free visit anchored to the paid consultation
-- it is "against" (original_op_visit_id)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS free_review_links (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id             BIGINT     NOT NULL,
    doctor_id              BIGINT     NOT NULL,
    facility_id            BIGINT     NOT NULL,
    op_visit_id            BIGINT     NOT NULL,
    original_op_visit_id   BIGINT     NOT NULL,
    created_date           TIMESTAMP  NOT NULL,
    CONSTRAINT fk_free_review_link_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_free_review_link_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_free_review_link_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_free_review_link_visit FOREIGN KEY (op_visit_id) REFERENCES op_visits (id),
    CONSTRAINT fk_free_review_link_original_visit FOREIGN KEY (original_op_visit_id) REFERENCES op_visits (id)
);

CREATE INDEX IF NOT EXISTS idx_free_review_link_original_visit ON free_review_links (original_op_visit_id);
CREATE INDEX IF NOT EXISTS idx_free_review_link_facility_date ON free_review_links (facility_id, created_date);

-- ---------------------------------------------------------------
-- CONSULTATION_RATINGS - one per OP visit, captured at the desk
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_ratings (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    op_visit_id         BIGINT     NOT NULL,
    doctor_id           BIGINT     NOT NULL,
    facility_id         BIGINT     NOT NULL,
    rating              INT        NOT NULL,
    comment             VARCHAR(500),
    rated_by_user_id    BIGINT     NOT NULL,
    created_date        TIMESTAMP  NOT NULL,
    updated_date        TIMESTAMP,
    CONSTRAINT fk_consultation_rating_visit FOREIGN KEY (op_visit_id) REFERENCES op_visits (id),
    CONSTRAINT fk_consultation_rating_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_consultation_rating_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT chk_consultation_rating_range CHECK (rating BETWEEN 1 AND 5)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_consultation_rating_visit ON consultation_ratings (op_visit_id);
CREATE INDEX IF NOT EXISTS idx_consultation_rating_doctor_date ON consultation_ratings (doctor_id, created_date);

-- ---------------------------------------------------------------
-- COMMISSION_BASIS - self-service commission terms per referrer;
-- party_name is free text matched against referral doctor/PRO names.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS commission_basis (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id     BIGINT        NOT NULL,
    party_type      VARCHAR(40)   NOT NULL,
    party_name      VARCHAR(150)  NOT NULL,
    basis           VARCHAR(10)   NOT NULL,
    value_amount    DECIMAL(10,2) NOT NULL,
    applies_to      VARCHAR(200),
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date    TIMESTAMP     NOT NULL,
    updated_date    TIMESTAMP,
    CONSTRAINT fk_commission_basis_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_commission_basis_facility ON commission_basis (facility_id, active);

-- ---------------------------------------------------------------
-- CONSENTS / ERASURE_REQUESTS (DPDP). patient_id is nullable with
-- ON DELETE SET NULL so a "Fulfilled (deleted)" erasure can hard-delete
-- the patient; the name/MRN snapshots keep the record legible.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consents (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id           BIGINT        NOT NULL,
    patient_id            BIGINT,
    patient_name_snapshot VARCHAR(150)  NOT NULL,
    patient_mrn_snapshot  VARCHAR(30)   NOT NULL,
    consent_type          VARCHAR(20)   NOT NULL,
    version               INT           NOT NULL DEFAULT 1,
    text_shown            CLOB          NOT NULL,
    captured_by_user_id   BIGINT        NOT NULL,
    captured_at           TIMESTAMP     NOT NULL,
    method                VARCHAR(20)   NOT NULL DEFAULT 'IN_APP_CHECKBOX',
    guardian_name         VARCHAR(150),
    guardian_relation     VARCHAR(50),
    CONSTRAINT fk_consent_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_consent_patient FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_consent_patient ON consents (patient_id, consent_type);

CREATE TABLE IF NOT EXISTS erasure_requests (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id           BIGINT        NOT NULL,
    patient_id            BIGINT,
    patient_name_snapshot VARCHAR(150)  NOT NULL,
    patient_mrn_snapshot  VARCHAR(30)   NOT NULL,
    requested_at          TIMESTAMP     NOT NULL,
    requested_via         VARCHAR(20)   NOT NULL,
    requested_by          VARCHAR(20)   NOT NULL,
    status                VARCHAR(30)   NOT NULL DEFAULT 'RECEIVED',
    reviewed_by_user_id   BIGINT,
    resolved_at           TIMESTAMP,
    resolution_notes      VARCHAR(1000),
    created_date          TIMESTAMP     NOT NULL,
    CONSTRAINT fk_erasure_facility FOREIGN KEY (facility_id) REFERENCES facilities (id),
    CONSTRAINT fk_erasure_patient FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_erasure_facility_status ON erasure_requests (facility_id, status);
