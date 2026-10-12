# Test Plan — Feature 4: Parts reservation for a job by the technician

## References
- Spec: [pipeline/features/feature-4-parts-reservation-by-technician.md](../../features/feature-4-parts-reservation-by-technician.md)
- Implementation plan: [pipeline/implementation-plan/feature-4-implementation-plan.md](../../implementation-plan/feature-4-implementation-plan.md)
- Handoff: [pipeline/handoffs/feature-4-handoff.md](../../handoffs/feature-4-handoff.md)

## Scope

### In-scope
- Create a parts reservation tied to a job.
- Validate inventory availability (no negative inventory).
- Quantity validation ($quantity > 0$).
- List reservations for a job.
- Cancel reservation restores inventory; repeated cancel conflicts.
- Concurrency safety: concurrent reservations for same SKU cannot over-allocate.

### Out of scope
As per spec: purchasing/replenishment, partial reservations, notifications, warehouse locations, real persistence.

## Test data strategy
- Prefer deterministic baseline seed.
- Tests must select parts by property (e.g., `stock > 0`) rather than relying on seed ordering.
- For concurrency tests, assert safety properties (never negative; total reserved $\le$ available).

## Traceability (DoD → tests)
| DoD item | Test case IDs |
|---|---|
| Reserve parts decreases inventory | TP-F4-API-001, TP-F4-BE-001 |
| Reject insufficient stock | TP-F4-API-002, TP-F4-BE-002 |
| Cancel restores inventory; repeated cancel conflicts | TP-F4-API-003, TP-F4-BE-003 |
| List reservations by job | TP-F4-API-004, TP-F4-BE-004 |
| Concurrency safety property | TP-F4-BE-005 |

## API test cases (Playwright .NET)

### TP-F4-API-001 — Reserve parts success
- Preconditions: API running; job exists; part SKU exists with stock.
- Steps:
  1. POST reserve parts for job with quantity <= available.
- Expected:
  - 201/200 success
  - reservation returned
  - inventory decremented

### TP-F4-API-002 — Reserve parts insufficient stock
- Preconditions: API running; job exists; part SKU exists.
- Steps:
  1. POST reserve parts with quantity > available.
- Expected:
  - 409 conflict

### TP-F4-API-003 — Cancel reservation restores inventory
- Preconditions: reservation exists.
- Steps:
  1. DELETE/POST cancel reservation.
  2. Cancel again.
- Expected:
  - first cancel succeeds
  - second cancel returns 409

### TP-F4-API-004 — List reservations by job
- Preconditions: at least one reservation exists for job.
- Steps:
  1. GET reservations for job.
- Expected:
  - all returned reservations belong to requested job

## Backend integration/service test cases (JUnit)

### TP-F4-BE-001 — Reserve parts decrements inventory
- Expected: inventory decremented; reservation created.

### TP-F4-BE-002 — Insufficient stock rejected
- Expected: conflict exception.

### TP-F4-BE-003 — Cancel restores inventory; repeated cancel conflicts
- Expected: inventory restored; second cancel conflicts.

### TP-F4-BE-004 — List-by-job safety property
- Expected: all returned reservations belong to job.

### TP-F4-BE-005 — Concurrency safety property
- Expected: total reserved $\le$ available; never negative.

## UI test cases (Playwright .NET)
N/A for Feature 4 unless a parts reservation UI is introduced.

## Execution readiness checklist
- [ ] Backend running on `SERVICEFORGE_API_BASE_URL`
- [ ] Frontend running on `SERVICEFORGE_UI_BASE_URL` (only if UI tests exist)
- [ ] Test automation solution present under `test-automation/`
- [ ] ReportPortal configured (optional)
