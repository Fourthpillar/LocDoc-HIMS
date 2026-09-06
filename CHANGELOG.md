# Changelog

All notable changes to this project are documented here.

## [1.0.0] - Initial release

### Added
- Spring Boot 3.3.4 application skeleton (Java 17), context path `/lockdoc`, port `7321`.
- **Gradle** build (`build.gradle`, `settings.gradle`) with `application.properties` configuration.
- H2 **file-based** database (`jdbc:h2:file:./data/lockdocdb`).
- Flyway-managed versioned SQL migrations (`src/main/resources/db/scripts`):
  - `V1` — creates all tables (users, roles, rights, user_roles, role_rights)
  - `V2` — seeds default rights, `SUPER_ADMIN` role (mapped to all rights), and default super-admin user `FP_USER`
- JWT authentication (`JwtUtil`, `JwtAuthenticationFilter`) with a configurable excluded-URL allow-list (`app.security.jwt.excluded-urls`).
- CORS configuration for UI apps on `http://localhost:5174`, `5175`, `5176`.
- `POST /auth/login` and example protected endpoint `GET /users/me`.
- Spring Boot Actuator health check at `/actuator/health` (public).
- Logback configuration with **size-based** rolling file logs (10MB per file, gzip archives, max 10 kept) plus a dedicated error log.
- Global exception handler for consistent JSON error responses.
- Documentation: `README.md`, `CLAUDE.md`, `API.md`, `.gitignore`.

## [1.1.0] - Build tooling & script consolidation

### Changed
- Migrated build from Maven (`pom.xml`) to **Gradle** (`build.gradle`, `settings.gradle`, `gradle.properties`).
- Migrated configuration from `application.yml` to **`application.properties`** (main and test).
- Consolidated the 8 versioned SQL scripts into **2 files**: `V1__create_tables.sql` (all DDL) and `V2__seed_default_data.sql` (all seed data).
