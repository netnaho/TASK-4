# PharmaProcure Backend

Spring Boot 3 / Java 17 REST API for the PharmaProcure compliance procurement portal.

---

## Local run (backend only)

### Prerequisites

- Java 17 (Temurin/Eclipse recommended)
- Maven 3.9+
- PostgreSQL 16 (or use the provided Docker Compose for the DB only)

### Start a development database

```bash
docker run --rm -d \
  --name pharmaprocure-pg \
  -e POSTGRES_DB=pharmaprocure \
  -e POSTGRES_USER=pharmaprocure \
  -e POSTGRES_PASSWORD=pharmaprocure \
  -p 5432:5432 \
  postgres:16-alpine
```

### Run the application

```bash
cd backend
mvn spring-boot:run
```

The API is available at `http://localhost:8080`.

---

## Configuration / environment variables

| Environment variable              | Spring property                          | Default           | Description                                                                              |
|-----------------------------------|------------------------------------------|-------------------|------------------------------------------------------------------------------------------|
| `DB_HOST`                         | `spring.datasource.url` (host segment)   | `localhost`       | PostgreSQL hostname                                                                      |
| `DB_PORT`                         | `spring.datasource.url` (port segment)   | `5432`            | PostgreSQL port                                                                          |
| `DB_NAME`                         | `spring.datasource.url` (db segment)     | `pharmaprocure`   | Database name                                                                            |
| `DB_USERNAME`                     | `spring.datasource.username`             | `pharmaprocure`   | Database user                                                                            |
| `DB_PASSWORD`                     | `spring.datasource.password`             | `pharmaprocure`   | Database password                                                                        |
| `RATE_LIMIT_MAX_PER_MINUTE`       | `rate.limit.max-per-minute`              | `60`              | Per-user authenticated request rate limit                                                |
| `RATE_LIMIT_LOGIN_MAX_PER_MINUTE` | `rate.limit.login.max-per-minute`        | `20`              | Per-IP login attempt rate limit. **Hardened target is 20.** See docker-compose.yml note. |

### Document storage

| Property                              | Default                    | Description                                  |
|---------------------------------------|----------------------------|----------------------------------------------|
| `application.document.storage-root`  | `/app/data/documents`      | Filesystem path for document storage         |
| `application.document.signature-key-path` | `/app/data/keys/document-signing.key` | HMAC signing key for document watermarks |

---

## Database migrations

Flyway manages schema migrations. Migration scripts live in:

```
src/main/resources/db/migration/
```

Migrations run automatically on startup (`spring.flyway.enabled=true` in production).
The test profile disables Flyway and uses `spring.jpa.hibernate.ddl-auto: create-drop` with an in-memory H2 database instead.

To run migrations manually against a target database:

```bash
mvn flyway:migrate \
  -Dflyway.url=jdbc:postgresql://localhost:5432/pharmaprocure \
  -Dflyway.user=pharmaprocure \
  -Dflyway.password=pharmaprocure
```

---

## Test commands

### Unit and integration tests (all)

```bash
cd backend
mvn clean test
```

Tests use an H2 in-memory database via `src/test/resources/application-test.yml` and do not require a running PostgreSQL instance or Docker.

### Run a specific test class

```bash
mvn test -Dtest=CaptchaServiceTest
mvn test -Dtest=OrderControllerIntegrationTest
```

### Skip tests during build

```bash
mvn -DskipTests clean package
```

---

## Troubleshooting

**`Unable to acquire JDBC Connection` on startup**
- Ensure PostgreSQL is running and the `DB_*` environment variables are set correctly.
- Verify the database exists: `psql -U pharmaprocure -d pharmaprocure -c '\l'`

**`Flyway validation error: detected resolved migration not applied to database`**
- The production profile validates the schema against applied migrations. If you have local schema changes not in a migration file, either add a migration or set `spring.flyway.enabled=false` temporarily.

**`429 Rate limit exceeded` during API test runs**
- The login rate limit (`RATE_LIMIT_LOGIN_MAX_PER_MINUTE`) defaults to 20 req/min per source IP.  
- When running the full `run_tests.sh` suite, set the value to 60 in docker-compose (see the comment there) or wait 60 seconds between test suite runs.

**`AccountLockedException` in tests**
- The lockout threshold is 5 failed login attempts per 15-minute window. Use a unique username per test or reset `failed_login_attempts` and `lockout_until` in the DB.

**Document upload fails with `UNSUPPORTED_MEDIA_TYPE`**
- The API validates both the declared `Content-Type` and the file's magic-byte signature. Ensure the file is a genuine PDF, PNG, or WAV and the MIME type matches.
