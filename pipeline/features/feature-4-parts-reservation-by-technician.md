# Feature 4 — Parts reservation for a job by the technician

| Field | Value |
|---|---|
| **Status** | Candidate-built / in progress |
| **One-line intent** | Let a technician reserve parts against a specific job while preventing over-allocation of inventory in the in-memory store |
| **Depends on** | Feature 1 decision: standard travel buffer per job is 45 minutes (calendar constraints remain authoritative for job timing) |
| **In scope** | - Technician can create a parts reservation tied to a specific job.
- Reservation validates inventory availability at time of reservation (no negative inventory).
- Reservation supports quantity-tracked parts (MVP); serial-tracked parts are explicitly out of scope unless already present.
- Technician can list reservations for a job.
- Technician can cancel a reservation for a job (restores inventory).
- API returns clear 400/404/409 errors using existing `ApiError` conventions.
- Concurrency safety: concurrent reservations for the same SKU cannot over-allocate.
- UI: add a job-level “Parts” panel to view/create/cancel reservations (via Angular service).
 |
| **Out of scope** | - Purchasing/replenishment workflows (auto-create order requests).
- Partial reservations (auto-reserve available quantity and order the rest).
- Notifications when stock arrives.
- Warehouse locations, bins, transfers.
- Real persistence/database.
- Complex permissions/RBAC beyond demo role header.
 |
| **Definition of Done** | - Backend endpoints exist (mock-backed) to:
  - GET /api/jobs/{jobId}/parts-reservations
  - POST /api/jobs/{jobId}/parts-reservations (sku, quantity)
  - DELETE /api/jobs/{jobId}/parts-reservations/{reservationId}
  - GET /api/parts (list/search for selection)
- Creating a reservation:
  - returns 201 with reservation details when inventory is sufficient.
  - returns 404 when jobId or sku is unknown.
  - returns 400 when quantity <= 0.
  - returns 409 when inventory is insufficient.
- Cancelling a reservation restores inventory and marks reservation cancelled; repeated cancel returns 409.
- Concurrent reservation attempts for the same SKU never result in negative inventory.
- Frontend uses an Angular service for all HTTP calls; components contain no direct HTTP.
- Unit tests cover: create success, insufficient stock, cancel restores stock, list-by-job, and a concurrency test for over-allocation prevention.
 |

## Notes / assumptions

- This feature does not change job booking overlap rules; it only attaches parts reservations to an existing job.
- If Feature 2 artifacts exist (architecture/review/rules) they may be reused, but this feature’s spec is authoritative for scope.

## Artifacts this feature touches

<filled in later, by the developer and tester agents>
