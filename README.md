# Journal App (Spring Boot)

A simple **journal logger** backend built with **Spring Boot**.
You can create users, **log in** to get a JWT, and then create/read/update/delete **your own** journal entries. Includes health checks, Flyway migrations, and Swagger UI.

---

## What it does

* **Users** with unique usernames and **BCrypt-hashed** passwords
* **JWT login** (`/api/auth/login`) → Bearer token for subsequent requests
* **Ownership enforcement**: every journal belongs to exactly one user; only the owner can read/update/delete it
* Journals: `id`, `title`, `message`, `createdAt`, `lastModifiedAt`, `userId (owner)`
* CRUD + pagination
* Health endpoints (app + DB)
* **Flyway** migrations on startup
* **Swagger UI** with global “Authorize” (Bearer JWT)

---

## Why these choices (design & tradeoffs)

**Auth strategy — Self-issued JWT (HS256)**

* *Options we had*: traditional sessions, OAuth 2.0/OpenID Connect with an external provider (Okta/Google/Auth0), or stateless JWT.
* *What we picked*: **JWT** (self-issued) with a symmetric secret (**HS256**).
* *Why*: minimal overhead, works great for SPAs/mobile, keeps the app stateless (no server sessions), easy to test locally.
* *Tradeoffs*: rotate secrets carefully; don’t put long-lived sensitive data in tokens; in production you might switch to an external IdP (OAuth/OIDC) or asymmetric keys (RS256) for better key management.

**DB migrations — Flyway**

* *Why*: schema in source control; reproducible & auditable upgrades.
* *How*: `ddl-auto: validate` → Hibernate validates, **Flyway owns schema changes**.

**Strict ownership in the service layer**

* Journals are always created for the **current JWT user** (no `userId` in the request).
* Reads/updates/deletes verify the caller is the owner (`403 FORBIDDEN` otherwise).

---

## Tech stack & dependencies

* **Java 17**
* **Spring Boot 3.5.4**

  * `spring-boot-starter-web` — REST API
  * `spring-boot-starter-data-jpa` — ORM (Hibernate)
  * `spring-boot-starter-actuator` — `/actuator/health`
  * `spring-boot-starter-validation` — DTO validation
  * `spring-boot-starter-oauth2-resource-server` — validates Bearer JWTs
  * `spring-security-oauth2-jose` — JWT encode/decode (Nimbus)
* **Spring Security Crypto** — `PasswordEncoder` (BCrypt)
* **PostgreSQL Driver**
* **Flyway** (`flyway-core`, `flyway-database-postgresql`)
* **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui`) — Swagger UI
* **Lombok** — reduce boilerplate

---

## Data model

**Users**

* `id` (BIGSERIAL / identity)
* `name`
* `username` (**unique**, letters only, ≥ 5 chars; validated)
* `password` (**BCrypt hash**, never returned in APIs)

**Journals**

* `id`, `title`, `message`, `createdAt`, `lastModifiedAt`
* `user_id` (**NOT NULL**) → FK to `users(id)`, **ON DELETE CASCADE**

> Responses include `userId` on a journal.
> `UserResponse` also exposes `journalIds` (derived) for convenience.

---

## API overview

### Public (no token)

* `POST /api/users` — **sign up**
* `POST /api/auth/login` — **get JWT**
* `GET /api/health`, `GET /api/health/db`, `GET /actuator/health`
* Swagger docs: `/swagger-ui.html`, `/v3/api-docs/**`

### Requires Bearer JWT

* `GET /api/users/{id}`, `PUT /api/users/{id}`, `DELETE /api/users/{id}` (basic CRUD)
* **Convenience on current user**:

  * `GET /api/users/me`
  * `PUT /api/users/me`
  * `DELETE /api/users/me`
* **Journals (owned by current user)**:

  * `POST /api/journals` — create **for current user** (no `userId` in body)
  * `GET /api/journals/{id}` — owner only
  * `GET /api/journals?page=0&size=10` — list **current user’s** journals (newest first)
  * `PUT /api/journals/{id}` — owner only
  * `PATCH /api/journals/{id}` — owner only
  * `DELETE /api/journals/{id}` — owner only

---

## Project structure (high level)

```
src/main/java/com/sb/journalApp
├─ config
│  ├─ ApiExceptionHandler.java          # friendly JSON errors (400/401/403/404/409)
│  ├─ JwtConfig.java                    # JwtEncoder/Decoder (HS256)
│  ├─ OpenApiConfig.java                # Swagger + bearerAuth scheme
│  ├─ SecurityConfig.java               # stateless, JWT, public routes, 401/403 JSON
│  └─ WebConfig.java                    # stable Page JSON via DTO
├─ controller
│  ├─ AuthController.java               # /api/auth/login
│  ├─ HealthController.java             # health endpoints
│  ├─ JournalController.java            # journal CRUD (owned by caller)
│  └─ UserController.java               # user CRUD + /me endpoints
├─ dto
│  ├─ LoginRequest.java, TokenResponse.java
│  ├─ JournalRequest.java, JournalPatchRequest.java, JournalResponse.java
│  ├─ UserRequest.java, UserResponse.java
├─ mapper
│  ├─ JournalMapper.java
│  └─ UserMapper.java
├─ model
│  ├─ Journal.java
│  └─ User.java
├─ repository
│  ├─ JournalRepository.java
│  └─ UserRepository.java
├─ service
│  ├─ Auth.java                         # helper: extract uid from SecurityContext
│  ├─ DbHealthService.java
│  ├─ JournalService.java               # ownership checks here
│  └─ UserService.java
└─ JournalApplication.java

src/main/resources
├─ application.yml
└─ db/migration
   ├─ V2__create_journals_table.sql
   ├─ V3__create_users_table.sql
   └─ V4__add_fk_journals_user.sql
```

---

## Configuration

We load secrets from a local **`.env`** via `spring.config.import`.

### `.env` (not committed)

Create in project root:

```env
DB_HOST=your-rds-endpoint.rds.amazonaws.com
DB_PORT=5432
DB_NAME=journaldb
DB_USER=journal_user
DB_PASSWORD=your-strong-password

# HS256 key (length ≥ 32 chars)
APP_JWT_SECRET=change-me-to-a-long-random-string-at-least-32-characters
```

`application.yml` already contains:

```yaml
spring:
  config:
    import: optional:file:.env[.properties]

app:
  jwt:
    secret: ${APP_JWT_SECRET}
```

---

## How to run

### 0) Prereqs

* JDK **17+**
* Network access to Postgres (for RDS dev: “Publicly accessible = Yes”, SG allows your IP on 5432)
* `.env` created as above (including `APP_JWT_SECRET`)

### 1) Start the app

```bash
./mvnw spring-boot:run
```

### 2) Check health

* App: `http://localhost:8080/actuator/health`
* DB:  `http://localhost:8080/api/health/db`

### 3) Open Swagger UI

* `http://localhost:8080/swagger-ui.html`
  Use the **Authorize** button (top-right) to paste `Bearer <your-jwt>` once you’ve logged in.

---

## Smoke test (curl)

### 1) Sign up (public)

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","username":"alice","password":"P@ssw0rd!"}'
```

### 2) Log in (public) → get token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"P@ssw0rd!"}' \
  | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')
echo "$TOKEN"
```

### 3) Create a journal (requires Bearer JWT)

```bash
curl -X POST http://localhost:8080/api/journals \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Day 1","message":"Started the journal."}'
```

### 4) List **my** journals

```bash
curl "http://localhost:8080/api/journals?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN"
```

### 5) Update & Delete (owner-only)

```bash
curl -X PUT http://localhost:8080/api/journals/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Day 1 (edited)","message":"More details."}'

curl -X DELETE http://localhost:8080/api/journals/1 \
  -H "Authorization: Bearer $TOKEN" -i
```

### 6) Current user convenience endpoints

```bash
curl http://localhost:8080/api/users/me -H "Authorization: Bearer $TOKEN"

curl -X PUT http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Smith","username":"alice","password":"N3wP@ssw0rd!"}'
```

---

## Swagger “Authorize” (Bearer JWT)

* Open Swagger UI → **Authorize**.
* Enter: `Bearer <paste your accessToken here>`.
* Now protected endpoints will include the token automatically.

---

## Notes & conventions

* **JWTs** expire after 1 hour by default. In production, add refresh token/rotation and secret rotation.
* **Stateless** security: sessions disabled; CSRF disabled (we use Bearer tokens, not cookies).
* **Exceptions**:

  * 401: missing/invalid token (custom JSON)
  * 403: not your resource (ownership check)
  * 404/409/400: validation or constraint errors → unified JSON from `ApiExceptionHandler`
* **Migrations over DDL-auto**: keep schema changes in new `V__` files.
* **Lombok**: ensure plugin + annotation processing are enabled.

---

## Troubleshooting

* **Login error “Failed to select a JWK signing key”**
  Ensure you’re setting the JWS header to HS256 when encoding:

  ```java
  var header = JwsHeader.with(MacAlgorithm.HS256).build();
  var token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  ```

  And `APP_JWT_SECRET` is ≥ 32 chars and loaded.

* **401 on protected routes**
  Add header `Authorization: Bearer <token>`. Check token hasn’t expired.

* **400 on journal create**
  Body must include non-blank `title` and `message` (no `userId` needed now).

* **DB connection issues**
  Verify `.env`, SG/firewall rules, RDS endpoint, and port.

* **Flyway checksum/history errors (dev)**
  Prefer new migration files. If you must, clean the specific version row in `flyway_schema_history` (avoid in prod).

---

## License

MIT (or your preferred license)

---

**Happy journaling — securely!**
