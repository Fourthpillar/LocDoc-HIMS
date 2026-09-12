# CLAUDE.md

Guidance for Claude (or any AI assistant) working in this repository.

## Project summary

LockDoc App is a Spring Boot 3 (Java 17) REST API:

- **Build tool**: Gradle (Groovy DSL), **multi-module**: `common`, `pharmacy`, `outpatient`, `doctor`, `app` (see "Module structure" below) — not Maven. `gradle build` / `gradle test` at the root build/test all five modules; run the app with `gradle :app:bootRun` (only `app` has the Boot plugin applied, so plain `gradle bootRun` no longer works).
- **Config format**: `application.properties` — not YAML. If you add config, use dotted-key properties syntax (e.g. `app.cors.allowed-origins[0]=...` for list entries), and keep it consistent with the existing file rather than introducing a `.yml` alongside it.
- **Database**: H2, file-based (`jdbc:h2:file:./data/lockdocdb`) — never switch this to in-memory (`jdbc:h2:mem:`) without being asked; the whole point is durability across restarts.
- **Schema management**: Flyway only. Do not set `spring.jpa.hibernate.ddl-auto` to `update` or `create`. It must stay `validate` — the schema is owned by the versioned SQL scripts.
- **Security**: Stateless JWT auth via a custom `OncePerRequestFilter` (`JwtAuthenticationFilter`), not Spring Session. Excluded URLs are data-driven from `app.security.jwt.excluded-urls` in `application.properties`.
- **Context path**: `/lockdoc`. **Port**: `7321`. Do not change these unless explicitly instructed — other systems (UI apps on 5174/5175/5176) are coded against this base URL.
- **CORS**: origins are read from `app.cors.allowed-origins`. If a new UI port needs to be supported, add it there — do not hardcode `*`.

## Database change process (IMPORTANT)

All schema and seed-data changes MUST go through a new versioned Flyway script in the owning module folder. The app module is reserved for documentation only; do not keep SQL files there.

```
common/src/main/resources/db/scripts/common/
outpatient/src/main/resources/db/scripts/outpatient/
pharmacy/src/main/resources/db/scripts/pharmacy/
doctor/src/main/resources/db/scripts/doctor/
```

Rules:
1. **Never edit a script that has already been committed/applied.** Flyway checksums applied migrations; editing one breaks `validate-on-migrate` for anyone who already ran it.
2. **Versioning is per-module blocks of 100**, not one global counter — see the table below. Create a new file following the pattern `V{next_number_in_your_module's_block}__{snake_case_description}.sql`, e.g. the next pharmacy script after `V201` is `V202__...sql`. This keeps modules from colliding on version numbers as they're developed independently, since Flyway's combined `spring.flyway.locations` list still requires every version to be globally unique. Leave gaps inside a block for future scripts — don't compact/renumber to close them.
3. Keep module-owned DDL in the owning module: `common` for shared auth/user schema (including `facilities`, which is not any one feature module's property), `outpatient` for patients, `pharmacy` for pharmacy tables, `doctor` for doctors and everything keyed on them. Do not leave seed or data-fix scripts in the app module.
4. Update the corresponding JPA entity in `entity/` to match, and bump any relevant repository/service code.
5. Document the new version in `CHANGELOG.md` under "Unreleased" or a new version heading.
6. Never write DDL directly against the running H2 file — always go through a script so the change is reproducible.
7. Keep user/role/right bootstrapping, including the default `FP_USER`, in `common` so that shared auth security remains together.

Version block assignment (a new module claims the next unused block; add a row here when one is added):

| Block | Module |
|---|---|
| V100-V199 | `common` |
| V200-V299 | `pharmacy` |
| V300-V399 | `outpatient` |
| V400-V499 | `doctor` |
| V500-V599 | *(next module)* |

Current version ledger (do not renumber existing files):

| Version | Location | Purpose |
|---|---|---|
| V100 | `common/src/main/resources/db/scripts/common` | users, roles, rights + user_roles/role_rights join tables (shared core DDL) |
| V101 | `common/src/main/resources/db/scripts/common` | seeds baseline rights, `SUPER_ADMIN` role, and default super-admin user `FP_USER` |
| V200 | `pharmacy/src/main/resources/db/scripts/pharmacy` | pharmacy module DDL - suppliers, medicines, medicine_batches, document_sequences, purchase_orders(+items), purchases/GRN(+items), sales_invoices(+items), sales_returns(+items), stock_ledger_entries |
| V201 | `pharmacy/src/main/resources/db/scripts/pharmacy` | seeds `PHARMACY_*` rights, maps them to `SUPER_ADMIN`, adds a `PHARMACIST` role |
| V300 | `outpatient/src/main/resources/db/scripts/outpatient` | creates the `patients` table for the outpatient module |
| V301 | `outpatient/src/main/resources/db/scripts/outpatient` | seeds `OUTPATIENT_PATIENT_MANAGE` and maps it to `SUPER_ADMIN` |
| V102 | `common/src/main/resources/db/scripts/common` | `facilities` table + nullable `users.facility_id`; seeds one demo facility |
| V400 | `doctor/src/main/resources/db/scripts/doctor` | doctor module DDL - doctors, doctor_facility_mappings(+hours), doctor_statuses, consultation_rates, free_review_policies |
| V401 | `doctor/src/main/resources/db/scripts/doctor` | seeds the `DOCTOR` and `HOSPITAL_ADMIN` roles and the module's rights |

`app/src/main/resources/db-scripts-overview.html` summarises the complete migration catalog across all modules; no SQL migrations remain under `app/src/main/resources`.

## Module structure

The codebase is a multi-module Gradle build, each module its own Gradle project under `com.lockdoc.<module>`:

| Module | Contains | Depends on |
|---|---|---|
| `common` | Auth/JWT/security config, `User`/`Role`/`Right`, generic exceptions (`ResourceNotFoundException`, `DuplicateResourceException`, `GlobalExceptionHandler`, `ApiErrorResponseFactory`), `PageResponse`, document-numbering (`DocumentNumberService`/`DocumentSequence`) | — |
| `outpatient` | Patients (`/outpatient/patients`) | `common` |
| `pharmacy` | Suppliers, medicines/batches, purchase orders, purchases/GRN, sales, sales returns, inventory, reports, plus the two pharmacy-specific exceptions (`InsufficientStockException`, `InvalidDocumentStateException`) and their own `PharmacyExceptionHandler` | `common`, `outpatient` |
| `doctor` | Doctor identity and Super-Admin verification (`/platform/doctors`), the doctor's own side (`/doctor/**`: profile, facility mappings + stated consulting hours, live status, proposing a consultation fee) and the facility's side (`/facility/**`: inviting doctors, approving fees, free-review policy, who is on duty) | `common` |
| `app` | `LockDocApplication` (main class, with explicit `@ComponentScan`/`@EntityScan`/`@EnableJpaRepositories(basePackages = "com.lockdoc")` since beans now span multiple modules), `application.properties`, Flyway scripts, tests | `common`, `pharmacy`, `outpatient`, `doctor` |

**Facility scoping**: `common` owns `Facility` and the optional `users.facility_id`. The doctor module is the first to scope by it — every facility-scoped service reads the acting facility from `SecurityUtils.requireFacilityId()` (off the request's `AppUserPrincipal`), never from a client-supplied parameter, because a tenant boundary that depends on a request field is not a boundary. `facility_id` is nullable and pharmacy/outpatient do not read it yet: a user with no facility (Super Admin) is refused by `requireFacilityId()` with a 403, which is correct — Super Admin acts through `/platform/**`.

**Rule**: `pharmacy` and `outpatient` never reach into each other's entities/repositories directly — the only link between them is `pharmacy`'s `SalesService` calling `outpatient`'s `PatientService` **bean** (e.g. `patientService.get(id)`) to validate a patient or resolve a name. `SalesInvoice` stores a plain `patientId` (no JPA relation to `Patient`). `common` is a shared kernel both feature modules depend on directly — that one *is* meant to be reached into like any other library, no service indirection needed. This retires the old "pharmacy sub-package exception" below this table used to document: each former `*.pharmacy` sub-package is now its own Gradle module, so there's nothing left to special-case.

## Security conventions

- Passwords are stored BCrypt-hashed (`BCryptPasswordEncoder`). Never store or log plaintext passwords.
- New protected endpoints do **not** need explicit annotation to require auth — anything not listed in `app.security.jwt.excluded-urls` is authenticated by default (secure-by-default posture in `SecurityConfig`).
- To make a new endpoint public, add its Ant pattern to `app.security.jwt.excluded-urls` in `application.properties` — do not special-case it in Java code.
- Role codes are exposed as Spring Security authorities prefixed with `ROLE_` (e.g. `ROLE_SUPER_ADMIN`). Right codes are exposed as-is (e.g. `USER_CREATE`) so both `hasRole()` and `hasAuthority()` checks work naturally.
- JWT secret lives in `app.security.jwt.secret`. For any environment beyond local dev, this must come from an environment variable / secrets manager, never committed in plaintext.

## Logging conventions

- Logging config: `src/main/resources/logback-spring.xml`.
- Rollover is **size-based only** (10MB per file, gzip archived, max 10 files kept) — do not silently switch this to time-based unless requested.
- Use SLF4J (`@Slf4j` from Lombok) — do not introduce `System.out.println` or another logging framework.

## Coding conventions

- Lombok is used throughout (`@Getter/@Setter/@Builder/@RequiredArgsConstructor`) — prefer it over manual boilerplate for new classes.
- Constructor injection only (via `@RequiredArgsConstructor` or explicit constructors) — no field `@Autowired`.
- DTOs live in `dto/`, never expose JPA entities directly from controllers for anything beyond the current simple `/users/me` example.
- Keep controllers thin; business logic belongs in `service/`.
- Entity ↔ DTO mapping is 100% manual - no MapStruct/ModelMapper. Response DTOs carry a small static `toResponse(Entity e)` mapper method (see any class under `pharmacy/src/.../dto/`).
- Services are concrete `@Service` classes, one per sub-domain - not interface+impl pairs.
- Repositories are plain `interface X extends JpaRepository<Entity, Long>` with derived query methods or a small `@Query`.

### Pagination convention

List endpoints use `common`'s `dto/PageResponse.java` (generic `<T>`: `content`, `page`, `size`, `totalElements`, `totalPages`). Controllers accept `@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size` (and an optional `search`), build a `Pageable`, call a repository method returning `Page<Entity>`, and map with `PageResponse.of(page, EntityResponse::toResponse)`.

### Method-level authorization (`@PreAuthorize`)

`SecurityConfig` now carries `@EnableMethodSecurity` in addition to `@EnableWebSecurity`. The pharmacy controllers are the first (and so far only) place in the codebase that annotate individual methods with `@PreAuthorize("hasAuthority('RIGHT_CODE')")`. This does not replace or bypass the `app.security.jwt.excluded-urls` allow-list mechanism (still the only way to make an endpoint public) - it is an additional, opt-in layer of per-right enforcement for authenticated endpoints. Existing (non-pharmacy) controllers are unaffected and remain "authenticated-by-default, no per-method right checks."

## Testing

- `LockDocApplicationTests` verifies the context loads (Flyway migrations + Spring context). Add focused unit/integration tests alongside new features.
- Tests should use a separate H2 file (`application-test.properties`) so they never collide with the dev database file.

## What NOT to do

- Don't add `spring.jpa.hibernate.ddl-auto=update` — schema drift will break Flyway validation.
- Don't hardcode the JWT secret elsewhere in code — always read from `JwtProperties`.
- Don't bypass the excluded-urls mechanism with ad-hoc permit-all annotations scattered across controllers — keep the public allow-list centralized in `application.properties`. (`@PreAuthorize` for *authorization*, as used in the pharmacy module, is fine — see above; it is distinct from making an endpoint public.)
- Don't remove or reuse a Flyway version number that has already shipped.
- Don't have `pharmacy` and `outpatient` reach into each other's entities/repositories directly — cross that boundary only through the other module's `@Service` bean (see "Module structure" above).

## Exception classes

Beyond the auth-related exceptions already handled in `common`'s `GlobalExceptionHandler` (`BadCredentialsException`, `AuthenticationException`, validation, etc.), the pharmacy module added four reusable exception types (all simple `RuntimeException` subclasses with a message constructor). `ResourceNotFoundException`/`DuplicateResourceException` are generic enough to live in `common` and are handled by `GlobalExceptionHandler`; `InvalidDocumentStateException`/`InsufficientStockException` are pharmacy-domain-specific, so they live in `pharmacy` and are handled by that module's own `PharmacyExceptionHandler` (`@RestControllerAdvice`) - both handlers share the same error-body shape via `common`'s `ApiErrorResponseFactory`:

| Exception | HTTP status | Used for |
|---|---|---|
| `ResourceNotFoundException` | 404 | entity lookup by id/code miss |
| `DuplicateResourceException` | 409 | uniqueness violation on create (e.g. medicine code) |
| `InvalidDocumentStateException` | 409 | illegal document status transition (e.g. approving an already-approved PO) |
| `InsufficientStockException` | 409 | a sale/cancel would drive batch stock negative |

New feature areas needing similar semantics should reuse these rather than inventing new exception types — with one deliberate exception already in place: the `doctor` module has its own `InvalidDocumentStateException` and `DoctorExceptionHandler`, mirroring pharmacy's, because the alternative was the doctor module depending on `pharmacy` for an exception class. If a third module needs those semantics, promote one copy into `common` rather than adding a third.

`IllegalStateException` is mapped to **403** in `common`'s `GlobalExceptionHandler`: the only place that throws it in anger is `SecurityUtils.requireFacilityId()`, and being refused there is an authorization answer, not a server fault.
