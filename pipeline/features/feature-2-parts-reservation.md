# Feature 2 — Parts Reservation

| Field | Value |
|---|---|
| **Status** | Candidate-built |
| **One-line intent** | Allow technicians to reserve parts from company inventory for jobs, decrement stock when reserved/consumed, and track reservations tied to jobs |
| **Depends on** | Decision: standard travel buffer per job is 45 minutes — see [pipeline/decisions/feature-1-decisions.md](pipeline/decisions/feature-1-decisions.md#L1-L6) |
| **In scope** | - Model an in-memory parts inventory (SKU, optional serials, description, quantity-on-hand, minimum-stock threshold).  
- Allow a technician (or the system on the technician's behalf) to reserve parts for a specific job; reservation reduces available quantity-on-hand but creates a reservation record (jobId, sku, quantity, reservedBy, reservedAt).  
- Support both bulk SKUs (quantity tracked) and optionally serial-tracked items (individual serials selectable) — treat serial tracking as optional behavior governed by a flag on the SKU.  
- Provide REST endpoints (mock-backed) for: list parts, get part by SKU, search by SKU/text, create reservation, list reservations for a job, cancel reservation, and admin restock endpoints.  
- Seed the mock datastore with an example SKU range (e.g., SKU-1000..SKU-1100) and randomized quantities so the UI and tests have realistic data.  
- Prevent reservations that would reduce available quantity below zero and return a clear error (HTTP 409) when attempted.  
- Show reservation history for a job and per-SKU movement summary (simple list). |
| **Out of scope** | - Purchasing workflows, supplier/PO management, receiving goods from vendors, invoices, or ERP integration.  
- Multi-warehouse location management, bin-level inventory optimization, forecasting, or replenishment automation.  
- Physical barcode scanning hardware integration, mobile offline sync, or real-time reconciliation with physical counts.  
- Any persistent database; this remains an in-memory/mock-backed feature unless a later decision records a migration. |
| **Definition of Done** | - REST endpoints exist and are documented in the API README: GET /api/parts, GET /api/parts/{sku}, POST /api/parts/reserve, GET /api/jobs/{jobId}/parts, DELETE /api/parts/reservations/{reservationId}, POST /api/parts/{sku}/restock (admin).  
- MockDataStore seeds inventory (SKU-1000..SKU-1100) with variable quantities and a few serial-tracked SKUs.  
- Reservations decrement available quantity and are visible on GET /api/parts and GET /api/jobs/{jobId}/parts.  
- Attempting to reserve more than available returns HTTP 409 with ApiError body.  
- Frontend component(s) exist to view inventory and make reservations from the technician calendar/job screen (client-side validation prevents obvious conflicts using the same 45-minute buffer decision where relevant).  
- Unit tests cover reservation success, insufficient-stock rejection, reservation cancellation, and inventory listing.  
- Migration-safety note recorded if the in-memory model changes shape (follow pipeline/.claude/migration-safety-skill). |

## User stories

- As a technician, I want to reserve parts from company inventory for a job so I can complete the work without delay.  
- As a dispatcher, I want to view parts reserved for a job so I can ensure the technician has everything needed.  
- As an admin, I want to restock a SKU so quantities reflect recent deliveries.  
- As a reviewer, I want to see reservation history for a job so I can audit parts usage.  

## Acceptance criteria

1. Given an available SKU with sufficient quantity, when a technician reserves N units for job J, then the reservation is created and GET /api/jobs/J/parts shows the reserved items and available quantity decreases by N.  
2. Given an SKU with insufficient quantity, when a reservation for more than available is attempted, then the API returns 409 with ApiError message "Insufficient stock for SKU <sku>" and no reservation is created.  
3. Given a serial-tracked SKU, when a reservation is made, then the API can either reserve by serial list or auto-assign available serials; the reservation records the serials reserved.  
4. Given a cancelled reservation, when DELETE is called, then reserved quantity is returned to available stock and the reservation is marked cancelled in history.  
5. Inventory endpoints return seeded data and are present on application startup.  
6. Frontend prevents obvious reservations that would conflict with the technician's schedule or the travel-buffer decision by warning the user before submission.  

## Edge cases to consider

- Concurrent reservations for the same SKU arriving near-simultaneously (race condition). The mock store should reject the second reservation if quantity would go negative.  
- Reserving zero or negative quantities — validate and reject with 400.  
- Serial-tracked SKUs with only some serials available — ensure selection logic is explicit and testable.  
- Restocking while reservations are being made — restock should increase available quantity immediately in the in-memory store.  
- Reservations for jobs that are later cancelled or moved — ensure reservations can be released or transferred to a different job.  
- Time-zone and timestamp consistency in seeded data and API responses (use ISO-8601 local date-times to match existing code patterns).  

## Dependencies on Feature 1

- Parts reservations are tied to jobs and therefore depend on the job model and APIs from Feature 1 (booking and job records).  
- Business rule: travel buffer (45 minutes) influences how many jobs a technician can complete in a day and therefore affects how many reservations should be allocated per day in reports — see decision [pipeline/decisions/feature-1-decisions.md](pipeline/decisions/feature-1-decisions.md#L1-L6).  

## Success metrics

- Functional: Reservation API returns 201 on success and 409 on insufficient stock; 100% of unit tests for reservations pass.  
- UX: Less than 2% of attempted reservations in manual QA result in unexpected 409 responses once users understand the buffer rule and stock levels.  
- Data: Seeded inventory made up of 101 SKUs (SKU-1000..SKU-1100) with non-zero quantities; during demo scenarios, reservations should succeed for at least 90% of simulated small requests (1–3 units), failing only when purposely exhausting stock.  
- Operability: Admin restock flow can replenish any SKU during a demo within 30 seconds.

## Assumptions & notes (BA interventions)

- Assumption: inventory remains in-memory/mock for training exercises. If a persistent store is later required, a migration-safety decision must be recorded under pipeline/decisions/.  
- Assumption: SKU format is alphanumeric like "SKU-1000" and the example range SKU-1000..SKU-1100 is sufficient for seeding.  
- Intervention note: I explicitly chose to keep serial-tracking optional to avoid over-complicating the initial model; if serial tracking is required for all SKUs, update scope and mock data accordingly.  
- Intervention note: I recommended admin restock endpoints instead of automatic replenishment — purchasing/PO workflows are explicitly out of scope for this feature.  

## Artifacts this feature touches
n/a — filled in by Developer/Testers as they implement the feature
