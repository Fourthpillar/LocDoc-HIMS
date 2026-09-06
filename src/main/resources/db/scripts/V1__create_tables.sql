-- ============================================================
-- Version     : V1
-- Description : Create all tables - USERS, ROLES, RIGHTS and
--               their many-to-many join tables
-- Author      : LockDoc App
-- ============================================================

-- ---------------------------------------------------------------
-- USERS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    username            VARCHAR(50)     NOT NULL,
    password            VARCHAR(255)    NOT NULL,
    email               VARCHAR(100),
    full_name           VARCHAR(150),
    enabled             BOOLEAN         NOT NULL DEFAULT TRUE,
    account_non_locked  BOOLEAN         NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP       NOT NULL,
    updated_date        TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username)
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
