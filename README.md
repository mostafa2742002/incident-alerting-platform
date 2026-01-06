# Incident & Alerting Platform (Multi-tenant) — Option A

A portfolio-grade Spring Boot application that turns incoming **Events** into **Incidents** using tenant-defined **Alert Rules**, then notifies users (later via WebSocket + email simulation).  
Each tenant is fully isolated from others.

---

## Tech Stack
- Java 21
- Spring Boot 3.x (Maven)
- PostgreSQL (Docker) + Flyway migrations
- Redis (Docker)
- OpenAPI / Swagger
- Spring Boot Actuator

---

## Local Development

### Requirements
- Java 21
- Docker + docker-compose

### Run dependencies
```bash
docker-compose up -d
```

### Run the app
```bash
./mvnw spring-boot:run
```

---

## URLs
- App: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs
- Postgres: localhost:15432
- Redis: localhost:16379

---

## Current Features
- Tenant signup (public)
- Tenant fetch by id (public)
- Consistent error responses:
  - validation errors
  - conflict errors
  - not found errors

---

## Architecture
This codebase follows a Clean-ish architecture to keep business logic independent from frameworks:

- **api** — controllers + DTOs (HTTP boundary)
- **application** — use cases (business workflows)
- **application/port** — interfaces the application depends on
- **domain** — pure domain models (no Spring / JPA annotations)
- **infrastructure** — adapters and implementations (JPA, security, etc.)
- **common/error** — global error handling models and exceptions

---

## Roadmap (Next)
- Users + Authentication (JWT + refresh tokens)
- Tenant isolation per request
- Role-based access control (OWNER / ADMIN / MEMBER)
- Event ingestion → Incident generation
- Notifications (WebSocket + email simulation)


---

## Database Architecture (ER Diagram)

The platform uses a **multi-tenant database design** where:
- Tenants represent companies
- Users can belong to multiple tenants
- Roles are assigned per tenant (RBAC)
- Authentication uses JWT + refresh tokens

```mermaid
erDiagram
    TENANTS ||--o{ TENANT_USERS : has
    USERS   ||--o{ TENANT_USERS : belongs_to
    USERS   ||--o{ REFRESH_TOKENS : has
    USERS   ||--o{ USER_ROLES : has
    ROLES   ||--o{ USER_ROLES : assigned

    TENANTS {
        UUID id PK
        VARCHAR slug "UNIQUE"
        VARCHAR name
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    USERS {
        UUID id PK
        VARCHAR email "UNIQUE"
        VARCHAR display_name
        VARCHAR password_hash
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    TENANT_USERS {
        UUID id PK
        UUID tenant_id FK
        UUID user_id FK
        VARCHAR role_code "OWNER | ADMIN | MEMBER"
        TIMESTAMPTZ created_at
    }

    REFRESH_TOKENS {
        UUID id PK
        UUID user_id FK
        VARCHAR token_hash "UNIQUE"
        TIMESTAMPTZ revoked_at "NULLABLE"
        TIMESTAMPTZ expires_at
        TIMESTAMPTZ created_at
    }

    ROLES {
        UUID id PK
        VARCHAR code "UNIQUE"
        VARCHAR name
        TIMESTAMPTZ created_at
    }

    USER_ROLES {
        UUID user_id FK
        UUID role_id FK
    }
