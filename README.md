
# Journal App (Spring Boot)

A simple **journal logger** backend built with **Spring Boot**. It lets you create users, create/read/update/delete journal entries **owned by a user**, and includes health checks, database migrations, and interactive API docs.

## What it does

* Users with unique usernames and hashed passwords (BCrypt)
* Journals with: `id`, `title`, `message`, `createdAt`, `lastModifiedAt`, **userId (owner)**
* Strict ownership: every journal belongs to a user (`journals.user_id` is **NOT NULL**, FK **ON DELETE CASCADE**)
* REST API for CRUD + pagination
* Health endpoints (app + DB)
* Automatic database migrations on startup (Flyway)
* Interactive API documentation with Swagger UI

---

## Tech stack & dependencies

* **Java 17**
* **Spring Boot 3.5.4**

  * `spring-boot-starter-web` — REST API
  * `spring-boot-starter-data-jpa` — ORM (Hibernate)
  * `spring-boot-starter-actuator` — health, metrics (`/actuator/health`)
  * `spring-boot-starter-validation` — DTO validation
* **PostgreSQL Driver** — connects to RDS/Postgres
* **Flyway** (`flyway-core` + `flyway-database-postgresql`) — versioned SQL migrations
  *Why:* schema in source control; reproducible & auditable.
  *Setup:* migrations live in `src/main/resources/db/migration`. We use `ddl-auto: validate` so Hibernate **validates** schema; Flyway is the single source of truth.
* **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui`) — Swagger UI & OpenAPI docs
  *Why:* easy discoverability/testing; client generation via OpenAPI.
* **Lombok** — less boilerplate (getters/setters/constructors/builders)
* **Spring Security Crypto** (`spring-security-crypto`) — BCrypt password hashing

---

## Data model

**Users**

* `id` (IDENTITY / BIGSERIAL)
* `name`
* `username` (**unique**, letters only, ≥ 5 chars; validated)
* `password` (**BCrypt hash**, not the raw password)

**Journals**

* `id`, `title`, `message`, `createdAt`, `lastModifiedAt`
* `user_id` (**required**) → FK to `users(id)`, **ON DELETE CASCADE**

> We keep relations normalized: a user “has many journals” via `journals.user_id`.
> Responses include `userId` on a journal, and a user response includes `journalIds` (derived) for convenience.

---

## API overview

### Health

* `GET /api/health` → `"Journal App Works!"`
* `GET /api/health/db` → DB name, version, table counts
* `GET /actuator/health` → Actuator health (db, disk, probes)

### Users

* `POST /api/users` — create user
  Body:

  ```json
  { "name": "Alice", "username": "alice", "password": "P@ssw0rd!" }
  ```

  * `username`: letters only, ≥5 characters (validated)
  * `password`: ≥8 characters; letters/digits/special characters allowed (validated)
  * Password is stored **hashed** with BCrypt.
* `GET /api/users/{id}` — get by id
* `GET /api/users?page=0&size=10` — list
* `PUT /api/users/{id}` — full update (send all fields)
* `DELETE /api/users/{id}` — delete user (**cascades** to delete their journals)

### Journals

* `POST /api/journals` — create (owner **required**)
  Body:

  ```json
  { "title": "Day 1", "message": "Started the journal.", "userId": 1 }
  ```
* `GET /api/journals/{id}` — get by id
* `GET /api/journals?page=0&size=10` — paged list (sorted by `createdAt` desc)
* `PUT /api/journals/{id}` — full update (body includes `title`, `message`; `userId` optional if you want to **reassign** the owner)
* `PATCH /api/journals/{id}` — *optional* partial update (if enabled in your code)
* `DELETE /api/journals/{id}` — delete

---

## Project structure (high level)

```
src/main/java/com/sb/journalApp
├─ config
│  ├─ ApiExceptionHandler.java
│  └─ OpenApiConfig.java
├─ controller
│  ├─ HealthController.java
│  ├─ JournalController.java
│  └─ UserController.java
├─ dto
│  ├─ JournalCreateRequest.java    # POST (requires userId)
│  ├─ JournalUpdateRequest.java    # PUT (optionally reassign userId)
│  ├─ JournalResponse.java
│  ├─ UserRequest.java             # POST/PUT (validated; raw password comes in)
│  └─ UserResponse.java            # never exposes password
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
│  ├─ JournalService.java
│  └─ UserService.java
└─ JournalApplication.java

src/main/resources
├─ application.yml
└─ db/migration
   ├─ V2__create_journals_table.sql      # journals (+ user_id column, NOT NULL)
   ├─ V3__create_users_table.sql         # users (username unique)
   └─ V4__add_fk_journals_user.sql       # FK ON DELETE CASCADE
```

---

## Configuration

We keep secrets out of git and load them from a local **`.env`** file via `spring.config.import`.

### Create `.env` (not committed)

Create a file named `.env` in the project root (same folder as `pom.xml`):

```env
DB_HOST=your-rds-endpoint.rds.amazonaws.com
DB_PORT=5432
DB_NAME=journaldb
DB_USER=journal_user
DB_PASSWORD=your-strong-password
```

> Your `application.yml` already contains
> `spring.config.import: optional:file:.env[.properties]`
> so Spring reads this file at startup.

### Database

* The app expects a reachable **PostgreSQL** (e.g., AWS RDS).
* On startup, **Flyway** runs migrations in `db/migration` and **Hibernate validates** schema.

---

## How to run

### 0) Prereqs

* JDK **17+**
* Network access to your Postgres (for RDS dev: “Publicly accessible = Yes”, Security Group allows your IP on TCP 5432)
* `.env` created as above

### 1) Start the app

```bash
./mvnw spring-boot:run
```

### 2) Check health

* App health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
* DB health: [http://localhost:8080/api/health/db](http://localhost:8080/api/health/db)

### 3) Open Swagger UI

* [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Quick smoke test (curl)

**Create user**

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","username":"alice","password":"P@ssw0rd!"}'
```

**Create journal (owner required)**

```bash
curl -X POST http://localhost:8080/api/journals \
  -H "Content-Type: application/json" \
  -d '{"title":"Day 1","message":"Started the journal.","userId":1}'
```

**List journals**

```bash
curl "http://localhost:8080/api/journals?page=0&size=5"
```

**Get journal**

```bash
curl http://localhost:8080/api/journals/1
```

**Update journal (PUT)**

```bash
curl -X PUT http://localhost:8080/api/journals/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Day 1 (edited)","message":"More details.","userId":1}'
```

**Delete journal**

```bash
curl -X DELETE http://localhost:8080/api/journals/1 -i
```

**Delete user (cascades journals)**

```bash
curl -X DELETE http://localhost:8080/api/users/1 -i
```

---

## Notes & conventions

* **Migrations over DDL-auto:** `spring.jpa.hibernate.ddl-auto=validate` keeps schema under Flyway. Add/modify tables by creating new migration files (e.g., `V5__add_index.sql`), not by changing entities alone.
* **Password storage:** raw password is only accepted in the request and stored **hashed** with BCrypt.
* **Validation:**

  * `username`: letters only, ≥5 characters
  * `password`: ≥8 characters; letters/digits/special characters allowed
* **Timestamps:** set in the **service** in UTC.
* **Lombok:** enable the Lombok plugin + **Annotation Processing** in IntelliJ.

---

## Troubleshooting

* **400 on journal create:** ensure your JSON is valid and includes a numeric `userId` (e.g., `1`, not `{{1}}`).
* **“User not found” on journal create:** create the user first and use its `id`.
* **Cannot connect to DB:** verify `.env` values, Security Group allows your current IP, RDS endpoint/port are correct.
* **Flyway checksum/history errors (dev):** either add a new migration or carefully clean the specific version row from `flyway_schema_history` and rerun (production: always prefer new migrations).
* **Swagger not found:** ensure `springdoc-openapi-starter-webmvc-ui` is on the classpath and visit `/swagger-ui.html`.

---

## License

MIT (or your preferred license).

---

Happy journaling!
