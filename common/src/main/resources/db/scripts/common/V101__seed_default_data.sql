-- ============================================================
-- Version     : V101
-- Description : Seed default data - rights, SUPER_ADMIN role
--               (mapped to every right), and the default
--               super-admin user FP_USER (mapped to SUPER_ADMIN)
--
--   Default credentials:
--     username : FP_USER
--     password : FP@Admin#123
--
--   The password below is BCrypt-hashed. IMPORTANT: change this
--   password immediately after the first login in any real
--   environment.
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- Default rights (permissions)
-- ---------------------------------------------------------------
INSERT INTO rights (right_code, right_name, description) VALUES
    ('USER_CREATE',  'Create User',   'Ability to create new users'),
    ('USER_READ',    'View User',     'Ability to view user details'),
    ('USER_UPDATE',  'Update User',   'Ability to update user details'),
    ('USER_DELETE',  'Delete User',   'Ability to delete users'),
    ('ROLE_MANAGE',  'Manage Roles',  'Ability to create/update/delete roles'),
    ('RIGHT_MANAGE', 'Manage Rights', 'Ability to create/update/delete rights'),
    ('SYSTEM_ADMIN', 'System Admin',  'Full administrative access to the system');

-- ---------------------------------------------------------------
-- Default SUPER_ADMIN role, mapped to every right above
-- ---------------------------------------------------------------
INSERT INTO roles (role_code, role_name, description) VALUES
    ('SUPER_ADMIN', 'Super Administrator', 'Default super administrator role with full access');

INSERT INTO role_rights (role_id, right_id)
SELECT r.id, rt.id
FROM roles r, rights rt
WHERE r.role_code = 'SUPER_ADMIN';

-- ---------------------------------------------------------------
-- Default super-admin user FP_USER, mapped to SUPER_ADMIN role
-- ---------------------------------------------------------------
INSERT INTO users (username, password, email, full_name, enabled, account_non_locked, created_date) VALUES
    ('FP_USER', '$2b$10$wziGg47emGStkmCTSreXIebA9AA3t38z2uNYbWVqdz/6ZVsBktGNq', 'fp_user@lockdoc.local', 'FP Super Admin', TRUE, TRUE, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'FP_USER' AND r.role_code = 'SUPER_ADMIN';
