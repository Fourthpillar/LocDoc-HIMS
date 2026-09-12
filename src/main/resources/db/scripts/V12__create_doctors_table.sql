-- ============================================================
-- Version     : V12
-- Description : Step 3 of the LocDoc-HIMS build order - the Doctor
--               entity itself. One row per doctor, linked one-to-one
--               to a USERS row (Doctor authenticates through the same
--               username+password mechanism every other role uses,
--               Master Spec §17.4 - no separate credential system).
--
--               Deliberately NOT facility-scoped, unlike every table
--               V9 touched - a doctor's identity is centrally
--               verified once and used across every facility they
--               later map to (Master Spec §8.1); which facilities
--               they practise at is DoctorFacilityMapping, built in a
--               later step, not a column here.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS doctors (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id               BIGINT        NOT NULL,
    full_name             VARCHAR(150)  NOT NULL,
    registration_number   VARCHAR(100)  NOT NULL,
    verification_status   VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    specialties            VARCHAR(300),
    created_date          TIMESTAMP     NOT NULL,
    updated_date          TIMESTAMP,
    CONSTRAINT uq_doctors_user_id UNIQUE (user_id),
    CONSTRAINT uq_doctors_registration_number UNIQUE (registration_number),
    CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users (id)
);
