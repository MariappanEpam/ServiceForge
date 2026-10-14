# Implementation Plan — ServiceForge-Migrated target stack (Features 1–3)

Date: 2026-10-14  
Scope (strict):
- ServiceForge-Migrated/pipeline/architecture-design/architecture-migrated-target-stack.md
- ServiceForge-Migrated/pipeline/features/feature-1-technician-availability-scheduling-view.md
- ServiceForge-Migrated/pipeline/features/feature-2-job-booking.md
- ServiceForge-Migrated/pipeline/features/feature-3-parts-catalog.md
- ADRs under ServiceForge-Migrated/pipeline/decisions/

Target stack (per ADRs): Java 21 + Spring Boot 3.x, React + TypeScript, Azure Entra ID (OIDC + JWT resource server), SQLite local-only + Flyway.

---

## 1) Executive summary

### Scope
Deliver a working migrated baseline for Features 1–3 on the target stack:
- Feature 1: Technician availability scheduling view (list technicians; view technician jobs in a date window)
- Feature 2: Job booking (dispatcher creates job; 45-minute buffer conflict detection)
- Feature 3: Parts catalog (list active + orderable parts)

### Assumptions (NEEDS-EVIDENCE where not explicitly specified)
- Entra tenant/app registrations exist (SPA + API) and required values will be provided to the team (tenant id, client id, scopes, issuer, audience).
- Role model exists in Entra (Dispatcher/Technician or equivalent) and claim mapping is agreed.
- SQLite schema baseline is aligned to the evidenced tables/columns referenced by the specs; no new tables are required for Features 1–3.

### Constraints
- SQLite must be local-only (no external DB services).
- Auth must be Azure Entra ID.
- Time handling must support seconds precision end-to-end (Feature 2 AC-9 boundary).

---

## 2) Delivery roadmap (phased)

### Phase 0 — Foundations (MVP-enabling)
- Repo structure for migrated backend/frontend
- Entra auth wiring (frontend sign-in + backend JWT validation)
- SQLite + Flyway baseline migrations + seed data
- Shared error contract (`ApiError`) and consistent HTTP status mapping

### Phase 1 — Feature 1 (Technician availability scheduling view)
- Backend: `GET /api/technicians`, `GET /api/technicians/{id}/jobs?from&to` with default window behavior
- Frontend: Technician Availability screen (selector + date range + schedule list/calendar + states)

### Phase 2 — Feature 2 (Job booking)
- Backend: `POST /api/jobs` with validation + 45-minute buffer conflict detection
- Frontend: Book Job screen (form + validation + conflict UI + success navigation)

### Phase 3 — Feature 3 (Parts catalog)
- Backend: `GET /api/parts` filtering active+orderable
- Frontend: Parts Catalog screen (list + details view + states)

### Phase 4 — Hardening & release readiness
- Integration tests (auth + DB + endpoints)
- UI tests (auth-aware flows)
- Documentation (runbook, env config, API docs if required)

---

## 3) Epic breakdown (Epics → Stories → Tasks)

### EPIC A — Platform foundations (target stack baseline)

#### Story A1 — Backend skeleton (Java 21 + Spring Boot 3)
**Outcome:** Backend service starts, exposes `/api` endpoints, uses consistent error handling.

Tasks:
1. Create Spring Boot project configured for Java 21.
2. Add dependencies:
   - Spring Web
   - Spring Validation
   - Spring Security + OAuth2 Resource Server
   - SQLite JDBC driver
   - Flyway
   - Test: JUnit 5, Spring Boot Test
3. Implement global error handling:
   - `ApiError` DTO (fields per spec; if exact shape not evidenced, keep minimal: `message`, `status`, `timestamp`)
   - `@ControllerAdvice` mapping for 400/404/409 and auth errors 401/403
4. Implement time parsing/normalization policy (per architecture):
   - Accept ISO-8601 with offset
   - Normalize to UTC
   - Preserve seconds precision
   - Overlap semantics: start inclusive, end exclusive

NEEDS-EVIDENCE:
- Final `ApiError` field set (spec says “aligned to current system intent; exact fields NEEDS-EVIDENCE”).

#### Story A2 — Persistence baseline (SQLite local-only + Flyway)
**Outcome:** Local SQLite file is created/managed by Flyway; schema supports technicians/jobs/parts.

Tasks:
1. Configure datasource for local SQLite file.
2. Configure Flyway to run on startup.
3. Create baseline migrations for:
   - `technicians(id, name, active, age, area_served, vehicle_*, skills_csv)`
   - `jobs(id, technician_id, customer_name, start_time, end_time)`
   - `parts(sku, name, description, active, orderable, quantity_on_hand)`
4. Add seed data migration (or explicit seed runner) for:
   - at least 2 technicians
   - at least 2 jobs spanning a week window
   - at least 5 parts with mixed active/orderable flags
5. Implement repositories (JDBC) for technicians/jobs/parts.

NEEDS-EVIDENCE:
- Exact column types and constraints (beyond the column names listed in architecture/specs).

#### Story A3 — Authentication & authorization (Azure Entra)
**Outcome:** Frontend can sign in; backend validates JWT; endpoints enforce RBAC.

Backend tasks:
1. Configure Spring Security OAuth2 Resource Server:
   - issuer (`iss`) validation
   - audience (`aud`) validation
   - JWKS retrieval
2. Implement claim-to-authority mapping for roles.
3. Enforce endpoint authorization (per architecture recommendations):
   - `POST /api/jobs`: Dispatcher only
   - `GET /api/technicians/{id}/jobs`: Dispatcher OR Technician (self-only)
   - `GET /api/technicians`: Dispatcher only
   - `GET /api/parts`: Dispatcher OR Technician

Frontend tasks:
1. Add Entra auth library (e.g., `@azure/msal-browser` + `@azure/msal-react`).
2. Implement sign-in/sign-out.
3. Acquire access token for API scope and attach Bearer token to API calls.
4. Handle 401/403 globally (route to sign-in / show not authorized).

NEEDS-EVIDENCE:
- Exact role names and whether roles come from app roles vs groups.
- Whether multi-tenant is required.
- Whether silent SSO is required.

---

### EPIC B — Feature 1: Technician availability scheduling view

#### Story B1 — Backend: list technicians
**Outcome:** `GET /api/technicians` returns `TechnicianSummary[]`.

Tasks:
1. Implement `TechniciansController.getTechnicians()`.
2. Implement `TechnicianRepository.listTechnicians()`.
3. Add authorization rule (Dispatcher only unless evidence changes).
4. Add tests:
   - 200 returns list
   - 401 unauthenticated
   - 403 unauthorized role

#### Story B2 — Backend: list technician jobs in window
**Outcome:** `GET /api/technicians/{technicianId}/jobs?from&to` returns `JobSummary[]`.

Tasks:
1. Implement query param parsing and validation:
   - If both omitted: default to current week window in UTC (architecture-defined)
   - If only one provided: 400
   - If `from > to`: 400
2. Implement technician existence check:
   - If technician not found: 404
3. Implement repository query for jobs in window.
4. Add authorization:
   - Dispatcher can view any technician
   - Technician can view self-only (requires identity-to-technician mapping)
5. Add tests:
   - default window behavior
   - from/to validation
   - 404 technician
   - 401/403

NEEDS-EVIDENCE:
- Identity-to-technician mapping strategy (how a Technician user maps to `technicianId`).

#### Story B3 — Frontend: Technician Availability screen
**Outcome:** React screen renders schedule for selected technician and date range.

Tasks:
1. Create route/page: “Technician Availability”.
2. Build UI components:
   - technician dropdown (populated from `GET /api/technicians`)
   - date range picker (default current week)
   - schedule rendering (list and/or calendar)
3. Implement API client methods:
   - `listTechnicians()`
   - `getTechnicianJobs(technicianId, from?, to?)`
4. Implement UI states:
   - loading
   - empty state (no jobs)
   - error states: 401 sign-in prompt, 403 not authorized, 404 technician not found
5. Add UI tests:
   - renders list
   - empty state
   - error handling

NEEDS-EVIDENCE:
- Whether calendar view is required vs list-only.

---

### EPIC C — Feature 2: Job booking

#### Story C1 — Backend: create job endpoint
**Outcome:** `POST /api/jobs` creates a job with validation and conflict detection.

Tasks:
1. Implement `JobsController.createJob()` accepting `CreateJobRequest`.
2. Validate request:
   - technician exists → else 404
   - `customerName` non-empty → 400
   - `endTime` after `startTime` → 400
3. Implement conflict detection with 45-minute buffer:
   - expanded window: `[startTime - 45m, endTime + 45m]`
   - overlap predicate: `existing.start < expandedEnd` AND `existing.end > expandedStart`
   - range semantics: start inclusive, end exclusive
4. Persist job to SQLite.
5. Return 201 + `Location: /api/jobs/{jobId}` + `Job` body.
6. Authorization: Dispatcher only.
7. Add tests:
   - 201 happy path
   - 404 technician
   - 400 blank customer
   - 400 invalid time range
   - 409 conflict
   - boundary AC-8 (exactly `existing.end + 45m` succeeds)
   - boundary AC-9 (`existing.end + 44m 59s` fails)

NEEDS-EVIDENCE:
- Confirm seconds precision is required/accepted by UI widgets and DB storage.

#### Story C2 — Frontend: Book Job screen
**Outcome:** Dispatcher can create a job and see validation/conflict feedback.

Tasks:
1. Create route/page: “Book Job”.
2. Build form:
   - technician selector
   - customer name input
   - start/end datetime pickers (must support seconds if AC-9 is tested at UI level)
3. Implement submit calling `POST /api/jobs`.
4. Handle responses:
   - 201: success toast + navigate to Technician Availability (selected technician)
   - 400: field-level validation display
   - 409: conflict message
   - 401/403: auth handling
5. Add UI tests:
   - successful booking
   - validation errors
   - conflict display

NEEDS-EVIDENCE:
- Navigation target after success (spec suggests schedule; confirm exact route/behavior).

---

### EPIC D — Feature 3: Parts catalog

#### Story D1 — Backend: list parts endpoint
**Outcome:** `GET /api/parts` returns active+orderable parts.

Tasks:
1. Implement `PartsController.listParts()`.
2. Implement repository query filtering `active=1 AND orderable=1`.
3. Map to `PartCatalogItem` (sku, name, description?).
4. Authorization: Dispatcher OR Technician.
5. Add tests:
   - excludes inactive
   - excludes non-orderable
   - 401/403

NEEDS-EVIDENCE:
- Whether `quantity_on_hand` must be included in response.
- Whether search/filter/paging/sorting is required.

#### Story D2 — Frontend: Parts Catalog screen
**Outcome:** User can browse parts list and view details.

Tasks:
1. Create route/page: “Parts Catalog”.
2. Implement list view:
   - fetch `GET /api/parts`
   - render SKU, name, description
   - loading/empty/error states
3. Implement details view (modal or route) showing part fields.
4. Add UI tests:
   - list renders
   - empty state
   - auth error handling

NEEDS-EVIDENCE:
- Whether search UI is required.

---

### EPIC E — Testing, quality gates, and evidence capture

#### Story E1 — Backend automated tests (unit + integration)
Tasks:
1. Unit tests for services:
   - time window defaulting
   - conflict detection boundaries
2. Controller tests (MockMvc) for:
   - status codes and `ApiError` body
   - auth enforcement (401/403)
3. Integration tests with SQLite + Flyway:
   - migrations apply
   - seed data present
   - end-to-end endpoint calls (with mocked JWT)

NEEDS-EVIDENCE:
- Whether contract tests / OpenAPI validation is required.

#### Story E2 — Frontend automated tests (unit + UI)
Tasks:
1. Unit tests for API client and auth wrapper.
2. Component tests for screens (loading/empty/error).
3. UI/E2E tests (Playwright/Cypress):
   - sign-in gate behavior (can be mocked)
   - Feature 1 schedule view
   - Feature 2 booking flow
   - Feature 3 parts list

NEEDS-EVIDENCE:
- Preferred UI test framework and CI expectations.

#### Story E3 — Evidence & documentation
Tasks:
1. Document required Entra configuration values and local `.env`/config keys.
2. Document how to run backend/frontend locally.
3. Capture NEEDS-EVIDENCE resolutions as ADR amendments or feature spec updates (outside this plan’s scope).

---

## 4) Cross-cutting technical decisions to implement (from architecture)

1. **Time policy** (required by architecture):
   - ISO-8601 with offset on the wire
   - normalize to UTC
   - seconds precision
   - overlap checks: start inclusive, end exclusive
2. **Default window behavior** for Feature 1 (required by architecture):
   - if `from` and `to` omitted: current week window in UTC (Monday 00:00:00 → +7 days)
   - if only one provided: 400
3. **RBAC enforcement** (recommended by architecture; role names NEEDS-EVIDENCE).

---

## 5) Dependencies

| Dependency | Impact | Resolution |
|---|---|---|
| Entra app registrations (SPA + API) | Blocks auth integration and end-to-end testing | Provide tenant/client IDs, issuer, audience, scopes; define roles/claims |
| Identity-to-technician mapping | Blocks “Technician can view own schedule” enforcement | Decide mapping (claim contains technicianId vs lookup table) |
| SQLite schema details (types/constraints) | Impacts migrations and repository SQL | Confirm baseline schema from existing evidence; encode in Flyway |
| UI time picker seconds support | Impacts AC-9 UI-level boundary testing | Decide whether UI tests validate seconds boundary or backend-only |

---

## 6) Risks and NEEDS-EVIDENCE register

### High risk
1. **Auth/RBAC ambiguity** (roles + claim mapping not evidenced)
   - Mitigation: define minimal roles and mapping early; add a small spike to validate JWT claims from Entra.
2. **Time precision and timezone handling** (Feature 2 AC-9)
   - Mitigation: enforce backend seconds precision; store timestamps with sufficient precision; add explicit tests.
3. **Technician self-only authorization** requires identity mapping
   - Mitigation: choose a deterministic mapping strategy and document it.

### Medium risk
4. **API error contract shape** not fully specified
   - Mitigation: keep minimal stable `ApiError` and version if expanded later.
5. **Parts catalog search/paging** not specified
   - Mitigation: ship list-only MVP; add query params only with evidence.

### NEEDS-EVIDENCE items (must be resolved before “release-ready”)
- Exact role names and whether roles are app roles or groups.
- Whether multi-tenant Entra is required.
- Whether silent SSO is required.
- Final `ApiError` schema.
- Calendar vs list requirement for Feature 1.
- Parts catalog: search/filter/paging/sorting; whether `quantity_on_hand` is included.

---

## 7) Definition of Done (DoD) checklist

### Functional
- [ ] Feature 1: Technician Availability screen works end-to-end (list technicians, select technician, view jobs, default week window, empty state).
- [ ] Feature 2: Job booking works end-to-end (validation, 45-minute buffer conflict detection, 201 + Location).
- [ ] Feature 3: Parts catalog works end-to-end (active+orderable filtering, empty state).

### Security/Auth
- [ ] Frontend uses Entra OIDC Authorization Code + PKCE.
- [ ] Backend validates Entra JWTs as a resource server.
- [ ] RBAC enforced per endpoint (401/403 behavior verified).

### Persistence
- [ ] SQLite local-only configured.
- [ ] Flyway migrations create required tables.
- [ ] Seed data exists for local dev and tests.

### Quality
- [ ] Backend unit tests cover validation and conflict boundaries (including AC-8/AC-9).
- [ ] Backend integration tests run against SQLite + Flyway.
- [ ] Frontend unit/component tests cover loading/empty/error states.
- [ ] UI/E2E tests cover primary flows (auth can be mocked).

### Operability
- [ ] Local run instructions documented (backend + frontend).
- [ ] Configuration keys documented (Entra + DB path).

---

## 8) Work package mapping (for tracker import)

- EPIC A: Platform foundations
  - Story A1: Backend skeleton
  - Story A2: Persistence baseline
  - Story A3: Auth & authorization
- EPIC B: Feature 1 — Technician availability
  - Story B1: List technicians
  - Story B2: List technician jobs
  - Story B3: Availability screen
- EPIC C: Feature 2 — Job booking
  - Story C1: Create job endpoint
  - Story C2: Booking screen
- EPIC D: Feature 3 — Parts catalog
  - Story D1: List parts endpoint
  - Story D2: Parts catalog screen
- EPIC E: Testing & evidence
  - Story E1: Backend tests
  - Story E2: Frontend tests
  - Story E3: Evidence & docs
