# Feature 8 — Job ↔ Parts Mapping & UI Control Conditions

| Field | Value |
|---|---|
| **Status** | Draft |
| **One-line intent** | Align parts reservation behavior and UI control visibility/enabled states with the updated rules: reservations are job-mandatory/immutable and UI controls are conditionally shown/enabled based on job context and inventory state |
| **Depends on** | Feature 2 (Parts Reservation), Feature 4 (Parts reservation by technician), Feature 7 (Parts Inventory Management), NAV rules (site map + context preservation) |
| **In scope** | - Enforce/align backend rules that a reservation is always linked to a Job (`jobId` required), cannot be orphaned, and `jobId` is immutable after creation.
- Ensure reservation listing is job-scoped (query by `jobId`).
- Ensure UI flows preserve `jobId` context when navigating to parts-related screens.
- Add/align UI control conditions for parts reservation actions (show/enable/disable) based on:
  - whether a `jobId` context exists
  - job status allows reservation
  - inventory availability for selected part
  - reservation state (active/cancelled)
- Update site map registration if new screens/routes are introduced.
 |
| **Out of scope** | - Purchasing/replenishment workflows (auto-create order requests).
- Partial reservations (auto-reserve available quantity and order the rest).
- Notifications.
- Warehouse/bin locations.
- RBAC beyond existing demo assumptions.
- Any change to scheduling overlap rules.
 |
| **Definition of Done** | - Backend rejects reservation create requests without `jobId` (400) and rejects unknown `jobId` (404).
- Backend prevents changing `jobId` for an existing reservation (409 conflict).
- Backend guarantees no orphan reservations exist (create validates job existence; delete/cancel keeps referential integrity).
- Frontend:
  - Reservation UI cannot be used without a job context; controls are hidden/disabled with clear messaging.
  - Controls follow the UI condition rules below.
  - Navigation preserves `jobId` across related forms via route/query param or shared state per existing conventions.
- Tests:
  - Backend unit/integration tests cover job-mandatory, immutability, and job-scoped listing.
  - Frontend tests cover control visibility/enabled states for key scenarios.
 |

---

## Background / rule alignment

This feature exists to align implementation with updated standing rules:

- Parts reservation must be **exclusively against a Job** (job mandatory, no orphan reservations, jobId immutable).
- UI controls must be conditionally shown/enabled based on context and state.
- Navigation uses the Site Map and preserves `jobId` context across related forms.

Authoritative rules:
- pipeline/rules/parts-reservation-against-job.md
- pipeline/rules/parts-count-inventory.md
- pipeline/rules/navigation-site-map.md
- pipeline/rules/how-controls-should-look-like.md

---

## Functional requirements

### FR-001: Reservation create requires job context
- The reservation create flow must require a `jobId`.
- If the user is not in a job context, the UI must not allow submission.

### FR-002: Job existence + status validation
- Backend must validate `jobId` exists.
- Backend must validate job status allows reservation (see rule PR-006). If job status rules are not yet modeled, treat this as:
  - Allowed statuses: `OPEN`, `SCHEDULED` (assumption; confirm in implementation)
  - Disallowed: `CLOSED`, `CANCELLED`

### FR-003: JobId immutability
- Once created, a reservation’s `jobId` cannot be changed.
- If an update endpoint exists that could change it, it must reject with 409.

### FR-004: Job-scoped reservation listing
- Reservations must be retrievable by job:
  - `GET /api/jobs/{jobId}/parts-reservations` (or existing equivalent)

### FR-005: UI control conditions (reservation panel)

#### Controls
- `SelectPartDropdown`
- `QuantityInput`
- `ReserveButton`
- `CancelReservationButton`

#### Conditions
1) No `jobId` context
- Hide reservation panel OR show it disabled.
- Disable `SelectPartDropdown`, `QuantityInput`, `ReserveButton`.
- Show message: “Select a job to reserve parts.”

2) Job status does not allow reservation
- Disable `ReserveButton`.
- Allow viewing existing reservations.
- Show message: “Parts cannot be reserved for this job status.”

3) Inventory availability
- If `AvailableQuantity == 0` for selected part:
  - Disable `ReserveButton`.
  - Show message: “Out of stock.”
- If `RequestedQuantity > AvailableQuantity`:
  - Disable `ReserveButton` (MVP) and show message: “Insufficient stock.”
  - (No partial reservation behavior in this feature.)

4) Reservation state
- For cancelled reservations:
  - Hide/disable `CancelReservationButton`.

---

## API surface (backend)

This feature is primarily alignment; it may reuse existing endpoints from Features 2/4.

Minimum required behaviors:
- Reservation create endpoint must require `jobId` and validate it.
- Reservation list endpoint must be job-scoped.
- Any endpoint that could change `jobId` must reject.

If an explicit update endpoint is needed for immutability enforcement, add:
- `PUT /api/parts/reservations/{reservationId}` (only if already present; otherwise do not add new scope)
  - Must reject any attempt to change `jobId` with 409.

---

## UI surface (frontend)

- Ensure parts reservation UI is reachable via Site Map.
- Ensure `jobId` context is preserved when navigating from Job screen to Parts Reservation screen.

---

## Open questions / assumptions

1) Job status model: which statuses exist today and which allow reservation?
2) Which existing endpoints are canonical for reservations (Feature 2 vs Feature 4)?
3) Should the reservation UI be embedded in Job Details only (preferred), or also as a standalone screen that requires selecting a job?

---

## Handoff update (for Architecture Design Agent)
- Status: Specification Complete
- Owner: Architecture Design Agent
- Artifacts:
  - pipeline/features/feature-8-job-parts-mapping-and-ui-conditions.md
