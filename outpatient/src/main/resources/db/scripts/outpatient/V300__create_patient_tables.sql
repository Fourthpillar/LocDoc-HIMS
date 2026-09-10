-- ============================================================
-- Version     : V300
-- Description : Create the outpatient module tables. Patients are
--               owned by the outpatient module rather than the
--               pharmacy module.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- PATIENTS (master data, soft-delete via active flag)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patients (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    mrn             VARCHAR(30)  NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    phone           VARCHAR(20),
    gender          VARCHAR(10),
    date_of_birth   DATE,
    address         VARCHAR(300),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date    TIMESTAMP    NOT NULL,
    updated_date    TIMESTAMP,
    CONSTRAINT uq_patients_mrn UNIQUE (mrn)
);
