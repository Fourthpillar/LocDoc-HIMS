-- ============================================================
-- Version     : V14
-- Description : Step 4 of the LocDoc-HIMS build order (Master Spec
--               §8.1/§6) - the relationship between a Doctor and a
--               Facility. Only Hospital/Clinic Admin invites (§4.1's
--               permission matrix); the doctor accepts/declines/ends.
-- Author      : LockDoc App
-- ============================================================

CREATE TABLE IF NOT EXISTS doctor_facility_mappings (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id           BIGINT        NOT NULL,
    facility_id         BIGINT        NOT NULL,
    relationship_type   VARCHAR(20)   NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'REQUESTED',
    requested_at        TIMESTAMP     NOT NULL,
    responded_at        TIMESTAMP,
    ended_at            TIMESTAMP,
    CONSTRAINT fk_dfm_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_dfm_facility FOREIGN KEY (facility_id) REFERENCES facilities (id)
);

CREATE INDEX IF NOT EXISTS idx_dfm_doctor ON doctor_facility_mappings (doctor_id);
CREATE INDEX IF NOT EXISTS idx_dfm_facility ON doctor_facility_mappings (facility_id);

-- A doctor should never have two REQUESTED/ACCEPTED rows open against the
-- same facility at once (DECLINED/ENDED ones don't block a fresh invite) -
-- enforced in DoctorFacilityMappingService at the application layer since
-- H2/standard SQL has no clean partial-unique-index syntax portable across
-- the eventual production database choice (§15.1's cloud vendor is still
-- open).
