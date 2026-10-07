# Feature 4 — Parts reservation for a job by the technician: Implementation Plan

## Executive Summary

Deliver job-scoped parts reservations for technicians with strict inventory enforcement and concurrency safety, using mock/in-memory data only. Provide minimal UI integration on the job screen and a test suite proving correctness (including concurrency).

## Inputs

- Spec: [pipeline/features/feature-4-parts-reservation-by-technician.md](../features/feature-4-parts-reservation-by-technician.md)
- Architecture: [pipeline/architecture/feature-4-architecture.md](../architecture/feature-4-architecture.md)
- Review: [pipeline/reviews/feature-4-review.md](../reviews/feature-4-review.md)
- Related decision: [pipeline/decisions/feature-1-decisions.md](../decisions/feature-1-decisions.md)
- Related rule (informational): [pipeline/rules/parts-count-inventory.md](../rules/parts-count-inventory.md)

## Development Phases

### Phase 1 — Backend foundations (models + mock store)
- Deliverables
  - Add in-memory models for `Part` and `JobPartReservation` (or equivalent DTO/domain split).
  - Extend `MockDataStore` with seeded parts inventory and reservation storage.
  - Deterministic seeding path for tests.

### Phase 2 — Backend services (inventory + reservations)
- Deliverables
  - `PartsCatalogService`: list/search active parts.
  - `JobPartsReservationService`: create/list/cancel.
  - Concurrency control: per-SKU locking around check/decrement and cancel/increment.

### Phase 3 — Backend controllers + error mapping
- Deliverables
  - `PartsCatalogController` and `JobPartsReservationController` endpoints per spec.
  - Validation: quantity > 0; sku pattern/length.
  - Error mapping: 400/404/409 with `ApiError`.

### Phase 4 — Frontend UI integration
- Deliverables
  - `PartsReservationsApiService` (Angular) for inventory + reservations.
  - `JobPartsPanelComponent` to list/create/cancel.
  - `CreatePartReservationDialogComponent` for SKU selection + quantity.

### Phase 5 — Tests + hardening
- Deliverables
  - Backend unit tests for create/list/cancel.
  - Backend concurrency test for over-allocation prevention.
  - Frontend unit tests for service + component wiring (as applicable in repo).

## Epics

### Epic A — Backend: Mock inventory + reservation domain
- Goal: establish in-memory data structures and seed data.
- Dependencies: none.

### Epic B — Backend: Reservation lifecycle
- Goal: implement create/list/cancel with strict inventory enforcement.
- Dependencies: Epic A.

### Epic C — Backend: REST API surface
- Goal: expose endpoints and consistent errors.
- Dependencies: Epic B.

### Epic D — Frontend: Job parts panel
- Goal: technician can manage job reservations from UI.
- Dependencies: Epic C.

### Epic E — Quality: tests and concurrency proof
- Goal: prevent regressions and prove no negative inventory.
- Dependencies: Epics A–D.

## Features (backlog-ready)

1. Inventory catalog endpoint
- Description: list/search parts for dropdown.
- Acceptance Criteria:
  - GET /api/parts returns active parts with available quantity.

2. Create job reservation
- Description: reserve SKU quantity against job.
- Acceptance Criteria:
  - 201 on success; 409 on insufficient stock; 404 on unknown job/sku; 400 on invalid quantity.

3. List job reservations
- Description: show reservations for job.
- Acceptance Criteria:
  - GET /api/jobs/{jobId}/parts-reservations returns only that job’s reservations.

4. Cancel reservation
- Description: cancel and restore inventory.
- Acceptance Criteria:
  - 204 on success; 409 if already cancelled; cannot cancel reservation from another job.

5. UI: Job parts panel
- Description: list/create/cancel from job screen.
- Acceptance Criteria:
  - No direct HTTP in components; all via service.

6. Concurrency test
- Description: prove no over-allocation.
- Acceptance Criteria:
  - Under concurrent create requests, inventory never goes negative and total reserved <= initial.

## Technical Tasks

- Backend
  - Add/extend models + DTOs.
  - Extend `MockDataStore` seed data.
  - Implement services with per-SKU locking.
  - Implement controllers + validation.
  - Add exception mapping to `ApiError`.

- Frontend
  - Add models for parts + reservations.
  - Add Angular service.
  - Add components/dialog and integrate into job view.

- Tests
  - JUnit tests for service logic.
  - Concurrency test using `CountDownLatch`.

## Dependency Matrix

- Epic A -> Epic B -> Epic C -> Epic D
- Epic E depends on A–D

## Risks

- Rule conflict: existing rule file describes partial reservation + replenishment.
  - Mitigation: keep Feature 4 strict (409) and defer partial reservation to a later feature.
- Identity ambiguity for “technician”.
  - Mitigation: derive `reservedByTechnicianId` from job assignment for MVP.

## Suggested Delivery Plan

Sprint 1
- Phase 1–3 (backend end-to-end) + initial tests

Sprint 2
- Phase 4 (frontend) + Phase 5 (concurrency + hardening)
