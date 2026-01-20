# Building Insurance (Backend)

Backend API for a **building insurance** application. In plain terms: it manages **clients**, **insured buildings**, and the **geography** data (country/county/city) required to register buildings.

## What this project does
- **Clients**: create / update / search / view details (individual or company).
- **Buildings**: register and manage insured buildings for a client (each building is linked to a city).
- **Geography**: countries → counties → cities (used for selection and validation).
- **Validation + errors**: input validation with consistent, centralized error responses.
- **Audit/history support**: tracks important changes (e.g., client identity updates).
- **DB migrations**: database schema is managed through versioned migrations (Flyway).

## Tech stack
- Java + Spring Boot
- Spring Web, Validation, Spring Data JPA
- Lombok
- Flyway (SQL migrations in `src/main/resources/db.migration/`)
- Maven Wrapper (`mvnw`)

## Run locally
```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

## Configs
- `application.properties`
- `application-dev.yml`
- `application-ci.yml`

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
- `mapper` – entity ↔ DTO mapping
- `error` – exceptions + global handler
- `resources/db.migration` – versioned SQL migrations

## Notes
- Flyway migrations run automatically on startup.
- Use the `dev` config/profile for local work and `ci` for pipelines.
