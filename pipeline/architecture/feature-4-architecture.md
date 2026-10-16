Feature 4 — Parts reservation for a job by the technician

Architecture Design

Source spec: [pipeline/features/feature-4-parts-reservation-by-technician.md](../features/feature-4-parts-reservation-by-technician.md)
Related decisions:
- [pipeline/decisions/feature-1-decisions.md](../decisions/feature-1-decisions.md) — travel buffer policy (job timing remains authoritative)
Related rules (existing):
- [pipeline/rules/parts-count-inventory.md](../rules/parts-count-inventory.md)

## 1) High-level component diagram (textual)

- Frontend (Angular)
  - `JobPartsPanelComponent` (embedded in job detail view)
    - lists reservations for the job
    - opens create reservation dialog
    - cancels reservation
  - `CreatePartReservationDialogComponent`
    - selects SKU from inventory list
    - enters quantity
  - `PartsReservationsApiService`
    - wraps all HTTP calls for inventory + reservations

- Backend (Spring Boot)
  - Controller layer
    - `JobPartsReservationController`
      - GET /api/jobs/{jobId}/parts-reservations
      - POST /api/jobs/{jobId}/parts-reservations
      - DELETE /api/jobs/{jobId}/parts-reservations/{reservationId}
    - `PartsCatalogController`
      - GET /api/parts (for dropdown/search)
  - Service layer
    - `PartsCatalogService` (read-only list/search)
    - `JobPartsReservationService` (create/list/cancel reservations)
  - Data layer (mock/in-memory)
    - `MockDataStore` extended with:
      - parts inventory map keyed by SKU
      - reservations map keyed by reservationId
      - index from jobId -> reservationIds (or filter by jobId)

## 2) Domain model (in-memory)

MVP models (quantity-tracked only):

- `Part`
  - `sku` (string, unique)
  - `name`
  - `description` (optional)
  - `quantityOnHand` (int)
  - `active` (boolean)

- `JobPartReservation`
  - `id`
  - `jobId`
  - `sku`
  - `quantityReserved`
  - `reservedByTechnicianId` (string)
  - `reservedAt` (ISO-8601)
  - `status` (ACTIVE | CANCELLED)

Notes:
- `reservedByTechnicianId` is derived from the job’s assigned technician (or request header if the system already models “current technician”). For MVP, derive from job.

## 3) API contracts (REST)

### List inventory
- GET /api/parts
  - Query params (optional): `q` (search), `activeOnly=true`
  - 200: list of `PartSummaryDto` (sku, name, availableQty)

### List reservations for a job
- GET /api/jobs/{jobId}/parts-reservations
  - 200: list of `JobPartReservationDto`
  - 404: job not found

### Create reservation for a job
- POST /api/jobs/{jobId}/parts-reservations
  - Body: `{ "sku": "SKU-1001", "quantity": 2 }`
  - 201: created `JobPartReservationDto`
  - 400: invalid quantity / invalid sku format
  - 404: job not found OR sku not found
  - 409: insufficient stock

### Cancel reservation
- DELETE /api/jobs/{jobId}/parts-reservations/{reservationId}
  - 204: cancelled
  - 404: job not found OR reservation not found (or reservation not under job)
  - 409: already cancelled

Error shape:
- Use existing `ApiError` conventions (400/404/409) and avoid leaking stack traces.

## 4) Concurrency / atomicity approach (in-memory)

Goal: prevent over-allocation under concurrent requests.

- Inventory is stored per SKU in a `PartRecord` (or `Part` with a mutable quantity field).
- For each SKU, synchronize mutations on the per-SKU record object:
  - create reservation: check `quantityOnHand >= requested`, then decrement, then persist reservation.
  - cancel reservation: mark cancelled, then increment quantityOnHand.

Lock granularity:
- Per-SKU lock only (avoid global lock).

## 5) Non-functional requirements (NFRs)

- Thread-safety: per-SKU locking for mutations.
- Testability: services accept `MockDataStore` via constructor injection; store can be reset/seeded deterministically for tests.
- Performance: demo-scale; avoid O(N) scans where possible (optional jobId index).
- Observability: log reservation create/cancel with jobId, sku, qty, reservationId.

## 6) Migration-safety notes

This feature likely introduces new models (`Part`, `JobPartReservation`) and extends `MockDataStore`.
Follow additive-first and seed-all-records guidance.

## 7) Risks and mitigations

- Risk: existing pipeline rule suggests partial reservations + replenishment; this feature’s spec explicitly excludes it.
  - Mitigation: keep behavior strict (409 on insufficient stock) and record as out-of-scope.
- Risk: ambiguity of “current technician”.
  - Mitigation: derive `reservedByTechnicianId` from job assignment for MVP.

Handoff update (for Design Review Agent)
- Status: Architecture Complete
- Owner: Design Review Agent
- Artifacts:
  - [pipeline/features/feature-4-parts-reservation-by-technician.md](../features/feature-4-parts-reservation-by-technician.md)
  - [pipeline/architecture/feature-4-architecture.md](feature-4-architecture.md)
