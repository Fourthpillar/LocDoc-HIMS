# LockDoc App

A Spring Boot 3 REST API backend with:

- **H2 file-based database** (data persisted to disk, not in-memory)
- **JWT authentication** with a configurable excluded-URL allow-list
- **CORS** enabled for local UI apps running on ports `5174`, `5175`, `5176`
- **User / Role / Right** model with a default super-admin user (`FP_USER`)
- **Spring Boot Actuator** health check
- **Size-based rolling file logs** via Logback
- **Flyway** versioned SQL migration scripts split by ownership: `common/src/main/resources/db/scripts/common`, `outpatient/src/main/resources/db/scripts/outpatient`, and `pharmacy/src/main/resources/db/scripts/pharmacy` (`app/src/main/resources/db-scripts-overview.html` summarises the full catalog)
- Application context path: `/lockdoc`, port: `7321`
- Multi-module Gradle build: `common`, `pharmacy`, `outpatient`, `app` (see `CLAUDE.md` for the module structure)

---

## 1. Requirements

| Tool | Version |
|------|---------|
| Java | 17+ |
| Gradle | 8.8+ (or use your IDE's bundled Gradle) |

> The Gradle wrapper is committed, so no local Gradle install is required — use `./gradlew` (or `gradlew.bat` on Windows) and it will download the right Gradle version on first run.

## 2. Running the application

```bash
gradle :app:bootRun
```

or build a jar and run it:

```bash
gradle clean build
java -jar app/build/libs/lockdoc-app.jar
```

On first startup:
- Flyway runs the versioned scripts from the owning modules (`common`, `outpatient`, `pharmacy`) to create the schema and bootstrap the shared default records.
- The H2 database file is created at `./data/lockdocdb.mv.db` (relative to the working directory).
- Logs are written to `./logs/lockdoc-app.log`.

The application will be available at:

```
http://localhost:7321/lockdoc
```

## 3. Default super admin user

| Field | Value |
|-------|-------|
| Username | `FP_USER` |
| Password | `FP@Admin#123` |
| Role | `SUPER_ADMIN` (mapped to every seeded right) |

> ⚠️ Change this password immediately in any non-local environment. It exists only to bootstrap the system.

## 4. Authentication flow

1. `POST /lockdoc/auth/login` with `{"username": "FP_USER", "password": "FP@Admin#123"}` → returns a JWT.
2. Send the token on subsequent requests: `Authorization: Bearer <token>`.
3. Requests to paths listed in `app.security.jwt.excluded-urls` (see `application.properties`) skip JWT validation — e.g. `/auth/login`, `/actuator/health`, `/h2-console/**`.

Example:

```bash
curl -X POST http://localhost:7321/lockdoc/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"FP_USER","password":"FP@Admin#123"}'

curl http://localhost:7321/lockdoc/users/me \
  -H "Authorization: Bearer <token-from-above>"
```

See [API.md](API.md) for the full endpoint reference.

## 5. Health check

Spring Boot Actuator is enabled:

```
GET http://localhost:7321/lockdoc/actuator/health
```

This path is included in the JWT excluded-urls list so monitoring tools can call it without a token.

## 6. Database

- Engine: H2, **file-based** (not in-memory) so data survives restarts.
- JDBC URL: `jdbc:h2:file:./data/lockdocdb;AUTO_SERVER=TRUE`
- H2 console (dev only): `http://localhost:7321/lockdoc/h2-console`
  - JDBC URL to use in the console: `jdbc:h2:file:./data/lockdocdb`
  - Username: `sa`, Password: *(blank)*

### DB versioned scripts

Flyway scripts are split by module ownership. There are no SQL files under `app/src/main/resources` other than the overview HTML document:

Each module owns a reserved block of 100 version numbers, so scripts can be added to any
module later without colliding with another module's versions:

```
common/src/main/resources/db/scripts/common/        (V100-V199)
  V100__create_tables.sql        -- users, roles, rights + join tables (core shared DDL)
  V101__seed_default_data.sql    -- default rights, SUPER_ADMIN role, FP_USER

pharmacy/src/main/resources/db/scripts/pharmacy/     (V200-V299)
  V200__create_pharmacy_tables.sql  -- pharmacy DDL only
  V201__seed_pharmacy_rights.sql    -- PHARMACY_* rights, SUPER_ADMIN mapping, PHARMACIST role

outpatient/src/main/resources/db/scripts/outpatient/ (V300-V399)
  V300__create_patient_tables.sql   -- patient table owned by outpatient
  V301__seed_outpatient_rights.sql  -- OUTPATIENT_PATIENT_MANAGE, SUPER_ADMIN mapping
```

`app/src/main/resources/db-scripts-overview.html` documents the full script catalog and module ownership. Flyway applies these automatically on startup, in order, and tracks the applied version in the `flyway_schema_history` table. **Never edit an already-applied script** — add a new `V{next_number_in_your_module's_block}__description.sql` file instead. See [CLAUDE.md](CLAUDE.md) for the versioning convention.

## 7. CORS

Allowed UI origins are configured under `app.cors.allowed-origins` in `application.properties`:

```properties
app.cors.allowed-origins[0]=http://localhost:5174
app.cors.allowed-origins[1]=http://localhost:5175
app.cors.allowed-origins[2]=http://localhost:5176
```

## 8. Logging

Logback is configured (`app/src/main/resources/logback-spring.xml`) with **size-based rollover**:

- Active log: `logs/lockdoc-app.log`
- Rolls over once it reaches **10 MB**
- Archives (gzip'd) kept in `logs/archived/`, up to 10 files
- A separate `logs/lockdoc-app-error.log` captures ERROR-level entries only

## 9. Project structure

Multi-module Gradle build - four modules, each its own Gradle project (see `CLAUDE.md`
for the dependency rules between them):

```
lockdoc-app/
├── build.gradle              # shared plugin/version config for all subprojects
├── settings.gradle           # include 'common', 'pharmacy', 'outpatient', 'app'
├── gradle.properties
├── README.md
├── CLAUDE.md
├── API.md
├── CHANGELOG.md
├── .gitignore
├── common/                   # shared kernel: auth, JWT/security, User/Role/Right,
│   └── src/main/java/com/lockdoc/common/{config,controller,dto,entity,exception,repository,service}
├── outpatient/                # patients
│   └── src/main/java/com/lockdoc/outpatient/{controller,service,repository,entity,dto}
├── pharmacy/                  # suppliers, medicines, purchases, sales, inventory, reports
│   └── src/main/java/com/lockdoc/pharmacy/{controller,service,repository,entity,dto,exception}
└── app/                        # the runnable Spring Boot module
    ├── src/main/java/com/lockdoc/app/LockDocApplication.java
    ├── src/main/resources/
    │   ├── application.properties
    │   ├── logback-spring.xml
    │   ├── db-scripts-overview.html   # summary of all Flyway scripts by module
    │   └── application.properties      # Flyway locations: common/, outpatient/, pharmacy/
    └── src/test/java/com/lockdoc/app/
```

## 10. Configuration reference

| Property | Purpose | Default |
|----------|---------|---------|
| `server.port` | HTTP port | `7321` |
| `server.servlet.context-path` | Base path | `/lockdoc` |
| `spring.datasource.url` | H2 file DB location | `jdbc:h2:file:./data/lockdocdb` |
| `app.cors.allowed-origins` | Allowed UI origins | `5174/5175/5176` |
| `app.security.jwt.secret` | HMAC signing key (Base64) | placeholder — change for prod |
| `app.security.jwt.expiration-ms` | Token TTL in ms | `3600000` (1h) |
| `app.security.jwt.excluded-urls` | Paths that skip JWT validation | see `application.properties` |

## 11. Running tests

```bash
gradle test
```

## 12. Building for production

- Replace `app.security.jwt.secret` with a securely generated, environment-supplied secret (never commit real secrets).
- Change the `FP_USER` default password.
- Point `spring.datasource.url` at a persistent volume path.
- Consider externalizing `app.cors.allowed-origins` per environment.
