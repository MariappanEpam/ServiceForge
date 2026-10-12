# Feature 2 — Parts Reservation

| Field | Value |
|---|---|
| **Status** | Candidate-built / in progress |
| **One-line intent** | Enable viewing parts inventory and creating/cancelling job-linked parts reservations while preventing over-allocation in the in-memory store |
| **Depends on** | Feature 1 decision: standard travel buffer per job is 45 minutes (calendar constraints remain authoritative for job timing) |
| **In scope** | - Parts inventory browsing and SKU detail retrieval.
- Create a parts reservation against a job with transactional check-and-decrement semantics per SKU.
- List parts reservations for a job.
- Cancel a parts reservation (restores inventory and marks reservation cancelled).
- Restock a SKU (admin/demo operation) to increase on-hand quantity (and serials when applicable).
- Concurrency safety: concurrent reservations for the same SKU cannot over-allocate.
- Error handling uses the existing backend `ApiError` response shape and HTTP status conventions.
- Frontend UI components and Angular service client as named in the architecture doc.
- In-memory only: extend `MockDataStore` with parts inventory + reservations collections (no SQLite persistence for this feature unless explicitly enabled by a later feature).
 |
| **Out of scope** | - Any SQLite persistence work (repositories, migrations, seed modes) unless a later feature explicitly switches persistence.
- Purchasing/replenishment workflows beyond the explicit `restock` endpoint.
- Warehouse locations/bins/transfers.
- Notifications, SLAs, or workflow automation.
- RBAC/permissions beyond demo-level assumptions.
- Any change to job booking overlap rules (Feature 1 behavior remains as-is).
 |
| **Definition of Done** | - Backend exposes the REST endpoints listed below and they operate against the in-memory `MockDataStore`.
- Reservation creation is atomic per SKU and never results in negative inventory under concurrent requests.
- Validation and error responses:
  - 400 for invalid request payloads (e.g., `quantity <= 0`, serial list invalid for SKU).
  - 404 for unknown `jobId`, unknown `sku`, or unknown `reservationId`.
  - 409 for insufficient stock, cancelling an already-cancelled reservation, or other reservation state conflicts.
  - Errors return an `ApiError` body.
- Frontend provides inventory browsing, SKU detail, and job-level reservation management using the components listed below.
- Frontend performs all HTTP calls via a dedicated Angular service client (no direct HTTP in components).
- Basic unit tests exist for `ReservationService` covering: create success, insufficient stock (409), cancel restores stock, list-by-job, and a concurrency test for over-allocation prevention.
 |

## API surface (backend)

Endpoints (as defined by the architecture doc):

- `GET /api/parts`
  - Purpose: list/search parts inventory.
  - Success: 200 with a list of `PartDto`.

- `GET /api/parts/{sku}`
  - Purpose: retrieve SKU details.
  - Success: 200 with `PartDetailDto`.
  - Errors: 404 with `ApiError` if SKU not found.

- `POST /api/parts/reserve`
  - Purpose: create a reservation.
  - Request: `ReservationCreateRequest` (`jobId`, `sku`, `quantity`, optional `serials[]`).
  - Success: 201 with `ReservationDto`.
  - Errors:
    - 400 with `ApiError` for invalid payload (quantity, serial rules).
    - 404 with `ApiError` if `jobId` or `sku` not found.
    - 409 with `ApiError` if insufficient stock or serials unavailable.

- `GET /api/jobs/{jobId}/parts`
  - Purpose: list reservations for a job.
  - Success: 200 with list of `ReservationDto`.
  - Errors: 404 with `ApiError` if job not found.

- `DELETE /api/parts/reservations/{reservationId}`
  - Purpose: cancel a reservation.
  - Success: 204 (no body).
  - Errors:
    - 404 with `ApiError` if reservation not found.
    - 409 with `ApiError` if reservation already cancelled (or otherwise not cancellable).

- `POST /api/parts/{sku}/restock`
  - Purpose: restock inventory for a SKU (admin/demo operation).
  - Request: `RestockRequest` (`quantity`). (If serials are supported for a SKU, restock may include serials per architecture intent; otherwise quantity-only.)
  - Success: 200 with updated `PartDetailDto` (or equivalent updated part view).
  - Errors:
    - 400 with `ApiError` for invalid quantity.
    - 404 with `ApiError` if SKU not found.

## UI surface (frontend)

Components (as named in the architecture doc):

- `PartsInventoryComponent`
  - Shows parts inventory list and supports search.
  - Navigates to / opens SKU detail view.

- `PartReservationDialog`
  - Allows selecting quantity (and serials when applicable) and submitting a reservation.
  - Displays validation and conflict errors from the API.

- `JobPartsPanel`
  - Embedded in the job screen.
  - Lists reservations for the job.
  - Allows creating a reservation (opens `PartReservationDialog`).
  - Allows cancelling a reservation.

- `PartsServiceClient` (Angular service)
  - Wraps all HTTP calls to the endpoints above.
  - Returns typed models (`part.model.ts`, `reservation.model.ts`).

## Error handling (contract)

- Backend returns `ResponseEntity<T>` for success and uses `ApiError` for error bodies.
- Status codes are limited to: 400 (validation), 404 (not found), 409 (conflict/insufficient stock).
- Frontend displays:
  - Field-level validation errors for 400 where possible.
  - Not-found messaging for 404.
  - Conflict messaging for 409 (insufficient stock / already cancelled).

## Data & persistence constraints

- This feature is **in-memory** only.
- Inventory and reservations live in `MockDataStore` collections as described in the architecture doc (e.g., `partsBySku`, `reservationsById`, `PartRecord` with `quantityOnHand` and optional serial tracking).
- No SQLite migrations, repositories, or seed-mode configuration are introduced as part of Feature 2.

## Artifacts this feature touches

<filled in as the developer and tester agents do their work>
