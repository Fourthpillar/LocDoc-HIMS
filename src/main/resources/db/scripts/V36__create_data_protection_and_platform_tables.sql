-- ============================================================
-- Version     : V36
-- Description : Step 9 (Master Spec §16 step 9, §18, §10) - the four
--               domains that were data-modeled (§6) but never actually
--               built: Consent, ErasureRequest, AuditLog, SupportTicket.
--               Backs screens #38a/#38b (Hospital/Clinic Admin) and
--               #41/#42/#42a (Super Admin) - #39/#40 reuse controllers
--               that already existed (FacilityController/DoctorController),
--               so they need no new schema here.
--
--               consents.text_shown is CLOB, not VARCHAR - §18.2's
--               versioned consent text is a full paragraph per capture,
--               stored so a later wording revision never retroactively
--               changes what an earlier patient is recorded as having
--               agreed to.
--
--               audit_log: NOT wired into every service in the app -
--               that would be a cross-cutting change disproportionate
--               to one step. Wired into the highest-value existing
--               actions instead (facility verify/reject/suspend/
--               reinstate, doctor verify/reject, facility user create/
--               deactivate/reactivate, erasure-request resolution) -
--               see AuditLogService's own javadoc for the honest scope
--               statement. entity_id is nullable (some actions, like a
--               facility itself being the subject, still populate it;
--               kept generic rather than modeling every entity type).
-- Author      : LockDoc App
-- ============================================================

-- patient_id is nullable with ON DELETE SET NULL, not NOT NULL: a
-- "Fulfilled (deleted)" erasure resolution hard-deletes the very Patient
-- row this request (and any of the patient's Consent rows) references -
-- an ordinary NOT NULL FK would make that delete impossible. The name/
-- MRN snapshot columns keep the record legible after the patient row is
-- gone; while the patient still exists, the API prefers the live row.
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

INSERT INTO rights (right_code, right_name, description) VALUES
    ('PATIENT_CONSENT_MANAGE', 'Manage Patient Consent', 'Capture and search treatment/data-processing consent records (Master Spec §18.2, screen #38a)'),
    ('DATA_PROTECTION_ERASURE_MANAGE', 'Manage Erasure Requests', 'Intake and resolve DPDP erasure/grievance requests (Master Spec §18.4/§18.6, screen #38b)'),
    ('AUDIT_LOG_VIEW', 'View Cross-facility Audit Log', 'Super Admin platform-wide audit trail (Master Spec §10, screen #42)'),
    ('SUPPORT_TICKET_CREATE', 'Raise Support Ticket', 'Raise a support/dispute ticket to Super Admin (Master Spec §10)'),
    ('SUPPORT_TICKET_MANAGE', 'Triage Support Tickets', 'Super Admin triage/resolve of facility-raised support tickets (Master Spec §10, screen #42a)');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'RECEPTIONIST'
  AND rt.right_code IN ('PATIENT_CONSENT_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'HOSPITAL_ADMIN'
  AND rt.right_code IN ('PATIENT_CONSENT_MANAGE', 'DATA_PROTECTION_ERASURE_MANAGE', 'SUPPORT_TICKET_CREATE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'DOCTOR'
  AND rt.right_code IN ('SUPPORT_TICKET_CREATE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN'
  AND rt.right_code IN ('PATIENT_CONSENT_MANAGE', 'DATA_PROTECTION_ERASURE_MANAGE', 'AUDIT_LOG_VIEW',
                         'SUPPORT_TICKET_CREATE', 'SUPPORT_TICKET_MANAGE')
  AND rt.id NOT IN (SELECT right_id FROM role_rights WHERE role_id = r.id);
