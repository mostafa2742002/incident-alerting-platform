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



## Product story

### The problem
Many teams receive lots of system events (errors, CPU spikes, failed jobs). Without rules and routing, people miss critical issues or get spammed.

### The users
- A company ("tenant") with multiple users.
- Roles control who can manage rules vs only view incidents.

### How it works (high level)
1) A tenant configures alert rules (e.g., "if event type=PAYMENT_FAILED then create incident").
2) Systems send events to an ingest API (with an idempotency key so retries don't duplicate).
3) The app evaluates rules and opens/updates incidents.
4) Users get notified in real time (WebSocket) and via “email simulation”.
5) Everything important is recorded in an audit log.

### Core concepts
- Event: a signal sent by a system (has type, severity, timestamp, attributes).
- Alert Rule: a tenant-defined condition that turns events into incidents.
- Incident: a tracked problem that can be OPEN/ACKED/RESOLVED.
- Tenant: a company boundary for data + security.
