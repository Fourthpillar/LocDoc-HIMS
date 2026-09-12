-- ============================================================
-- Version     : V17
-- Description : Step 4 of the LocDoc-HIMS build order. No screen yet
--               creates a Hospital/Clinic Admin account (that's Super
--               Admin's job, build order step 8, §4.1) - without one,
--               nothing can live-verify this step's facility-side
--               endpoints (invite a doctor, view facility status).
--               Seeds exactly one, scoped to V9's bootstrap facility
--               (id 1), the same way V2 seeded FP_USER for the same
--               reason. Same password as FP_USER for convenience
--               (reuses its bcrypt hash) - change immediately in any
--               real environment, same caveat V2 already states.
--
--   Default credentials:
--     username : BOOTSTRAP_ADMIN
--     password : FP@Admin#123
-- Author      : LockDoc App
-- ============================================================

INSERT INTO users (username, password, email, full_name, facility_id, enabled, account_non_locked, created_date) VALUES
    ('BOOTSTRAP_ADMIN', '$2b$10$wziGg47emGStkmCTSreXIebA9AA3t38z2uNYbWVqdz/6ZVsBktGNq', 'admin@bootstrap-facility.local', 'Bootstrap Facility Admin', 1, TRUE, TRUE, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'BOOTSTRAP_ADMIN' AND r.role_code = 'HOSPITAL_ADMIN';
