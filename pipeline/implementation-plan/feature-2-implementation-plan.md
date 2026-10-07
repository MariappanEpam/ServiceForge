# Implementation Plan — Feature 2: Parts Reservation

Links (source of truth)
- Spec: pipeline/features/feature-2-parts-reservation.md
- Architecture: pipeline/architecture/feature-2-architecture.md
- Rules: pipeline/rules/parts-count-inventory.md, pipeline/rules/local-sqlite-only.md
- Prior decision dependency: pipeline/decisions/feature-1-decisions.md

## Executive Summary

### Scope (MVP)
Deliver in-memory parts inventory browsing and job-linked parts reservations with concurrency-safe, per-SKU atomic check-and-decrement semantics.

Backend (Spring Boot)
- Inventory browse + SKU detail.
- Create reservation (201) with validation and conflict handling.
- List reservations by job.
- Cancel reservation (204) restoring inventory.
- Restock SKU (demo/admin) increasing on-hand (and serials when applicable).

Frontend (Angular)
- Inventory list/search and SKU detail.
- Job screen panel to list/create/cancel reservations.
- Reservation dialog for quantity (and serials when applicable).
- All HTTP via a dedicated Angular service client.

Testing
- Backend unit tests for `ReservationService` including concurrency.
- Test automation tasks for Playwright C# under sibling test-automation/ (not under pipeline/).

### Assumptions
- Feature 2 remains in-memory only (no SQLite persistence introduced).
- Reservation behavior follows the feature spec’s DoD: insufficient stock returns 409 (no partial reservation behavior in MVP).
- Serial-tracked SKUs exist but can be limited to a small seeded subset for demo/testing.

### Constraints
- No external DB/services; local-only SQLite is allowed by repo rule but explicitly out of scope for Feature 2.
- Must use existing backend `ApiError` response shape and HTTP status conventions.
- Concurrency safety must prevent negative inventory under concurrent requests.

---

## Delivery Roadmap

### Phase 1 — Backend foundation (data model + services)
1. Extend `MockDataStore` with parts inventory and reservations collections.
2. Implement `PartRecord` and reservation domain model/state.
3. Implement `PartsInventoryService` and `ReservationService` with per-SKU synchronization.
4. Add deterministic seed data for SKUs and (optionally) serial-tracked SKUs.

### Phase 2 — Backend API surface (controllers + DTOs + errors)
1. Add DTOs/requests.
2. Implement `PartsController` endpoints.
3. Ensure validation + error mapping to `ApiError` with 400/404/409.

### Phase 3 — Frontend UI + service client
1. Add typed models.
2. Implement `PartsServiceClient`.
3. Implement components: inventory list, reservation dialog, job parts panel.
4. Wire into routing / job screen integration.

### Phase 4 — Tests + automation readiness
1. Backend unit tests (including concurrency test).
2. Add/confirm UI selectors (`data-testid`) needed for Playwright.
3. Create Playwright C# API/UI tests under test-automation/.

---

## Epic Breakdown

### EPIC: Feature 2 — Parts Reservation

#### Feature: In-memory parts inventory + reservation lifecycle

##### Story 2.1 — Backend: In-memory data model + services
- Task 2.1.1: Add parts inventory collections to `MockDataStore`
  - Add `partsBySku` (e.g., `ConcurrentHashMap<String, PartRecord>`).
  - Add `reservationsById` (e.g., `ConcurrentHashMap<String, Reservation>`).
  - Add helper accessors (get-by-sku, get-by-id) that return optional/throw not-found.
  - Add deterministic seeding method(s) for:
    - Non-serial SKUs with stable SKUs (e.g., SKU-1000..SKU-1100).
    - A small subset of serial-tracked SKUs with stable serial lists.

- Task 2.1.2: Implement `PartRecord` (in-memory representation)
  - Fields per architecture: `AtomicInteger quantityOnHand`.
  - For serial-tracked SKUs: `ConcurrentLinkedDeque<String> availableSerials`.
  - Include metadata needed for DTO mapping (description, serialTracked flag, thresholds).

- Task 2.1.3: Implement backend models for parts + reservations
  - Add `Part` and `Reservation` models under the package-by-layer layout.
  - Reservation fields per architecture: `id`, `jobId`, `sku`, `quantity`, optional `serials`, `reservedBy`, `reservedAt`, `status`.
  - Define `ReservationStatus` enum (e.g., `ACTIVE`, `CANCELLED`).

- Task 2.1.4: Implement `PartsInventoryService`
  - `listParts(search?)` returning list view.
  - `getPartDetail(sku)` returning detail view.
  - Ensure SKU not found -> not-found exception mapped to 404.

- Task 2.1.5: Implement `ReservationService` with concurrency safety
  - `createReservation(req)`
    - Validate: `quantity > 0`, job exists, SKU exists.
    - Serial rules:
      - If `serialTracked=false`, reject non-empty serial list (400).
      - If `serialTracked=true`, validate serial list size matches quantity (or implement auto-assign if architecture allows; keep deterministic).
    - Synchronize per SKU: `synchronized(partRecord)`.
    - Check-and-decrement semantics:
      - If insufficient stock/serials -> throw conflict exception -> 409.
      - Never allow negative inventory.
    - Create reservation record and store in `reservationsById`.

  - `listByJob(jobId)`
    - Validate job exists (404).
    - Return reservations filtered by job.

  - `cancelReservation(reservationId)`
    - 404 if not found.
    - 409 if already cancelled.
    - Synchronize on associated `PartRecord` and restore quantity/serials.
    - Mark reservation cancelled.

  - `restock(sku, req)`
    - Validate `quantity > 0` (400).
    - 404 if SKU not found.
    - Synchronize on `PartRecord` and increment quantity; add serials if supported.

  - Logging: log SKU + reservationId + jobId for create/cancel/restock.

##### Story 2.2 — Backend: REST API + DTOs
- Task 2.2.1: Add DTOs and request models
  - `PartDto`, `PartDetailDto`.
  - `ReservationDto`.
  - `ReservationCreateRequest`.
  - `RestockRequest`.

- Task 2.2.2: Implement `PartsController` endpoints
  - `GET /api/parts` -> 200 list.
  - `GET /api/parts/{sku}` -> 200 detail, 404 if missing.
  - `POST /api/parts/reserve` -> 201 created, 400/404/409 errors.
  - `GET /api/jobs/{jobId}/parts` -> 200 list, 404 if job missing.
  - `DELETE /api/parts/reservations/{reservationId}` -> 204, 404/409 errors.
  - `POST /api/parts/{sku}/restock` -> 200 updated detail, 400/404 errors.

- Task 2.2.3: Validation + error mapping
  - Use existing `ApiError` shape.
  - Ensure status codes:
    - 400 for invalid payloads.
    - 404 for unknown `jobId`/`sku`/`reservationId`.
    - 409 for insufficient stock, serials unavailable, already-cancelled.
  - Add/extend exception handler(s) if needed to keep consistent error responses.

- Task 2.2.4: Contract sanity checks
  - Confirm response DTOs are stable and match frontend needs.
  - Confirm `201` includes created reservation body.

##### Story 2.3 — Frontend: Service client + UI components
- Task 2.3.1: Add Angular models
  - `part.model.ts` and `reservation.model.ts` aligned to DTOs.

- Task 2.3.2: Implement `PartsServiceClient` (Angular service)
  - Methods:
    - `listParts(query?)`
    - `getPartDetail(sku)`
    - `createReservation(payload)`
    - `listJobReservations(jobId)`
    - `cancelReservation(reservationId)`
    - `restock(sku, payload)` (optional UI exposure; can be hidden behind a demo/admin section)
  - Centralize error handling and map 400/404/409 to UI-friendly messages.

- Task 2.3.3: Implement `PartsInventoryComponent`
  - Inventory list + search.
  - Navigate/open SKU detail.
  - Provide entry point to reserve (either from detail or list).

- Task 2.3.4: Implement `PartReservationDialog`
  - Inputs: `jobId`, `sku`, `serialTracked`, `availableQuantity`.
  - Form validation: quantity > 0; serial list rules.
  - Submit -> call `PartsServiceClient.createReservation()`.
  - Display API errors:
    - 400 validation
    - 404 not found
    - 409 conflict/insufficient stock

- Task 2.3.5: Implement `JobPartsPanel`
  - Embedded in job screen.
  - Load reservations for job.
  - Create reservation (open dialog) and refresh list.
  - Cancel reservation and refresh list.

- Task 2.3.6: Add Playwright-friendly selectors
  - Add `data-testid` attributes for:
    - inventory search input
    - inventory row / SKU link
    - open reservation dialog button
    - reservation quantity input
    - reservation submit button
    - job parts panel list
    - cancel reservation button
  - Keep selectors stable (avoid CSS-structure selectors).

##### Story 2.4 — Testing: backend unit tests + Playwright automation readiness
- Task 2.4.1: Backend unit tests for `ReservationService`
  - Create success (quantity-tracked SKU).
  - Insufficient stock -> conflict.
  - Cancel restores stock.
  - List-by-job returns expected reservations.
  - Concurrency test:
    - Use `CountDownLatch` to start N threads.
    - Assert no negative inventory.
    - Assert conservation: initialQty == remainingQty + sum(reservedQty).

- Task 2.4.2: Backend controller tests (optional but recommended)
  - Verify status codes and `ApiError` body for 400/404/409.

- Task 2.4.3: Test plan + report artifacts (pipeline)
  - Create/update test plan under `pipeline/test-plan/feature-2-parts-reservation/`.
  - Create/update test report under `pipeline/test-report/feature-2-parts-reservation/`.

- Task 2.4.4: Playwright C# automation (NOT under pipeline)
  - Location: sibling `test-automation/` solution.
  - API tests:
    - Reserve success.
    - Reserve insufficient stock -> 409.
    - Cancel -> 204 and stock restored.
    - List job reservations.
  - UI tests:
    - Browse inventory.
    - Create reservation from job panel.
    - Cancel reservation.
  - Config via env vars:
    - `SERVICEFORGE_API_BASE_URL`, `SERVICEFORGE_UI_BASE_URL`, `SERVICEFORGE_HEADLESS`.

---

## Dependencies

| Dependency | Impact | Resolution |
|---|---|---|
| Existing job model / job screen integration | Reservation is job-linked; must validate `jobId` exists and embed `JobPartsPanel` | Reuse existing job endpoints/data in backend; add job existence check in `ReservationService` |
| Existing `ApiError` conventions | Error responses must be consistent across backend | Reuse existing exception handler / error DTO; add new exceptions mapped to 400/404/409 |
| In-memory store conventions | Must not break existing consumers of `MockDataStore` | Additive changes only; seed all new fields; follow migration-safety guidance |
| Test automation repo layout | Playwright C# lives under sibling `test-automation/` | Keep pipeline clean; only plan/report under pipeline; automation code under `test-automation/` |

---

## Risks & Mitigations

- Risk: Concurrency bugs cause negative inventory or flaky tests
  - Mitigation: per-SKU synchronization on `PartRecord`; deterministic seed; dedicated concurrency unit test.

- Risk: Serial-tracked logic increases complexity and slows delivery
  - Mitigation: keep serial-tracked SKUs limited; implement strict validation first; add auto-assign only if required by UI.

- Risk: Rule doc suggests partial reservation + replenishment, but Feature 2 DoD requires 409 on insufficient stock
  - Mitigation: implement per spec (409). If partial reservation is desired later, record a decision and create a new feature/spec.

---

## Acceptance / Exit Criteria (traceable to DoD)
- All endpoints in the spec are implemented and return correct status codes.
- Reservation creation is atomic per SKU and never results in negative inventory under concurrent requests.
- Errors return `ApiError` with 400/404/409.
- Frontend components exist and use `PartsServiceClient` for all HTTP.
- Backend unit tests cover create/cancel/list and concurrency.
- Playwright automation tasks are defined for implementation under test-automation/.
