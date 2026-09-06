# CLAUDE.md

Guidance for Claude (or any AI assistant) working in this repository.

## Project summary

LockDoc App is a Spring Boot 3 (Java 17) REST API:

- **Build tool**: Gradle (Groovy DSL, `build.gradle` / `settings.gradle`) — not Maven. Use `gradle bootRun`, `gradle build`, `gradle test`.
- **Config format**: `application.properties` — not YAML. If you add config, use dotted-key properties syntax (e.g. `app.cors.allowed-origins[0]=...` for list entries), and keep it consistent with the existing file rather than introducing a `.yml` alongside it.
- **Database**: H2, file-based (`jdbc:h2:file:./data/lockdocdb`) — never switch this to in-memory (`jdbc:h2:mem:`) without being asked; the whole point is durability across restarts.
- **Schema management**: Flyway only. Do not set `spring.jpa.hibernate.ddl-auto` to `update` or `create`. It must stay `validate` — the schema is owned by the versioned SQL scripts.
- **Security**: Stateless JWT auth via a custom `OncePerRequestFilter` (`JwtAuthenticationFilter`), not Spring Session. Excluded URLs are data-driven from `app.security.jwt.excluded-urls` in `application.properties`.
- **Context path**: `/lockdoc`. **Port**: `7321`. Do not change these unless explicitly instructed — other systems (UI apps on 5174/5175/5176) are coded against this base URL.
- **CORS**: origins are read from `app.cors.allowed-origins`. If a new UI port needs to be supported, add it there — do not hardcode `*`.

## Database change process (IMPORTANT)

All schema and seed-data changes MUST go through a new versioned Flyway script in:

```
src/main/resources/db/scripts/
```

Rules:
1. **Never edit a script that has already been committed/applied.** Flyway checksums applied migrations; editing one breaks `validate-on-migrate` for anyone who already ran it.
2. Create a new file following the pattern `V{next_number}__{snake_case_description}.sql`, e.g. `V3__add_user_last_login_column.sql`.
3. This project intentionally keeps all table DDL in one script (`V1`) and all default-data seeding in another (`V2`) — don't fragment future changes back into one-file-per-table unless asked. A genuinely new concern (e.g. a new feature's tables, or a later data migration) still deserves its own new versioned file rather than editing V1/V2.
4. Update the corresponding JPA entity in `entity/` to match, and bump any relevant repository/service code.
5. Document the new version in `CHANGELOG.md` under "Unreleased" or a new version heading.
6. Never write DDL directly against the running H2 file — always go through a script so the change is reproducible.

Current version ledger (do not renumber existing files):

| Version | File | Purpose |
|---|---|---|
| V1 | create_tables | users, roles, rights + user_roles/role_rights join tables (all DDL) |
| V2 | seed_default_data | seeds baseline rights, SUPER_ADMIN role (mapped to all rights), and default super-admin user `FP_USER` |
| V3 | create_pharmacy_tables | pharmacy module DDL - patients, suppliers, medicines, medicine_batches, document_sequences, purchase_orders(+items), purchases/GRN(+items), sales_invoices(+items), sales_returns(+items), stock_ledger_entries |
| V4 | seed_pharmacy_rights | seeds the 10 `PHARMACY_*` rights, maps them to `SUPER_ADMIN`, and adds a new `PHARMACIST` role mapped to all of them |
| V5 | seed_demo_suppliers_and_medicines | seeds 20 suppliers and 222 medicines transcribed from a real legacy "Medicine Stock Report" |
| V6 | seed_demo_medicine_batches_and_ledger | seeds demo medicine batches and stock ledger entries (opening-balance load) |
| V7 | enhance_supplier_contact_and_purchase_dues | adds supplier master-data columns (mobile2, landline, city/state/pincode, tin_no, website, supplier_type, drug_license_no); adds `amount_paid`/`balance_due`/`due_date` to `purchases` (GRN payables tracking, mirroring `sales_invoices`); adds `round_off_amount` to `sales_invoices` |
| V8 | fix_medicine_category_typos | corrects seed-data typos transcribed from the source report (`OINTEMENT`→`OINTMENT`, `SWEB`→`SWAB`, bandage/dressing items miscategorized as `GAUGE`→`GAUZE`) |

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
- Entity ↔ DTO mapping is 100% manual - no MapStruct/ModelMapper. Response DTOs carry a small static `toResponse(Entity e)` mapper method (see any class under `dto/pharmacy/`).
- Services are concrete `@Service` classes, one per sub-domain - not interface+impl pairs.
- Repositories are plain `interface X extends JpaRepository<Entity, Long>` with derived query methods or a small `@Query`.

### Pharmacy module package exception (deliberate)

The `pharmacy` module (patients, suppliers, medicines/batches, purchase orders, purchases/GRN, sales, sales returns, inventory, reports - 14 entities) is large enough that it is organized as a **sub-package under each layer**, as a deliberate, documented exception to the rest of the codebase's flat `controller/service/repository/entity/dto` layout:

```
controller.pharmacy / service.pharmacy / repository.pharmacy / entity.pharmacy / dto.pharmacy
```

Do not flatten these back into the top-level packages, and do not start sub-packaging other (smaller) areas of the app just because this precedent exists - this is specifically because of the pharmacy module's size.

### Pagination convention

List endpoints use `dto/PageResponse.java` (shared, top-level `dto` package, generic `<T>`: `content`, `page`, `size`, `totalElements`, `totalPages`). Controllers accept `@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size` (and an optional `search`), build a `Pageable`, call a repository method returning `Page<Entity>`, and map with `PageResponse.of(page, EntityResponse::toResponse)`.

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
- Don't flatten the `pharmacy` sub-packages back into the top-level layout, and don't copy that sub-packaging pattern into other small feature areas without a similar scale justification.

## Exception classes

Beyond the auth-related exceptions already handled in `GlobalExceptionHandler` (`BadCredentialsException`, `AuthenticationException`, validation, etc.), the pharmacy module added four reusable exception types in `exception/` (all simple `RuntimeException` subclasses with a message constructor, all handled in the same `GlobalExceptionHandler` - no new `@RestControllerAdvice` class):

| Exception | HTTP status | Used for |
|---|---|---|
| `ResourceNotFoundException` | 404 | entity lookup by id/code miss |
| `DuplicateResourceException` | 409 | uniqueness violation on create (e.g. medicine code) |
| `InvalidDocumentStateException` | 409 | illegal document status transition (e.g. approving an already-approved PO) |
| `InsufficientStockException` | 409 | a sale/cancel would drive batch stock negative |

New feature areas needing similar semantics should reuse these rather than inventing new exception types.
