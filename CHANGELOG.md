# Changelog

All notable changes to this project are documented here.

## [Unreleased]

### Changed
- Split the single Gradle module into four: `common` (auth/JWT/security config, User/Role/Right, generic exceptions, `PageResponse`, document numbering), `pharmacy` (suppliers, medicines/batches, purchase orders, purchases/GRN, sales, sales returns, inventory, reports), `outpatient` (patients), and `app` (the runnable Spring Boot module - main class, `application.properties`, tests). `pharmacy` and `outpatient` never reach into each other's entities/repositories directly - the only cross-module link is `pharmacy`'s `SalesService` calling `outpatient`'s `PatientService` bean. Retires the old "pharmacy sub-package exception" - each former sub-package is now its own module. See `CLAUDE.md`.
- Corrected the ownership split for database scripts: the patients table is now in `outpatient/src/main/resources/db/scripts/outpatient`, and the shared auth/user bootstrap remains in `common/src/main/resources/db/scripts/common`. The app module no longer contains SQL migrations; only `app/src/main/resources/db-scripts-overview.html` documents the catalog.
- `SalesInvoice` no longer holds a JPA relationship to `Patient` (that entity now lives in a different module) - it stores a plain `patientId`, and `SalesInvoiceResponse.patientName` is resolved via a live call into outpatient's `PatientService` at read time.
- `DocumentNumberService`/`DocumentSequence`/`DocumentSequenceRepository` moved from the pharmacy sub-package into `common`, since they're a generic numbering utility used by both pharmacy documents and patient MRNs, not a pharmacy-specific concern.
- The two pharmacy-specific exceptions (`InsufficientStockException`, `InvalidDocumentStateException`) moved into the `pharmacy` module with their own `@RestControllerAdvice` (`PharmacyExceptionHandler`); `common`'s `GlobalExceptionHandler` keeps only the generic ones. Both share a new `ApiErrorResponseFactory` (`common`) for the error-body shape.

### Added
- `V7` migration: supplier master-data columns (`mobile2`, `landline`, `city`, `state`, `pincode`, `tin_no`, `website`, `supplier_type`, `drug_license_no`); `purchases` payables tracking (`amount_paid`, `balance_due`, `due_date`), mirroring the pattern already used on `sales_invoices`; `sales_invoices.round_off_amount` to make the cash-rounding adjustment explicit instead of silently folding it into `balance_due`.
- New pharmacy report endpoints under `/pharmacy/reports`: `purchase-orders` (date-range PO report), `purchase-dues` (GRN payables aging - GRNs with an outstanding balance, oldest due date first), `medicine-sales` (medicine-wise sales for a date range, net of returns), `stock-detail` (batch-level stock valuation: qty × purchase rate / MRP, with category/manufacturer/supplier).
- `PurchaseService.create()` now accepts an optional `amountPaid`/`dueDate` on the GRN and computes `balanceDue`; `SalesService.create()` now computes `roundOffAmount` (rounding the payable amount to the nearest rupee) and derives `balanceDue` from the rounded payable amount rather than the unrounded total, so partial/rounded payments are tracked accurately instead of always showing zero due.

### Fixed
- `V8` migration: corrects medicine category typos transcribed verbatim from the source legacy report into the `V5` seed data (`OINTEMENT`→`OINTMENT`, `SWEB`→`SWAB`, bandage/dressing items miscategorized under the needle-gauge category `GAUGE`→`GAUZE`), and fixes the same typo in a medicine name (`STERILE GAUGE SWAB`→`STERILE GAUZE SWAB`).

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
