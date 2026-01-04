# Portfolio Project Plan — Multi-tenant Incident & Alerting Platform (Option A)

## Why this project (hiring impact + realism)
- Real backend concerns: multi-tenancy, auth, RBAC, idempotency, rate limiting, auditing, websockets, testing, CI.
- Still finishable solo with a focused MVP.

## Tech decisions (committed)
- Java: 21 (LTS) + Spring Boot 3.x
- Build: Maven
- DB: PostgreSQL (Docker) + Flyway migrations
- Cache/rate-limit: Redis (Docker)
- Docs: OpenAPI/Swagger (springdoc)
- Observability: Actuator + Micrometer (Prometheus-ready) + structured logging (JSON)
- Tests: Unit + Integration (Testcontainers)
- CI: GitHub Actions (build + tests)
- Docker Compose: app dependencies run locally

## MVP (must be finishable)
1) Auth & tenancy
   - Tenant signup (create tenant + owner user)
   - Login (JWT access token) + refresh token rotation
   - RBAC: OWNER, ADMIN, MEMBER
   - Tenant isolation on every request

2) Alerting core
   - CRUD Alert Rules (simple rule types)
   - Event ingest endpoint:
     - tenant-scoped
     - validation
     - idempotency via Idempotency-Key
   - Incidents list with pagination/filtering
   - Incident generation from rules

3) Notifications + audit
   - WebSocket notifications (tenant-scoped)
   - Audit log for important actions (login, rule changes, incident created)
   - Rate limiting for ingest endpoint (Redis-based)

## “Wow” features (2–3)
- Idempotent ingest (Idempotency-Key) with clear, test-covered behavior
- Tenant-safe WebSockets (auth + tenant channel isolation)
- Production-like error responses + structured logs + integration tests with Testcontainers

## Package structure (clean architecture in one Maven module)
com.example.incidentplatform
- api
  - controller
  - dto
- application
  - usecase
  - port
- domain
  - model
  - policy
- infrastructure
  - persistence
  - security
  - redis
  - websocket
  - email
- common
  - error
  - time
  - ids
  - utils
