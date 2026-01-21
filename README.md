# Building Insurance (Backend)

Backend API for a **building insurance** application. In plain terms: it manages **clients**, **insured buildings**, and the **geography** data required to register buildings.

## What this project does
- **Clients**: create / update / search / view details .
- **Buildings**: register and manage insured buildings for a client .
- **Geography**: countries → counties → cities .
- **Validation + errors**: input validation with consistent, centralized error responses.
- **Audit/history support**: tracks important changes .
- **DB migrations**: database schema is managed through versioned migrations .

## Tech stack
- Java + Spring Boot
- Spring Web, Validation, Spring Data JPA
- Lombok
- Flyway (SQL migrations in `src/main/resources/db.migration/`)
- Maven Wrapper (`mvnw`)

## Run locally
```bash
# Windows
.\mvnw.cmd spring-boot:run
```

## Configs
- `application.properties`
- `application-dev.yml`
- `application-ci.yml`
- `application-test.yml`

## Tests
```bash
./mvnw clean test
# or
./mvnw clean verify
```

## Project structure (high level)
- `controller` – API endpoints
- `service` – business logic
- `facade` – orchestration layer (where needed)
- `repository` – database access
- `entity` – JPA entities
- `dto` – request/response models (client/building/city/county/country)
- `mapper` – entity <-> DTO mapping
- `error` – exceptions + global handler
- `resources/db.migration` – versioned SQL migrations

## Notes
- Flyway migrations run automatically on startup.
