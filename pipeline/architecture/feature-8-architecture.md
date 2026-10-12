# Feature 8 — Architecture Design: Job ↔ Parts Mapping & UI Control Conditions

> Canonical copy for the orchestration workflow.
> Source of truth content is maintained in: `pipeline/architecture-design/feature-8-architecture.md`

---

# Feature 8 — Architecture Design: Job ↔ Parts Mapping & UI Control Conditions

| Field | Value |
|---|---|
| Feature | 8 |
| Spec | pipeline/features/feature-8-job-parts-mapping-and-ui-conditions.md |
| Review date | 2026-10-08 |
| Status | Draft |

## 1) Scope and dependencies

### In scope
- Enforce/align parts reservation relationship rules:
  - reservation requires `jobId`
  - no orphan reservations
  - `jobId` immutable after creation
- Job-scoped reservation listing.
- UI control conditions for reservation actions based on job context/status, inventory availability, and reservation state.
- Navigation context preservation (`jobId`) across forms via Site Map.

### Out of scope
- Partial reservations, replenishment, notifications.
- Warehouse/bin locations.
- New RBAC.

### Depends on
- Existing reservation endpoints and data model from Feature 2 / Feature 4.
- Navigation rules: pipeline/rules/navigation-site-map.md
- Inventory rules: pipeline/rules/parts-count-inventory.md
- Relationship rules: pipeline/rules/parts-reservation-against-job.md

---

## 2) High-level architecture

### Backend (Spring Boot)
Layered packages (repo convention):
- `controller` — reservation endpoints (existing controllers extended)
- `service` — enforce relationship rules and UI-relevant validation outcomes
- `data` — repository / store access (in-memory or SQLite depending on Feature 5+ decisions)
- `dto` — request/response DTOs

Key principle: **do not introduce new endpoints unless required**; prefer aligning existing endpoints to the rules.

### Frontend (Angular)
- Reservation UI remains in a dedicated feature form/screen (NAV-001).
- All HTTP calls via Angular service.
- UI state (enabled/disabled/visible) is derived from:
  - route/job context (`jobId`)
  - job status
  - selected part availability
  - reservation status

---

## 3) Backend design details

### 3.1 Reservation create validation flow
On `POST` create reservation:
1. Validate payload (`jobId`, `sku`, `quantity`).
2. Validate job exists.
3. Validate job status allows reservation.
4. Validate part exists and inventory is sufficient (per existing reservation rules).
5. Create reservation record with immutable `jobId`.

Error mapping (repo convention):
- 400: missing/invalid payload (including missing `jobId`)
- 404: unknown `jobId` or unknown `sku`
- 409: insufficient stock; attempt to change `jobId`; cancel conflict

### 3.2 JobId immutability
- If there is an update endpoint for reservations, enforce:
  - any attempt to change `jobId` → 409
- If there is no update endpoint, immutability is guaranteed by design (create-only + cancel).

### 3.3 No orphan reservations
- Create validates job existence.
- Cancel does not remove job; it only changes reservation state.
- If job deletion exists elsewhere, it must either:
  - be blocked when active reservations exist, or
  - cascade-cancel reservations (explicit decision required; not introduced here).

---

## 4) Frontend design details

### 4.1 Navigation + context preservation
- Site Map provides access to:
  - Job Details
  - Parts Reservation (job-scoped)
- When navigating from Job Details to Parts Reservation, pass `jobId` via:
  - route param (preferred): `/jobs/:jobId/parts-reservations`
  - or query param: `/parts/reservations?jobId=...`

### 4.2 UI control conditions (implementation approach)
Implement a single view-model state object in the component (computed from inputs + API data):
- `hasJobContext: boolean`
- `jobAllowsReservation: boolean`
- `selectedPartAvailableQty: number | null`
- `canReserve: boolean`
- `reserveDisabledReason: string | null`

Controls bind to this state:
- Disable `ReserveButton` when `!canReserve`.
- Show inline message when disabled.

---

## 5) Risks and mitigations

- Risk: Two competing reservation APIs (Feature 2 vs Feature 4) cause drift.
  - Mitigation: pick one canonical endpoint set for UI; keep the other as compatibility alias if it already exists.

- Risk: Job status rules are not modeled.
  - Mitigation: define allowed statuses in one place (backend service) and keep UI driven by backend responses.

---

## 6) Non-functional requirements

- Maintain existing `ApiError` response shape.
- No SPOF introduced (local-only app; no new infra).
- No external services.
