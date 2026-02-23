# Building Insurance Backend API

Spring Boot REST API for managing brokers, clients, buildings, policies, geography, currencies, risk factors, fees, and reporting.

## Tech stack

- Java 21
- Spring Boot (Web, Validation, Data JPA, Security)
- PostgreSQL (Docker Compose for local dev)
- Flyway (DB migrations)
- Maven (via `./mvnw`)

---

## Quick start

### Start database (optional)

```bash
docker compose up -d
```

### Run tests

```bash
./mvnw clean test
```

### Run app

```bash
./mvnw spring-boot:run
```

Default port: `8080`

---

## Configuration

### Application properties and profiles

- `src/main/resources` (e.g. `application.properties`, `application-dev.yml`)
- Test config: `src/test/resources`

### Common environment variables

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT` (default `8080`)

---

## Security

Many endpoints are protected and require authentication.

- Broker endpoints: `/api/brokers/**` → roles `BROKER` or `ADMIN`
- Admin endpoints: `/api/admin/**` → role `ADMIN`
- Geography endpoints: `/api/geography/**` → `ADMIN`

The project uses **JWT-based** authentication 

- An authentication endpoint issues a JWT token after successful login.
- All protected endpoints require the header:Authorization: Bearer <token>
- Role-based access control is enforced at endpoint level.
- Supported roles:
    - **BROKER**
    - **ADMIN**
---

## API documentation

When the application runs, OpenAPI docs are typically available at:

- `/v3/api-docs`
- Swagger UI (if enabled)

---

## Endpoints (summary)

> Note: Use standard JSON request/response bodies. Pageable endpoints accept `page/size` or Spring `Pageable` parameters.

### Clients (`/api/brokers/clients`)

- `POST /api/brokers/clients` — create client
- `POST /api/brokers/clients/search` — search clients (body: search DTO, pageable)
- `POST /api/brokers/clients/lookup` — lookup by name + identificationNumber
- `GET /api/brokers/clients/{clientId}` — get client
- `GET /api/brokers/clients/{clientId}/details` — get client details
- `GET /api/brokers/clients` — list clients (paged)
- `PUT /api/brokers/clients/{clientId}` — update client
- `PATCH /api/brokers/clients/{clientId}/identity` — patch client identity (authenticated)
- `DELETE /api/brokers/clients/{clientId}` — delete client

### Buildings (`/api/brokers/buildings`)

- `POST /api/brokers/buildings` — register a building
- `GET /api/brokers/buildings` — list buildings by `clientId` (paged)
- `GET /api/brokers/buildings/{buildingId}` — get building
- `GET /api/brokers/buildings/{buildingId}/details` — get building details
- `PUT /api/brokers/buildings/{buildingId}` — update building (full)
- `PATCH /api/brokers/buildings/{buildingId}` — patch building (partial)
- `DELETE /api/brokers/buildings/{buildingId}` — delete building

### Brokers (`/api/admin/brokers`)

- `POST /api/admin/brokers` — create broker
- `POST /api/admin/brokers/activate/{id}` — activate broker
- `POST /api/admin/brokers/deactivate/{id}` — deactivate broker
- `GET /api/admin/brokers/{id}` — get broker
- `GET /api/admin/brokers` — list brokers (paged)
- `PUT /api/admin/brokers/{id}` — update broker

### Geography

Countries (`/api/geography/countries`)
- `POST`, `GET` (paged, filter by name), `GET /{id}`, `PUT /{id}`, `DELETE /{id}`

Counties (`/api/geography/counties`)
- `POST`, `GET` (paged, filter by name), `GET /{id}`, `PUT /{id}`, `DELETE /{id}`

Cities (`/api/geography/cities`)
- `POST`, `GET` (paged, filter by name), `GET /{id}`, `PUT /{id}`, `DELETE /{id}`

### Currencies (`/api/admin/currencies`)

- `POST /api/admin/currencies` — register currency
- `GET /api/admin/currencies` — list (paged)
- `POST /api/admin/currencies/{id}/activate` — activate
- `POST /api/admin/currencies/{id}/deactivate` — deactivate

### Risk Factor Configuration (`/api/admin/risk-factors`)

- `POST /api/admin/risk-factors` — create config
- `GET /api/admin/risk-factors` — list (paged)
- `GET /api/admin/risk-factors/building/{buildingId}` — get factors for building (optional date param)
- `PUT /api/admin/risk-factors/{id}` — update config

### Fee Configuration (`/api/admin/fees`)

- `POST /api/admin/fees` — create fee config
- `GET /api/admin/fees` — list fees active on date (optional date param, pageable)
- `GET /api/admin/fees/{id}` — get fee by id
- `PUT /api/admin/fees/{id}` — update fee

### Policies (`/api/brokers/policies`)

- `POST /api/brokers/policies` — create draft policy
- `POST /api/brokers/policies/{id}/activate` — activate policy (authenticated)
- `POST /api/brokers/policies/{id}/cancel` — cancel policy (optional body, authenticated)
- `GET /api/brokers/policies` — list policies (filters: `clientId`, `status`, `startDate`, `endDate`, pagination)
- `GET /api/brokers/policies/{id}` — get policy details (authenticated)
- `PATCH /api/brokers/policies/{id}` — patch draft policy (authenticated)

### Reports (`/api/admin/reports`)

- `POST /api/admin/reports/policies` — generate policies report (body: report request DTO)

---

## Notes

- Default pageable sizes may be applied in controllers (commonly `size=5`).
- DTO names and JSON payloads are defined in `src/main/java/.../web/dto`. Check controller request/response DTOs for exact structures.

---

## Useful paths

- Source: `src/main/java`
- Tests: `src/test/java`
- Resources: `src/main/resources`
- Test config: `src/test/resources`
- Docker compose: `docker-compose.yml`

---

## Maintainer

Stefu Alexandru-Marian
