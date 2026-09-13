-- ============================================================
-- Version     : V1001
-- Module      : common
-- Description : All DDL owned by the common module - identity
--               (users, roles, rights), facilities (tenants),
--               doctors and the doctor<->facility master data that
--               both outpatient and doctor modules read (mappings,
--               stated hours, consultation rates, free-review
--               policies), document number sequences and the
--               platform tables (audit log, support tickets).
--
--               Must not reference any outpatient (V2xxx) or doctor
--               (V3xxx) table - those modules build on top of this.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- FACILITIES - a tenant (hospital, clinic or lab). Every fact that
-- isn't platform-level reference data belongs to exactly one.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS facilities (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(200)  NOT NULL,
    type                VARCHAR(20)   NOT NULL,
    address             VARCHAR(300),
    geo_lat             DECIMAL(10,7),
    geo_lng             DECIMAL(10,7),
    licence_number      VARCHAR(100),
    verification_status VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    active              BOOLEAN       NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP     NOT NULL,
    updated_date        TIMESTAMP
);

-- ---------------------------------------------------------------
-- FACILITY_MODULES (Facility.activeModules @ElementCollection)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS facility_modules (
    facility_id  BIGINT       NOT NULL,
    module_code  VARCHAR(20)  NOT NULL,
    PRIMARY KEY (facility_id, module_code),
    CONSTRAINT fk_facility_modules_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

-- ---------------------------------------------------------------
-- USERS - facility_id is NULL for Super Admin (cross-facility) and
-- for doctors (who practise at facilities via mappings instead).
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    username              VARCHAR(50)     NOT NULL,
    password              VARCHAR(255)    NOT NULL,
    email                 VARCHAR(100),
    full_name             VARCHAR(150),
    facility_id           BIGINT,
    enabled               BOOLEAN         NOT NULL DEFAULT TRUE,
    account_non_locked    BOOLEAN         NOT NULL DEFAULT TRUE,
    must_change_password  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_date          TIMESTAMP       NOT NULL,
    updated_date          TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT fk_users_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

-- ---------------------------------------------------------------
-- ROLES
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code    VARCHAR(50)  NOT NULL,
    role_name    VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    CONSTRAINT uq_roles_role_code UNIQUE (role_code)
);

-- ---------------------------------------------------------------
-- RIGHTS (fine-grained permissions)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rights (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    right_code   VARCHAR(50)  NOT NULL,
    right_name   VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    CONSTRAINT uq_rights_right_code UNIQUE (right_code)
);

-- ---------------------------------------------------------------
-- USER_ROLES (many-to-many: users <-> roles)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_roles (
    user_id  BIGINT NOT NULL,
    role_id  BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------
-- ROLE_RIGHTS (many-to-many: roles <-> rights)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS role_rights (
    role_id   BIGINT NOT NULL,
    right_id  BIGINT NOT NULL,
    PRIMARY KEY (role_id, right_id),
    CONSTRAINT fk_role_rights_role  FOREIGN KEY (role_id)  REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_rights_right FOREIGN KEY (right_id) REFERENCES rights (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------
-- DOCTORS - one row per doctor, one-to-one with USERS. Not
-- facility-scoped: identity is verified once and used across every
-- facility the doctor maps to.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctors (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id               BIGINT        NOT NULL,
    full_name             VARCHAR(150)  NOT NULL,
    registration_number   VARCHAR(100)  NOT NULL,
    verification_status   VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    specialties           VARCHAR(300),
    qualifications        VARCHAR(300),
    created_date          TIMESTAMP     NOT NULL,
    updated_date          TIMESTAMP,
    CONSTRAINT uq_doctors_user_id UNIQUE (user_id),
    CONSTRAINT uq_doctors_registration_number UNIQUE (registration_number),
    CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ---------------------------------------------------------------
-- DOCTOR_FACILITY_MAPPINGS - openable from either side;
-- initiated_by records who owes the response:
--   REQUESTED + FACILITY -> awaiting the doctor
--   REQUESTED + DOCTOR   -> awaiting the facility
-- "No two open REQUESTED/ACCEPTED rows per doctor+facility" is
-- enforced in DoctorFacilityMappingService (no portable partial
-- unique index).
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctor_facility_mappings (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id           BIGINT        NOT NULL,
    facility_id         BIGINT        NOT NULL,
    relationship_type   VARCHAR(20)   NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'REQUESTED',
    initiated_by        VARCHAR(20)   NOT NULL DEFAULT 'FACILITY',
    requested_at        TIMESTAMP     NOT NULL,
    responded_at        TIMESTAMP,
    ended_at            TIMESTAMP,
    CONSTRAINT fk_dfm_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_dfm_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_dfm_doctor ON doctor_facility_mappings (doctor_id);
CREATE INDEX IF NOT EXISTS idx_dfm_facility ON doctor_facility_mappings (facility_id);
CREATE INDEX IF NOT EXISTS idx_doctor_facility_mapping_status_initiator ON doctor_facility_mappings (status, initiated_by);

-- ---------------------------------------------------------------
-- DOCTOR_FACILITY_MAPPING_HOURS - the hours a doctor says they will
-- be present. Input to the facility's doctor_schedules, not a
-- schedule itself.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctor_facility_mapping_hours (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    mapping_id   BIGINT      NOT NULL,
    weekday      VARCHAR(10) NOT NULL,
    start_time   TIME        NOT NULL,
    end_time     TIME        NOT NULL,
    CONSTRAINT fk_mapping_hours_mapping FOREIGN KEY (mapping_id) REFERENCES doctor_facility_mappings (id)
);

CREATE INDEX IF NOT EXISTS idx_mapping_hours_mapping ON doctor_facility_mapping_hours (mapping_id);

-- ---------------------------------------------------------------
-- CONSULTATION_RATES - doctor-proposed, Hospital/Clinic Admin-
-- approved consultation fee.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_rates (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id             BIGINT         NOT NULL,
    facility_id           BIGINT         NOT NULL,
    org_type              VARCHAR(20)    NOT NULL DEFAULT 'DIRECT',
    day_night_indicator   VARCHAR(10)    NOT NULL DEFAULT 'DAY',
    total_amount          DECIMAL(10,2)  NOT NULL,
    hospital_percent      DECIMAL(5,2),
    effective_from        DATE,
    effective_to          DATE,
    status                VARCHAR(20)    NOT NULL DEFAULT 'PENDING_APPROVAL',
    proposed_by_user_id   BIGINT         NOT NULL,
    approved_by_user_id   BIGINT,
    approved_at           TIMESTAMP,
    rejection_reason      VARCHAR(300),
    created_date          TIMESTAMP      NOT NULL,
    updated_date          TIMESTAMP,
    CONSTRAINT fk_cr_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_cr_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_cr_doctor_facility ON consultation_rates (doctor_id, facility_id);
CREATE INDEX IF NOT EXISTS idx_cr_facility_status ON consultation_rates (facility_id, status);

-- ---------------------------------------------------------------
-- FREE_REVIEW_POLICIES - per doctor per facility, max days and max
-- visits (both must hold). Eligibility is computed at billing time.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS free_review_policies (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id         BIGINT     NOT NULL,
    facility_id       BIGINT     NOT NULL,
    max_days          INT        NOT NULL,
    max_visits        INT        NOT NULL,
    created_date      TIMESTAMP  NOT NULL,
    updated_date      TIMESTAMP,
    CONSTRAINT fk_free_review_policy_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_free_review_policy_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_free_review_policy_doctor_facility ON free_review_policies (doctor_id, facility_id);

-- ---------------------------------------------------------------
-- DOCUMENT_SEQUENCES - pure counter table. Number series are per
-- facility, per document type, per year, gapless.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS document_sequences (
    facility_id  BIGINT      NOT NULL,
    doc_type     VARCHAR(30) NOT NULL,
    seq_year     INT         NOT NULL,
    prefix       VARCHAR(10) NOT NULL,
    last_number  INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (facility_id, doc_type, seq_year),
    CONSTRAINT fk_document_sequences_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

-- ---------------------------------------------------------------
-- AUDIT_LOG - wired into the highest-value actions (see
-- AuditLogService); entity_id is nullable/generic.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id     BIGINT,
    actor_user_id   BIGINT,
    action          VARCHAR(60)   NOT NULL,
    entity_type     VARCHAR(60)   NOT NULL,
    entity_id       BIGINT,
    before_data     VARCHAR(2000),
    after_data      VARCHAR(2000),
    occurred_at     TIMESTAMP     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_log_occurred ON audit_log (occurred_at);
CREATE INDEX IF NOT EXISTS idx_audit_log_facility ON audit_log (facility_id, occurred_at);

-- ---------------------------------------------------------------
-- SUPPORT_TICKETS - raised by a facility/doctor to Super Admin
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS support_tickets (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    facility_id         BIGINT        NOT NULL,
    raised_by_user_id   BIGINT        NOT NULL,
    category            VARCHAR(30),
    subject             VARCHAR(200)  NOT NULL,
    description         VARCHAR(2000) NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    resolution_notes    VARCHAR(1000),
    resolved_by_user_id BIGINT,
    resolved_at         TIMESTAMP,
    created_date        TIMESTAMP     NOT NULL,
    updated_date        TIMESTAMP,
    CONSTRAINT fk_ticket_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_ticket_status ON support_tickets (status, created_date);
