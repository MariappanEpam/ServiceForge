# Feature 7 — Parts Inventory Management (CRUD)

| Field | Value |
|---|---|
| **Status** | Draft |
| **One-line intent** | Allow users to create, update, and delete parts in the parts catalog/inventory |
| **Depends on** | Feature 5 (SQLite persistence foundation) |
| **In scope** | CRUD for parts catalog/inventory: add part, edit part, delete part (soft-delete preferred), list/search parts |
| **Out of scope** | Reservations, replenishment/purchasing, warehouse locations/bins, serial tracking, role-based access control beyond a simple admin guard, bulk import/export |
| **Definition of Done** | Users can add, modify, and delete parts; backend exposes REST endpoints with correct 200/201/204 + 400/404/409 `ApiError` semantics; Angular UI provides a parts management screen using an Angular service; basic tests cover API + UI flows |

## Problem statement

ServiceForge already models parts for reservation flows, but there is no dedicated CRUD management capability for maintaining the parts catalog and on-hand quantities.

We need a minimal **Parts Inventory Management** feature so a user can:
- add a new part
- modify an existing part
- delete a part

This feature is intentionally limited to CRUD only.

## Goals

1. Provide a stable REST API for parts CRUD.
2. Provide a simple Angular screen for managing parts.
3. Preserve existing API conventions (`ApiError`, 400/404/409).
4. Keep persistence local-only (SQLite) per repo rules.

## Non-goals

- Parts reservation workflows (already covered by other features).
- Partial reservations, replenishment, notifications.
- Warehouse/location management.
- Serial-tracked inventory.
- Advanced auth/RBAC.

## Assumptions

- SQLite is the source of truth for parts (Feature 5 decision).
- Deleting a part should not break historical reservations; therefore **soft delete** is preferred.
- SKU is the primary identifier and must be unique.

## Data model (logical)

A `Part` has:
- `sku` (string, unique, required)
- `name` (string, required)
- `description` (string, optional)
- `quantityOnHand` (integer, required, must be $\ge 0$)
- `active` (boolean, required)

Notes:
- `active=false` represents a deleted/retired part (soft delete).

## Backend API (REST)

Base path: `/api/parts`

### 1) List/search parts
`GET /api/parts`

Query params (optional):
- `q` (string) — search by SKU or name (contains match)
- `activeOnly` (boolean, default `true`) — when `true`, return only active parts

Response: `200 OK`
```json
[
  {
    "sku": "SKU-1001",
    "name": "Air Filter",
    "description": "HVAC air filter",
    "quantityOnHand": 12,
    "active": true
  }
]
```

### 2) Get part by SKU
`GET /api/parts/{sku}`

Response:
- `200 OK` with part
- `404 Not Found` with `ApiError` if SKU does not exist

### 3) Create part
`POST /api/parts`

Request body:
```json
{
  "sku": "SKU-2001",
  "name": "Thermostat",
  "description": "Smart thermostat",
  "quantityOnHand": 5,
  "active": true
}
```

Response:
- `201 Created` with created part
- `400 Bad Request` with `ApiError` for validation errors
- `409 Conflict` with `ApiError` if SKU already exists

Validation:
- `sku` required, pattern `^[A-Z0-9-]{3,32}$`
- `name` required, max length 120
- `description` max length 500
- `quantityOnHand` required, $\ge 0$

### 4) Update part
`PUT /api/parts/{sku}`

Request body (full replace for MVP):
```json
{
  "name": "Thermostat v2",
  "description": "Updated description",
  "quantityOnHand": 7,
  "active": true
}
```

Response:
- `200 OK` with updated part
- `400 Bad Request` with `ApiError` for validation errors
- `404 Not Found` with `ApiError` if SKU does not exist

Rules:
- Path SKU is authoritative; body must not contain `sku` (or if present must match path).

### 5) Delete part
`DELETE /api/parts/{sku}`

Behavior (MVP):
- Soft delete: set `active=false`.

Response:
- `204 No Content` on success
- `404 Not Found` with `ApiError` if SKU does not exist
- `409 Conflict` with `ApiError` if the part cannot be deleted due to existing active reservations (optional guard; see below)

Deletion guard (optional, but recommended):
- If there are ACTIVE reservations referencing the SKU, return 409 and instruct user to cancel reservations first.

## Frontend (Angular)

### Screen: Parts Management
- Route: `/parts/manage` (or similar, per existing routing conventions)
- Capabilities:
  - list/search parts
  - add part (modal or inline form)
  - edit part
  - delete part (with confirmation)

### Angular service
- All HTTP calls must be in a dedicated service (e.g., `PartsApiService`).
- Components must not call `HttpClient` directly.

### UX requirements (minimal)
- Show validation errors from backend.
- Show conflict errors (409) clearly (e.g., duplicate SKU).
- Use stable `data-testid` attributes for automation.

## Error handling conventions

- Use existing `ApiError` response shape.
- Status codes:
  - 400 validation
  - 404 not found
  - 409 conflict (duplicate SKU, delete blocked)

## Definition of Done (detailed)

Backend
- [ ] Endpoints implemented: list/search, get-by-sku, create, update, delete.
- [ ] Validation enforced; errors return `ApiError` with 400/404/409.
- [ ] SKU uniqueness enforced.
- [ ] Delete is soft delete (`active=false`).

Frontend
- [ ] Parts management screen exists and is reachable.
- [ ] Add/edit/delete flows work end-to-end.
- [ ] All HTTP via Angular service.
- [ ] `data-testid` attributes added for key controls.

Tests
- [ ] Backend tests cover: create success, create duplicate -> 409, update missing -> 404, delete -> 204, list/search.
- [ ] Frontend tests cover: component renders list, create calls service, delete confirmation triggers service.

---

Handoff update (for Architecture Design Agent)
- Status: Specification Complete
- Owner: Architecture Design Agent
- Artifacts:
  - pipeline/features/feature-7-parts-inventory-management.md
