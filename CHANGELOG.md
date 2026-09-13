# Changelog

All notable changes to this project are documented here.

## [Unreleased]

### Changed
- Split the single Gradle project into a multi-project build: `common`, `outpatient` and `doctor` modules (each with its own Java code and Flyway scripts), assembled by a new `app` module that owns `LockDocApplication`, `application.properties`, logging config and the integration tests. Packages moved from `com.lockdoc.app.*` to `com.lockdoc.common.*`, `com.lockdoc.outpatient.*` and `com.lockdoc.doctor.*`. The jar is now built at `app/build/libs/lockdoc-app.jar`; `./gradlew bootRun` still runs from the repository root, so `./data` and `./logs` are unchanged.
- `ConsultationRate`, `FreeReviewPolicy`, `DoctorFacilityMapping(+Hour)` now live in `common` (outpatient billing/scheduling reads them), and `FreeReviewLink` in `outpatient`, so that `outpatient` does not depend on `doctor`. `DoctorScheduleController`, `DoctorConsultationHoursController` and `FacilityScheduleExceptionController` moved to `doctor` (URLs unchanged).
- **Flyway history reset.** The 54 incremental migrations (`V1`-`V54`) are replaced by one DDL and one DML script per module, named for what they do: `V1001__create_user_facility_doctor_and_platform_tables` / `V1002__insert_roles_rights_and_bootstrap_users` (common), `V2001__create_patient_visit_appointment_and_billing_tables` / `V2002__insert_receptionist_role_and_outpatient_rights` (outpatient), `V3001__create_doctor_status_consultation_and_schedule_tables` / `V3002__insert_doctor_rights` (doctor). The resulting schema and seed data are identical to the old `V54` end state minus everything pharmacy (verified by diffing `INFORMATION_SCHEMA` columns, constraints and indexes, plus rights/roles/mappings). `spring.flyway.out-of-order` is now `true` so later per-module versions (e.g. `V1003`) can apply after `V3002`. **Existing H2 files (`data/lockdocdb*.mv.db`) must be deleted and recreated** — their `flyway_schema_history` no longer matches.
- The bootstrap facility is now `Default Facility (bootstrap)`, type `HOSPITAL`, with modules `OP` and `DOCTOR` (was type/module `PHARMACY`).
- `PATCH /op/patients/{id}/deactivate` added (previously only reachable via the removed `/pharmacy/patients`).

### Removed
- The pharmacy module: all `/pharmacy/**` controllers, services, repositories, entities and DTOs, plus `Store`, `InsufficientStockException` and `SafetyCheckException`.
- All pharmacy tables (medicines, batches, suppliers, purchase orders, GRNs, sales, returns, stock ledger, indents, transfers, stock counts, statutory register, stores, user store scope), the 18 `PHARMACY_*` rights, the `PHARMACIST` role, and `prescription_lines.matched_pharmacy_medicine_id`.
- `GET /op/reports/pharmacy-conversion` and `GET /doctor/consultations/{opVisitId}/medicine-lookup`.
- `PrescriptionLineRequest/Response.matchedPharmacyMedicineId`, `PrescriptionLineResponse.stockStatus`/`availableQty`, and `pharmacyMatchedLines` on the medicines-prescribed report.
- Pharmacy constants and behaviour: `Facility.TYPE_PHARMACY`, `CounterSession.TYPE_PHARMACY` (counter sessions accept `OP` only), `CommissionBasis.PARTY_PRESCRIBING_DOCTOR`, Pharmacist accounts in Manage Users, and the pharmacy-sales check on erasure requests.
- Pharmacy reference PDFs under `temp/`; `matched_pharmacy_medicine_id` from the `dev-data/doctor-reports` generator and seed.

### Fixed

- Unknown URLs and wrong HTTP methods no longer return `500`. `GlobalExceptionHandler` had only a catch-all `@ExceptionHandler(Exception.class)`, which swallowed Spring's `NoResourceFoundException` (thrown by the static-resource handler that Boot 3.2+ maps at `/**` for any request matching no `@RequestMapping`) and `HttpRequestMethodNotSupportedException` — so every typo'd or stale path answered `500 "An unexpected error occurred"`, and every wrong verb did too. Explicit handlers now return `404` and `405` respectively in the same `{error, message, timestamp, status}` body shape as the other handlers, with the `Allow` header on a `405`; they log at DEBUG instead of an ERROR stack trace, so URL scans no longer flood the log. A `NoHandlerFoundException` handler is included for the case where static-resource mappings are ever disabled. The catch-all is unchanged and still covers genuine failures. Covered by `UnknownEndpointErrorTest`.

## [1.2.0] - Pharmacy module

### Added
- Full pharmacy module: master data (patients, suppliers, medicines, medicine batches), purchase orders, purchases (GRN), sales invoices, sales returns, inventory, and reports, plus a document-numbering sequence generator (`document_sequences`).
- Flyway migrations:
  - `V3` — creates all 14 pharmacy tables (patients, suppliers, medicines, medicine_batches, document_sequences, purchase_orders/items, purchases/items, sales_invoices/items, sales_returns/items, stock_ledger_entries)
  - `V4` — seeds 10 new `PHARMACY_*` rights, maps them to `SUPER_ADMIN`, and adds a new `PHARMACIST` role mapped to all of them
- New `pharmacy` sub-package under each layer (`controller/service/repository/entity/dto`) — a deliberate, documented exception to the codebase's flat layout given the module's size (14 entities). See `CLAUDE.md`.
- Shared pagination DTO `dto/PageResponse.java` used by all pharmacy list endpoints.
- Method-level authorization: `@EnableMethodSecurity` added to `SecurityConfig`; every pharmacy controller method is annotated with `@PreAuthorize("hasAuthority('...')")`. This is additive - the existing `app.security.jwt.excluded-urls` allow-list mechanism is unchanged.
- Four new reusable exception types in `exception/` (`ResourceNotFoundException` → 404, `DuplicateResourceException` → 409, `InvalidDocumentStateException` → 409, `InsufficientStockException` → 409), all handled in the existing `GlobalExceptionHandler`.
- New endpoints under `/pharmacy/**`: `medicines`, `suppliers`, `patients`, `inventory`, `purchase-orders`, `purchases`, `sales`, `sales-returns`, `reports` (see `API.md`).
- Stock ledger (`stock_ledger_entries`) as the audit trail for every purchase/sale/return posting and cancellation, feeding the inventory and report endpoints.

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
