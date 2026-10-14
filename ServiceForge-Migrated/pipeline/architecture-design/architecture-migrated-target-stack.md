# Architecture Design — ServiceForge-Migrated target stack (Java 21 + React + Entra + SQLite local-only)

Date: 2026-10-14  
Scope: Features 1–3 only (per migrated specs)

## 0) Inputs (strict)

This architecture is derived strictly from:
- ServiceForge-Migrated/pipeline/features/feature-1-technician-availability-scheduling-view.md
- ServiceForge-Migrated/pipeline/features/feature-2-job-booking.md
- ServiceForge-Migrated/pipeline/features/feature-3-parts-catalog.md

And cross-cutting ADRs:
- ServiceForge-Migrated/pipeline/decisions/adr-0001-target-tech-stack.md
- ServiceForge-Migrated/pipeline/decisions/adr-0002-auth-azure-entra.md
- ServiceForge-Migrated/pipeline/decisions/adr-0003-persistence-sqlite-local-only.md

Anything not evidenced in those documents is explicitly marked NEEDS-EVIDENCE.

---

## 1) High-level architecture

### 1.1 Runtime topology
- React (TypeScript) SPA
  - Auth via Azure Entra ID (OIDC Authorization Code + PKCE)
  - Calls backend REST API with Bearer access token
- Java 21 / Spring Boot 3.x backend
  - OAuth2 Resource Server validating Entra-issued JWTs
  - Business services for scheduling, booking, parts catalog
  - SQLite local file persistence via JDBC + repositories
  - Flyway migrations on startup

### 1.2 Component diagram (frontend + backend modules)

```mermaid
flowchart LR
  subgraph FE[React Frontend (TypeScript)]
    FE_Router[App Router]
    FE_Auth[Auth Module
(msal-browser / OIDC PKCE)]
    FE_Api[API Client Layer
(fetch/axios wrappers)]

    FE_Schedule[Feature: Technician Availability
- Technician selector
- Date range picker
- Schedule view (list/calendar)]

    FE_Booking[Feature: Job Booking
- Booking form
- Validation + conflict UI]

    FE_Parts[Feature: Parts Catalog
- Parts list
- Part details]

    FE_Err[Shared UI
- Loading/empty/error states
- 401 sign-in prompt
- 403 not authorized]

    FE_Router --> FE_Schedule
    FE_Router --> FE_Booking
    FE_Router --> FE_Parts

    FE_Schedule --> FE_Api
    FE_Booking --> FE_Api
    FE_Parts --> FE_Api

    FE_Api --> FE_Auth
    FE_Schedule --> FE_Err
    FE_Booking --> FE_Err
    FE_Parts --> FE_Err
  end

  subgraph BE[Java 21 Backend (Spring Boot)]
    BE_Sec[Security
- JWT validation
- RBAC (roles)]

    BE_Ctrl[REST Controllers
- TechniciansController
- JobsController
- PartsController]

    BE_Svc[Services
- TechnicianScheduleService
- JobBookingService
- PartsCatalogService]

    BE_Repo[Repositories (JDBC)
- TechnicianRepository
- JobRepository
- PartRepository]

    BE_Db[(SQLite local file)]
    BE_Flyway[Flyway migrations]
    BE_Err[Error Handling
- ApiError
- @ControllerAdvice]

    BE_Ctrl --> BE_Svc
    BE_Svc --> BE_Repo
    BE_Repo --> BE_Db
    BE_Flyway --> BE_Db

    BE_Sec --> BE_Ctrl
    BE_Err --> BE_Ctrl
  end

  FE_Api -->|HTTPS /api
Bearer JWT| BE_Ctrl
```

---

## 2) Data flow design (per feature)

### 2.1 Feature 1 — Technician availability scheduling view
1. User signs in (Entra) and obtains access token for backend API scope.
2. UI calls `GET /api/technicians` to populate selector.
3. UI calls `GET /api/technicians/{technicianId}/jobs?from&to`.
4. Backend validates JWT, authorizes request.
5. Backend queries SQLite for jobs in window and returns `JobSummary[]`.

NEEDS-EVIDENCE:
- Default window behavior when `from`/`to` omitted.
- Exact authorization rule for dispatcher vs technician (self-only vs any technician).

### 2.4 Cross-cutting: time handling policy (REQUIRED)

To make Feature 1 AC-7 and Feature 2 boundary AC-8/AC-9 implementable and testable, the system adopts the following time policy for Features 1–3:

- **On the wire:** all datetimes are ISO-8601 with offset, and the backend normalizes to **UTC**.
  - Accepted examples: `2026-10-14T09:30:00Z`, `2026-10-14T09:30:00+00:00`.
- **Precision:** seconds are supported end-to-end.
  - Rationale: Feature 2 AC-9 explicitly references a `44m 59s` boundary.
- **Range semantics:** treat `startTime` inclusive and `endTime` exclusive for overlap checks.
  - This aligns with the overlap predicate already referenced in Feature 2 (existing.start < expandedEnd AND existing.end > expandedStart).

NEEDS-EVIDENCE:
- If the UI must display in a specific local timezone (e.g., technician locale) beyond browser default.

### 2.2 Feature 2 — Job booking
1. Dispatcher signs in and obtains access token.
2. UI submits `POST /api/jobs` with `CreateJobRequest`.
3. Backend validates JWT and dispatcher role.
4. Backend validates request (non-empty customer name, time range).
5. Backend checks technician exists.
6. Backend checks conflicts using 45-minute buffer.
7. Backend inserts job row into SQLite.
8. Backend returns 201 with `Location: /api/jobs/{jobId}` and `Job` body.

NEEDS-EVIDENCE:
- Time precision (minutes vs seconds) and timezone handling.

### 2.3 Feature 3 — Parts catalog
1. User signs in and obtains access token.
2. UI calls `GET /api/parts`.
3. Backend validates JWT and authorizes.
4. Backend queries SQLite for parts where `active=1 AND orderable=1`.
5. Backend returns `PartCatalogItem[]`.

NEEDS-EVIDENCE:
- Whether search/filter is required and how (query params, paging, sorting).
- Whether `quantity_on_hand` is included in catalog response.

---

## 3) API surface summary (Features 1–3)

Base URL: `/api`

### 3.1 Technicians
- `GET /api/technicians`
  - 200: `TechnicianSummary[]`

- `GET /api/technicians/{technicianId}/jobs?from={iso}&to={iso}`
  - 200: `JobSummary[]`
  - 400: `ApiError` (e.g., `from > to`)
  - 404: `ApiError` (technician not found)
  - 401/403: auth errors

Defaulting rules (REQUIRED):
- If `from` and `to` are both omitted: backend defaults to **current week window in UTC**:
  - `from = startOfWeek(UTC, Monday 00:00:00)`
  - `to = from + 7 days`
- If only one of `from`/`to` is provided: return **400** with `ApiError` (avoid ambiguous windows).

### 3.2 Jobs
- `POST /api/jobs`
  - Request: `CreateJobRequest`
  - 201: `Job` + `Location` header
  - 400: `ApiError` (validation)
  - 404: `ApiError` (technician not found)
  - 409: `ApiError` (conflict)
  - 401/403: auth errors

### 3.3 Parts
- `GET /api/parts`
  - 200: `PartCatalogItem[]`
  - 401/403: auth errors

NEEDS-EVIDENCE:
- OpenAPI requirement/format (spec mentions “OpenAPI or equivalent” as NEEDS-EVIDENCE).

---

## 4) Auth approach (Azure Entra ID)

### 4.1 Frontend authentication
- OIDC Authorization Code Flow with PKCE (per ADR-0002).
- Store tokens in memory (preferred) and rely on library-managed refresh.

NEEDS-EVIDENCE:
- Whether silent SSO is required.
- Whether multi-tenant Entra is required.

### 4.2 Backend JWT validation
- Spring Security OAuth2 Resource Server validates:
  - issuer (`iss`) matches Entra tenant
  - audience (`aud`) matches API application ID URI / scope
  - signature via Entra JWKS
  - token expiry

### 4.3 Authorization (RBAC)
- Role-based access control using Entra app roles and/or group claims (per ADR-0002).

Recommended initial role model (names NEEDS-EVIDENCE):
- `Dispatcher`: can create jobs; can view any technician schedule; can view parts catalog.
- `Technician`: can view own schedule; can view parts catalog.

Enforcement points:
- `POST /api/jobs`: Dispatcher only.
- `GET /api/technicians/{id}/jobs`: Dispatcher OR Technician (self-only).
- `GET /api/technicians`: Dispatcher only.
- `GET /api/parts`: Dispatcher OR Technician.

NEEDS-EVIDENCE:
- Exact role names and claim mapping strategy (app roles vs groups).
- Whether technicians can list all technicians (if required, add a separate endpoint like `GET /api/technicians/me` to avoid broad enumeration).

---

## 5) SQLite persistence approach (local-only)

### 5.1 Database
- SQLite local file.
- Flyway migrations manage schema versions (per ADR-0003).

### 5.2 Baseline schema (as referenced by specs)
- `technicians(id, name, active, age, area_served, vehicle_*, skills_csv)`
- `jobs(id, technician_id, customer_name, start_time, end_time)`
- `parts(sku, name, description, active, orderable, quantity_on_hand)`

No new tables required for Features 1–3.

### 5.3 Repository pattern
- Repositories encapsulate SQL and map rows to domain models/DTOs.
- Services implement business rules:
  - booking validation
  - conflict detection with 45-minute buffer
  - parts filtering (active + orderable)

### 5.4 Migrations
- Versioned migrations (e.g., `V1__init.sql`, `V2__...sql`).
- Seed behavior must be explicit.

NEEDS-EVIDENCE:
- Whether seed data is required for demo/training and how it is triggered.
- Concurrency expectations (single-user local vs multi-user).

---

## 6) Error handling conventions

### 6.1 Response shape
Specs reference an `ApiError` shape aligned to current system intent:
- `message: string`
- `status: number`
- `timestamp: string`

REQUIRED (canonical error contract for Features 1–3):

`ApiError`:
- `timestamp: string` (ISO-8601)
- `status: number`
- `message: string`
- `path: string`
- `errorCode: string` (stable, machine-readable)
- `details?: Array<{ field?: string; issue: string }>`

Auth error handling:
- 401/403 responses MUST conform to `ApiError` (wrap Spring Security exceptions via `AuthenticationEntryPoint` and `AccessDeniedHandler`).

NEEDS-EVIDENCE:
- None for Features 1–3. (If future features require additional fields, extend `details` rather than changing top-level fields.)

### 6.2 Status code mapping
- 400: validation errors (e.g., `from > to`, blank customer name, invalid time range)
- 401: unauthenticated (missing/invalid token)
- 403: authenticated but not authorized
- 404: technician not found
- 409: booking conflict

### 6.3 Implementation recommendation
- Centralize exception-to-response mapping via `@ControllerAdvice`.
- Ensure auth errors are consistent with Spring Security defaults or wrapped into `ApiError` (NEEDS-EVIDENCE: whether wrapping is required).

---

## 7) NFR considerations (evidence-limited)

Because the locked NFR baseline is referenced as missing measurable targets in ADR-0001/0002/0003, the following are recommendations and require confirmation.

### 7.1 Security
- Enforce HTTPS in deployment.
- Validate JWT issuer/audience.
- Principle of least privilege via RBAC.
- Avoid storing tokens in localStorage (XSS risk).

NEEDS-EVIDENCE:
- Required audit logging (who booked what job, when).
- Data classification / regulated data flows.

### 7.2 Performance
- Scheduling view queries should be indexed by `technician_id` and time range.

NEEDS-EVIDENCE:
- Expected dataset size (jobs per technician per week/month).
- Paging requirements for parts catalog.

### 7.3 Reliability
- Flyway migration failures should fail fast on startup.
- Graceful error messages for DB locked scenarios (SQLite).

NEEDS-EVIDENCE:
- Backup/restore expectations for local SQLite.

### 7.4 Observability
- Structured logs for request id, user id (subject), and outcome.

NEEDS-EVIDENCE:
- Logging/telemetry standard (OpenTelemetry, log format).

### 7.5 Accessibility & UX
- Empty/loading/error states are explicitly required by specs.

NEEDS-EVIDENCE:
- Accessibility standard (WCAG level) and UI design system.

---

## 8) Risks, unknowns, and NEEDS-EVIDENCE checklist

### 8.1 Auth / RBAC
- NEEDS-EVIDENCE: exact role names and mapping (app roles vs groups).
- NEEDS-EVIDENCE: whether technicians can list all technicians or only self.
- Risk: incorrect claim mapping causes widespread 403s.

### 8.2 Time handling
- NEEDS-EVIDENCE: timezone policy (UTC vs local) and parsing rules.
- NEEDS-EVIDENCE: time precision (minutes vs seconds) impacts boundary AC-8/AC-9.
- Risk: off-by-one-minute/second conflict detection.

### 8.3 API defaults
- NEEDS-EVIDENCE: default `from/to` window for schedule endpoint.
- Risk: UI and backend disagree on default range.

### 8.4 Parts catalog scope
- NEEDS-EVIDENCE: search/filter/paging/sorting requirements.
- NEEDS-EVIDENCE: whether `quantity_on_hand` is exposed.
- Risk: exposing inventory levels unintentionally.

### 8.5 SQLite operational constraints
- NEEDS-EVIDENCE: concurrency model (single-user desktop vs shared file).
- Risk: SQLite locking under concurrent writes (job booking) causing transient failures.

### 8.6 Error contract
- NEEDS-EVIDENCE: canonical `ApiError` schema and whether Spring Security errors must conform.
- Risk: inconsistent error shapes complicate frontend error handling.

---

## 9) Technology stack recommendations (within decided stack)

Backend:
- Java 21, Spring Boot 3.x
- Spring Security OAuth2 Resource Server
- Flyway
- SQLite JDBC

Frontend:
- React + TypeScript
- OIDC client (e.g., MSAL) for Entra
- Central API client with auth header injection

NEEDS-EVIDENCE:
- Preferred React router/state management approach.
- Preferred HTTP client (fetch vs axios).
