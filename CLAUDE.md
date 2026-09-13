# CLAUDE.md

Guidance for Claude (or any AI assistant) working in this repository.

## Project summary

LockDoc App is a Spring Boot 3 (Java 17) REST API:

- **Build tool**: Gradle multi-project build (Groovy DSL, `build.gradle` / `settings.gradle`) — not Maven. Use `./gradlew bootRun`, `./gradlew build`, `./gradlew test`. See "Module layout" below.
- **Config format**: `application.properties` — not YAML. If you add config, use dotted-key properties syntax (e.g. `app.cors.allowed-origins[0]=...` for list entries), and keep it consistent with the existing file rather than introducing a `.yml` alongside it.
- **Database**: H2, file-based (`jdbc:h2:file:./data/lockdocdb`) — never switch this to in-memory (`jdbc:h2:mem:`) without being asked; the whole point is durability across restarts.
- **Schema management**: Flyway only. Do not set `spring.jpa.hibernate.ddl-auto` to `update` or `create`. It must stay `validate` — the schema is owned by the versioned SQL scripts.
- **Security**: Stateless JWT auth via a custom `OncePerRequestFilter` (`JwtAuthenticationFilter`), not Spring Session. Excluded URLs are data-driven from `app.security.jwt.excluded-urls` in `application.properties`.
- **Context path**: `/lockdoc`. **Port**: `7321`. Do not change these unless explicitly instructed — other systems (UI apps on 5174/5175/5176) are coded against this base URL.
- **CORS**: origins are read from `app.cors.allowed-origins`. If a new UI port needs to be supported, add it there — do not hardcode `*`.

## Database change process (IMPORTANT)

Each module owns exactly **one DDL script and one DML script**, in its own version range:

| Module | Range | Location | DDL script | DML script |
|---|---|---|---|---|
| common | `V1xxx` | `common/src/main/resources/db/scripts/common/` | `V1001__create_user_facility_doctor_and_platform_tables.sql` | `V1002__insert_roles_rights_and_bootstrap_users.sql` |
| outpatient | `V2xxx` | `outpatient/src/main/resources/db/scripts/outpatient/` | `V2001__create_patient_visit_appointment_and_billing_tables.sql` | `V2002__insert_receptionist_role_and_outpatient_rights.sql` |
| doctor | `V3xxx` | `doctor/src/main/resources/db/scripts/doctor/` | `V3001__create_doctor_status_consultation_and_schedule_tables.sql` | `V3002__insert_doctor_rights.sql` |

File names describe **what the script does**, not which module it belongs to (the folder and version range already say
that): `V{version}__{verb}_{what}.sql`, e.g. `create_..._tables`, `insert_..._rights`, `add_..._column`. The separator
after the version must be a **double** underscore — Flyway ignores files named `V1003_something.sql`.

`spring.flyway.locations` (in `app/src/main/resources/application.properties`) lists all three. The ranges follow
the module dependency order (`common` ← `outpatient` ← `doctor`), so on a fresh database every module's tables exist
before a higher module's foreign keys point at them: doctor tables reference `op_visits` and `doctor_schedules`, which
is why doctor is `V3xxx` and not `V2xxx`. A lower module's script must never reference a higher module's table or
role/right.

What goes where:
- **DDL** — tables, constraints and indexes for the entities in that module's `entity` package.
- **DML** — the roles defined by that module, the rights its controllers check (`@PreAuthorize`), and those rights'
  role mappings. A mapping to a role from a lower module is fine (e.g. doctor's DML grants `RECEPTIONIST`, created
  in outpatient's DML); a mapping to a role from a higher module is not.

Rules:
1. **Never edit a script that has already been applied to a shared database.** Flyway checksums applied migrations;
   editing one breaks `validate-on-migrate` for anyone who already ran it. Until the scripts ship to a shared
   environment, folding a change into the module's existing DDL/DML file is preferred over adding a new one.
2. Once they have shipped, add the next version **in the owning module's range**, e.g. `V1003__add_user_last_login_column.sql`
   in common. `spring.flyway.out-of-order=true` is required for this: a new `V1003` sorts below an already-applied
   `V3002`, and without out-of-order Flyway would refuse it. Don't turn it off.
3. Update the corresponding JPA entity in the owning module's `entity` package to match, and bump any relevant
   repository/service code. `spring.jpa.hibernate.ddl-auto=validate` will fail startup on any drift.
4. Document the change in `CHANGELOG.md` under "Unreleased" or a new version heading.
5. Never write DDL directly against the running H2 file — always go through a script so the change is reproducible.

## Security conventions

- Passwords are stored BCrypt-hashed (`BCryptPasswordEncoder`). Never store or log plaintext passwords.
- New protected endpoints do **not** need explicit annotation to require auth — anything not listed in `app.security.jwt.excluded-urls` is authenticated by default (secure-by-default posture in `SecurityConfig`).
- To make a new endpoint public, add its Ant pattern to `app.security.jwt.excluded-urls` in `application.properties` — do not special-case it in Java code.
- Role codes are exposed as Spring Security authorities prefixed with `ROLE_` (e.g. `ROLE_SUPER_ADMIN`). Right codes are exposed as-is (e.g. `USER_CREATE`) so both `hasRole()` and `hasAuthority()` checks work naturally.
- JWT secret lives in `app.security.jwt.secret`. For any environment beyond local dev, this must come from an environment variable / secrets manager, never committed in plaintext.

## Logging conventions

- Logging config: `app/src/main/resources/logback-spring.xml`.
- Rollover is **size-based only** (10MB per file, gzip archived, max 10 files kept) — do not silently switch this to time-based unless requested.
- Use SLF4J (`@Slf4j` from Lombok) — do not introduce `System.out.println` or another logging framework.

## Coding conventions

- Lombok is used throughout (`@Getter/@Setter/@Builder/@RequiredArgsConstructor`) — prefer it over manual boilerplate for new classes.
- Constructor injection only (via `@RequiredArgsConstructor` or explicit constructors) — no field `@Autowired`.
- DTOs live in `dto/`, never expose JPA entities directly from controllers for anything beyond the current simple `/users/me` example.
- Keep controllers thin; business logic belongs in `service/`.
- Entity ↔ DTO mapping is 100% manual - no MapStruct/ModelMapper. Response DTOs carry a small static `toResponse(Entity e)` mapper method (see any class under `com.lockdoc.outpatient.dto`).
- Services are concrete `@Service` classes, one per sub-domain - not interface+impl pairs.
- Repositories are plain `interface X extends JpaRepository<Entity, Long>` with derived query methods or a small `@Query`.

### Module layout

```
common/      com.lockdoc.common.{config,controller,dto,entity,exception,repository,service}[.platform]
outpatient/  com.lockdoc.outpatient.{controller,dto,entity,repository,service}[.dataprotection]
doctor/      com.lockdoc.doctor.{controller,dto,entity,repository,service}
app/         com.lockdoc.app.LockDocApplication + application.properties, logback-spring.xml, tests
```

- Dependencies are strictly one-way: `common` ← `outpatient` ← `doctor` ← `app`. Gradle rejects cycles, so
  `common` must never reference outpatient/doctor classes and `outpatient` must never reference doctor classes.
- When a lower module needs something a higher one owns, move the shared piece down (as was done for
  `ConsultationRate`, `FreeReviewPolicy`, `DoctorFacilityMapping(+Hour)` → `common`, `FreeReviewLink` → `outpatient`)
  or move the consumer up (as with `DoctorScheduleController` → `doctor`). Don't add a reverse dependency.
- `LockDocApplication` widens component, entity and repository scanning to `com.lockdoc`; new modules under that
  root are picked up automatically once `app/build.gradle` depends on them.
- `bootRun` and `test` run with the repository root as working directory, so `./data` and `./logs` stay at the root.
- The pharmacy module (code, tables, rights, `PHARMACIST` role) has been removed entirely. Don't reintroduce it without being asked.

### Pagination convention

List endpoints use `com.lockdoc.common.dto.PageResponse` (shared, generic `<T>`: `content`, `page`, `size`, `totalElements`, `totalPages`). Controllers accept `@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size` (and an optional `search`), build a `Pageable`, call a repository method returning `Page<Entity>`, and map with `PageResponse.of(page, EntityResponse::toResponse)`.

### Method-level authorization (`@PreAuthorize`)

`SecurityConfig` carries `@EnableMethodSecurity` in addition to `@EnableWebSecurity`. Controllers across all modules annotate classes or methods with `@PreAuthorize("hasAuthority('RIGHT_CODE')")`. This does not replace or bypass the `app.security.jwt.excluded-urls` allow-list mechanism (still the only way to make an endpoint public) - it is an additional, opt-in layer of per-right enforcement for authenticated endpoints.

## Testing

- Tests live in `app/src/test` (the only module with the full context). `LockDocApplicationTests` verifies the context loads (Flyway migrations + Spring context). Add focused unit/integration tests alongside new features.
- Tests should use a separate H2 file (`application-test.properties`) so they never collide with the dev database file.

## What NOT to do

- Don't add `spring.jpa.hibernate.ddl-auto=update` — schema drift will break Flyway validation.
- Don't hardcode the JWT secret elsewhere in code — always read from `JwtProperties`.
- Don't bypass the excluded-urls mechanism with ad-hoc permit-all annotations scattered across controllers — keep the public allow-list centralized in `application.properties`. (`@PreAuthorize` for *authorization* is fine — see above; it is distinct from making an endpoint public.)
- Don't remove or reuse a Flyway version number that has already shipped.
- Don't add a dependency from `common` to `outpatient`/`doctor`, or from `outpatient` to `doctor` (see "Module layout").

## Exception classes

Beyond the auth-related exceptions already handled in `GlobalExceptionHandler` (`BadCredentialsException`, `AuthenticationException`, validation, etc.), `com.lockdoc.common.exception` has three reusable exception types (all simple `RuntimeException` subclasses with a message constructor, all handled in the same `GlobalExceptionHandler` - no new `@RestControllerAdvice` class):

| Exception | HTTP status | Used for |
|---|---|---|
| `ResourceNotFoundException` | 404 | entity lookup by id/code miss |
| `DuplicateResourceException` | 409 | uniqueness violation on create (e.g. medicine code) |
| `InvalidDocumentStateException` | 409 | illegal document status transition (e.g. closing an already-closed counter session) |

New feature areas needing similar semantics should reuse these rather than inventing new exception types.
