# Journal App (Spring Boot)

A simple **journal logger** backend built with **Spring Boot**. It lets you create, read, update, and delete journal entries, and includes health checks, database migrations, and interactive API docs.

## What it does

* Stores journals with: `id`, `title`, `message`, `createdAt`, `lastModifiedAt`
* REST API for CRUD + pagination
* Health endpoints (app + DB)
* Automatic database migrations on startup
* Interactive API documentation with Swagger UI

---

## Tech stack & dependencies

* **Java 17** — project language level (runs fine on newer JDKs too)
* **Spring Boot 3.5.4**

  * `spring-boot-starter-web` — REST API
  * `spring-boot-starter-data-jpa` — ORM with Hibernate
  * `spring-boot-starter-actuator` — health, metrics, `/actuator/health`
  * `spring-boot-starter-validation` — request DTO validation
* **PostgreSQL Driver** — connects to RDS/Postgres
* **Flyway** (`flyway-core` + `flyway-database-postgresql`) — versioned SQL migrations
  *Why:* keeps schema in source control, repeatable, traceable.
  *Requirement:* migrations live in `src/main/resources/db/migration` and run on app start. We set `ddl-auto: validate` so Hibernate **validates** schema instead of changing it, letting Flyway be the single source of truth.
* **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui`) — Swagger UI & OpenAPI docs
  *Why:* discoverable, testable API; shareable OpenAPI spec for clients.
* **Lombok** — boilerplate reduction (getters/setters/constructors/builders)

---

## API overview

### Health

* `GET /api/health` → `"Journal App Works!"`
* `GET /api/health/db` → DB name, version, table counts
* `GET /actuator/health` → Actuator health (db, disk, probes)

### Journals

* `POST /api/journals` — create (body: `{ "title", "message" }`)
* `GET /api/journals/{id}` — get by id
* `GET /api/journals?page=0&size=10` — paged list
* `PUT /api/journals/{id}` — full update (body: `{ "title", "message" }`)
* `PATCH /api/journals/{id}` — partial update (body: `{ "title"? , "message"? }`)
* `DELETE /api/journals/{id}` — delete

### Data model

```json
{
  "id": 1,
  "title": "Day 1",
  "message": "Started the journal.",
  "createdAt": "2025-08-09T15:00:00Z",
  "lastModifiedAt": "2025-08-09T15:00:00Z"
}
```

---

## Project structure (high level)

```
src/main/java/com/sb/journalApp
├─ controller
│  ├─ HealthController.java
│  └─ JournalController.java
├─ dto
│  ├─ JournalRequest.java         # POST/PUT (validated)
│  ├─ JournalPatchRequest.java    # PATCH (optional fields)
│  └─ JournalResponse.java        # API responses
├─ mapper
│  └─ JournalMapper.java
├─ model
│  └─ Journal.java                # JPA entity
├─ repository
│  └─ JournalRepository.java
├─ service
│  └─ JournalService.java
├─ config
│  ├─ ApiExceptionHandler.java    # friendly error JSON
│  └─ OpenApiConfig.java          # Swagger/OpenAPI metadata
└─ JournalAppApplication.java

src/main/resources
├─ application.yml
└─ db/migration
   ├─ V1__create_entries_table.sql        # (example/demo)
   └─ V2__create_journals_table.sql       # journals schema
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
> so Spring will read this file at startup when the working directory is the project root.

### Database

* The app expects a reachable **PostgreSQL** (e.g., AWS RDS).
* On startup, **Flyway** runs migrations in `db/migration` and **Hibernate validates** schema.

---

## How to run

### 0) Prereqs

* JDK **17+**
* Network access to your Postgres (if RDS: "Publicly accessible = Yes" for dev, Security Group allows your IP on TCP 5432)
* `.env` created as above

### 1) Start the app

```bash
# from project root
./mvnw spring-boot:run
```

### 2) Check health

* App health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
* DB health: [http://localhost:8080/api/health/db](http://localhost:8080/api/health/db)

### 3) Open Swagger UI

* [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
  Try each endpoint directly in your browser.

---

## Sample requests (curl)

**Create**

```bash
curl -X POST http://localhost:8080/api/journals \
  -H "Content-Type: application/json" \
  -d '{"title":"Day 1","message":"Started the journal."}'
```

**List**

```bash
curl "http://localhost:8080/api/journals?page=0&size=5"
```

**Get by id**

```bash
curl http://localhost:8080/api/journals/1
```

**Update (PUT)**

```bash
curl -X PUT http://localhost:8080/api/journals/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Day 1 (edited)","message":"Refined thoughts."}'
```

**Partial update (PATCH)**

```bash
curl -X PATCH http://localhost:8080/api/journals/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Only title changed"}'
```

**Delete**

```bash
curl -X DELETE http://localhost:8080/api/journals/1 -i
```

---

## Notes & conventions

* **Migrations over DDL-auto:** `spring.jpa.hibernate.ddl-auto=validate` ensures schema is controlled by Flyway. Add/modify tables by creating new migration files (e.g., `V3__add_index.sql`), not by changing entities alone.
* **Timestamps:** managed in the **service** (UTC) to keep logic explicit.
* **Lombok:** make sure IntelliJ has the Lombok plugin and **Annotation Processing** enabled.

---

## Troubleshooting

* **Cannot connect to DB:** verify `.env` values, Security Group allows your current IP, and the RDS endpoint/port are correct.
* **Flyway "Unsupported Database"**: ensure you included **both** `flyway-core` **and** `flyway-database-postgresql` (same version).
* **Swagger not found:** confirm `springdoc-openapi-starter-webmvc-ui` is on the classpath and you're hitting `/swagger-ui.html`.

---

## License

MIT (or your preferred license).

---

Happy journaling!
