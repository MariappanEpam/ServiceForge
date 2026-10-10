# Implementation Plan — Feature 8: Job ↔ Parts Mapping & UI Control Conditions

| Field | Value |
|---|---|
| Feature | 8 |
| Spec | pipeline/features/feature-8-job-parts-mapping-and-ui-conditions.md |
| Architecture | pipeline/architecture-design/feature-8-architecture.md |
| Design review | pipeline/design-review-logs/feature-8-2026-10-08.md |
| Depends on | Features 2/4/7 + NAV rules |

## Executive summary

Feature 8 is an alignment feature: it updates/standardizes parts reservation behavior and UI control conditions to match the updated standing rules (job-mandatory mapping, immutability, and conditional UI enablement).

Primary risk is API drift between Feature 2 and Feature 4 reservation endpoints; the plan includes an explicit “choose canonical API” task.

---

## Phase 0 — Discovery (must do first)

1) Identify canonical reservation API
- Inspect current backend controllers/routes for reservations.
- Decide which endpoint set is canonical for UI:
  - Option A: Feature 4 style: `/api/jobs/{jobId}/parts-reservations`
  - Option B: Feature 2 style: `/api/parts/reserve` + `/api/jobs/{jobId}/parts`
- If both exist, keep one as compatibility alias (no breaking changes).

2) Confirm job status model
- Identify existing job statuses and where they are enforced.
- Define allowlist/denylist for reservation in one backend service method.

Deliverable: short decision note in the PR/commit message (no new pipeline decision file required unless repo process mandates it).

---

## Phase 1 — Backend alignment

### Story 8.1 — Enforce job-mandatory reservation creation
Tasks
- Ensure create reservation request requires `jobId` (DTO validation + service validation).
- Validate job existence (404 if unknown).
- Validate job status allows reservation (409 or 400; prefer 409 conflict for “status disallows action”, but keep consistent with existing conventions).

Acceptance criteria
- Missing `jobId` → 400 `ApiError`.
- Unknown `jobId` → 404 `ApiError`.
- Disallowed job status → consistent error code + `ApiError`.

### Story 8.2 — Enforce `jobId` immutability
Tasks
- If a reservation update endpoint exists, reject any attempt to change `jobId` with 409.
- If no update endpoint exists, add a unit test asserting reservation `jobId` cannot change through available operations.

Acceptance criteria
- Any attempt to change `jobId` is impossible or rejected with 409 `ApiError`.

### Story 8.3 — Ensure job-scoped listing
Tasks
- Ensure there is a job-scoped list endpoint and it returns only reservations for that `jobId`.
- Ensure it validates job existence (404 if unknown).

Acceptance criteria
- Listing is job-scoped and consistent with UI needs.

### Story 8.4 — Backend tests
Tasks
- Add tests for:
  - create without `jobId` → 400
  - create with unknown `jobId` → 404
  - create with disallowed job status → expected error
  - list reservations by job returns only that job’s reservations
  - immutability (update attempt or invariant test)

---

## Phase 2 — Frontend alignment

### Story 8.5 — Navigation context preservation
Tasks
- Ensure Site Map navigation to reservation UI preserves `jobId` context:
  - route param preferred (`/jobs/:jobId/...`) or query param.
- If reservation UI is reachable without job context, it must render in a disabled state with guidance.

Acceptance criteria
- `jobId` is preserved when navigating from Job Details.
- No reservation submission is possible without `jobId`.

### Story 8.6 — UI control conditions
Tasks
- Implement a computed UI state model:
  - `hasJobContext`
  - `jobAllowsReservation`
  - `selectedPartAvailableQty`
  - `canReserve`
  - `reserveDisabledReason`
- Bind control enabled/visible states to this model.

Acceptance criteria
- Controls follow the spec’s conditions:
  - no job context → disabled + message
  - disallowed job status → reserve disabled + message
  - out of stock / insufficient stock → reserve disabled + message
  - cancelled reservation → cancel button hidden/disabled

### Story 8.7 — Frontend tests
Tasks
- Unit tests for component state:
  - no job context disables reserve
  - out of stock disables reserve
  - cancelled reservation hides cancel

---

## Dependencies and sequencing

- If Feature 7 introduced SQLite-backed parts, ensure reservation flows still read inventory consistently.
- Implement backend alignment before frontend so UI can rely on stable error semantics.

---

## Deliverables
- Updated backend behavior aligned to rules.
- Updated UI control conditions aligned to rules.
- Automated tests for key scenarios.
