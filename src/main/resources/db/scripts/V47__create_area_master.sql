-- ============================================================
-- Version     : V47
-- Description : Area master, Country→State→City→Area (Master Spec §7.2/
--               §6 Masters list) - the last of OP Reports #12a's eight
--               named reports ("area-wise consultations") still unbuilt,
--               per OpReportsService's own javadoc. One flat table, not
--               four normalized ones - the spec names the hierarchy as
--               a browsing/entry convenience on the registration form,
--               not as independently-manageable Country/State/City
--               master data with their own CRUD screens (India's
--               administrative divisions aren't this facility's data to
--               maintain); a single area_name string alongside its
--               country/state/city labels gets the same "area-wise"
--               grouping the report needs without inventing three
--               master screens nothing else in the spec calls for.
--
--               patients.area_id is nullable - existing patients and any
--               registration that skips it report under "Unspecified",
--               not a forced backfill.
-- Author      : LockDoc App
-- ============================================================

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

ALTER TABLE patients ADD COLUMN IF NOT EXISTS area_id BIGINT;
ALTER TABLE patients ADD CONSTRAINT IF NOT EXISTS fk_patient_area FOREIGN KEY (area_id) REFERENCES areas (id);

INSERT INTO rights (right_code, right_name, description) VALUES
    ('AREA_MASTERS_MANAGE', 'Manage Area Master', 'Country/State/City/Area master for patient registration, own facility only (Master Spec §7.2)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('AREA_MASTERS_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('AREA_MASTERS_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
