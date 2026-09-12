-- ============================================================
-- Version     : V45
-- Description : Free-review policy & linkage (Master Spec §7.2/§7.4,
--               entity list's own FreeReviewLink - "missing from every
--               earlier revision", part of OP Reports #12a's "free
--               reviews" report). §7.2: "free-review policy per doctor
--               (max days and max visits, both must hold)". Hospital/
--               Clinic Admin sets the policy per doctor at their own
--               facility - the same masters-ownership shape as every
--               other OP config screen (registration fee, approval
--               threshold), not a doctor-proposed workflow like
--               consultation fee.
--
--               free_review_links.original_op_visit_id anchors back to
--               the paid consultation the free visit is "against" -
--               §7.4's own example: "Free review — 2nd of 3, against
--               consultation OP/2026/01432". Eligibility (both days-
--               since-anchor and visits-used-against-anchor) is computed
--               at billing time in OpVisitService, not stored - the
--               policy can change after the fact without corrupting
--               already-decided free visits.
-- Author      : LockDoc App
-- ============================================================

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

INSERT INTO rights (right_code, right_name, description) VALUES
    ('FREE_REVIEW_POLICY_MANAGE', 'Manage Free-Review Policy', 'Per-doctor max-days/max-visits free-review policy at this facility (Master Spec §7.2)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('FREE_REVIEW_POLICY_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('FREE_REVIEW_POLICY_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
