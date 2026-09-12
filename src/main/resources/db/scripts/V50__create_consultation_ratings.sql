-- ============================================================
-- Version     : V50
-- Description : Consultation Ratings (Master Spec §17.7 #18's own gap -
--               Doctor Reports had punctuality and consultation volume
--               but no patient-satisfaction signal at all). Captured by
--               Receptionist/front-desk once an OP visit reaches
--               COMPLETED - there's no patient self-service portal in
--               this build, so the desk collects the patient's feedback
--               on the way out, the same "front desk records what the
--               patient tells them" shape as vitals capture at arrival
--               (§7.4). One row per OP visit, edited in place rather than
--               re-inserted (correcting a mis-tap is not a second review).
-- Author      : LockDoc App
-- ============================================================

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
