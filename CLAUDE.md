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

## Testing

- `LockDocApplicationTests` verifies the context loads (Flyway migrations + Spring context). Add focused unit/integration tests alongside new features.
- Tests should use a separate H2 file (`application-test.properties`) so they never collide with the dev database file.

## What NOT to do

- Don't add `spring.jpa.hibernate.ddl-auto=update` — schema drift will break Flyway validation.
- Don't hardcode the JWT secret elsewhere in code — always read from `JwtProperties`.
- Don't bypass the excluded-urls mechanism with ad-hoc `@PreAuthorize`/permit-all annotations scattered across controllers — keep the allow-list centralized in `application.properties`.
- Don't remove or reuse a Flyway version number that has already shipped.
