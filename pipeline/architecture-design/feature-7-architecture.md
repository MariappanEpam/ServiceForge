# Feature 7 — Architecture Design (Reviewed): Parts Inventory Management (CRUD)

| Field | Value |
|---|---|
| Feature | 7 |
| Spec | pipeline/features/feature-7-parts-inventory-management.md |
| Source architecture (handoff) | pipeline/architecture/feature-7-architecture.md |
| Review date | 2026-10-08 |
| Status | Needs updates (non-blocking) |

## 0) Review summary

The handoff architecture is broadly aligned with the spec (CRUD endpoints, soft delete, validation, Angular service separation). The main gaps are around **repo conventions and cross-feature compatibility**:

- The repo’s canonical conventions emphasize **in-memory mock data**; Feature 7 assumes SQLite as source of truth. This is acceptable only if Feature 5 already introduced SQLite and the project decisions/rules confirm it.
- The architecture doc should explicitly define **ApiError shape**, **global exception mapping**, and **DTO boundaries** to avoid drift.
- The optional **delete guard** depends on reservation schema/status; the design should define a safe fallback (no guard) and avoid hard-coupling to unknown tables.
- Search semantics should be clarified (case sensitivity, trimming, and whether inactive parts are returned by `GET /{sku}`).

This reviewed doc proposes adjustments while keeping the spec’s API contract intact.

---

## 1) Scope, dependencies, and compatibility

### In scope
- Parts catalog CRUD:
  - List/search parts
  - Get part by SKU
  - Create part
  - Update part
  - Soft delete part (`active=false`)

### Out of scope
- Reservations workflows, replenishment/purchasing, warehouse/bin locations, serial tracking, bulk import/export.
- RBAC beyond a simple admin guard.

### Dependencies
- Feature 5 (SQLite persistence foundation) is a hard dependency.

### Compatibility constraints (must-haves)
- Preserve existing REST conventions:
  - `ResponseEntity<T>`
  - `ApiError` body for 400/404/409
- Do not break any existing “parts reservation” flows:
  - If existing endpoints/models already expose parts, keep field names and meanings consistent.

---

## 2) Architecture overview

### Backend (Spring Boot)
Package-by-layer (repo convention):
- `controller` — REST endpoints under `/api/parts`
- `service` — business rules and orchestration
- `data` — persistence (repository + entity/row mapping)
- `dto` — request/response DTOs
- `model` — domain model (optional; only if it adds value beyond persistence entity)

Persistence:
- Use the same persistence approach introduced in Feature 5 (do not introduce a new persistence stack in Feature 7).
- SQLite schema managed via versioned migrations.

### Frontend (Angular)
- One screen: Parts Management.
- All HTTP calls via a dedicated Angular service.
- Components remain thin (render + delegate).

---

## 3) API contract (authoritative)

Base path: `/api/parts`

### 3.1 List/search
`GET /api/parts?q={q?}&activeOnly={activeOnly?}`

- `activeOnly` defaults to `true`.
- `q` is a contains match against SKU or name.

Response: `200 OK` with `PartDto[]`.

**Clarifications (recommended):**
- Trim `q`; treat empty/blank as “no filter”.
- Define case behavior:
  - Prefer case-insensitive search using `LOWER(sku)` / `LOWER(name)` comparisons for consistent UX.

### 3.2 Get by SKU
`GET /api/parts/{sku}`

Response:
- `200 OK` with `PartDto`
- `404 Not Found` with `ApiError` if not found

**Clarification (recommended):**
- Treat `active=false` as not found for this endpoint (aligns with “SKU does not exist” wording), unless a future admin-only endpoint is added.

### 3.3 Create
`POST /api/parts`

Response:
- `201 Created` with created `PartDto`
- `400 Bad Request` with `ApiError` for validation
- `409 Conflict` with `ApiError` if SKU already exists (including inactive)

**Clarification (recommended):**
- If the spec requires `active` in the request, keep it required. If implementation wants to default `active=true`, that is a spec change and should be explicitly approved.

### 3.4 Update
`PUT /api/parts/{sku}`

Response:
- `200 OK` with updated `PartDto`
- `400 Bad Request` with `ApiError` for validation
- `404 Not Found` with `ApiError` if SKU does not exist

Rule:
- Path SKU is authoritative.
- If request body contains `sku`, reject with 400 unless it matches path.

### 3.5 Delete (soft)
`DELETE /api/parts/{sku}`

Response:
- `204 No Content` on success
- `404 Not Found` with `ApiError` if SKU does not exist
- `409 Conflict` with `ApiError` if delete is blocked due to existing ACTIVE reservations referencing the SKU (optional)

**Delete guard design (safe):**
- Implement guard only if:
  - a reservations table exists in the Feature 5 schema, and
  - “ACTIVE” is a stable, well-defined status.
- Otherwise, omit guard and rely on soft delete to preserve history.

---

## 4) DTOs, validation, and error mapping

### DTOs
`PartDto`
- `sku: string`
- `name: string`
- `description?: string | null`
- `quantityOnHand: number`
- `active: boolean`

`PartCreateRequest`
- `sku: string`
- `name: string`
- `description?: string | null`
- `quantityOnHand: number`
- `active: boolean`

`PartUpdateRequest`
- `name: string`
- `description?: string | null`
- `quantityOnHand: number`
- `active: boolean`
- (optional) `sku?: string` only if enforcing “must match path” via DTO

### Validation (Jakarta Bean Validation)
- `sku`: `@NotBlank`, `@Pattern("^[A-Z0-9-]{3,32}$")`
- `name`: `@NotBlank`, `@Size(max=120)`
- `description`: `@Size(max=500)`
- `quantityOnHand`: `@NotNull`, `@Min(0)`
- `active`: `@NotNull`

### Error mapping (repo convention)
- Validation failures → `400` with `ApiError`
- Not found → `404` with `ApiError`
- Conflicts (duplicate SKU, delete blocked) → `409` with `ApiError`

**Gap to close:**
- Ensure there is a single global exception handler that maps domain exceptions to `ApiError` consistently.

---

## 5) Persistence and migrations

### Schema (SQLite)
Table: `parts`
- `sku` TEXT PRIMARY KEY
- `name` TEXT NOT NULL
- `description` TEXT NULL
- `quantity_on_hand` INTEGER NOT NULL CHECK(quantity_on_hand >= 0)
- `active` INTEGER NOT NULL CHECK(active IN (0,1))
- `created_at` TEXT NOT NULL (ISO-8601) (optional)
- `updated_at` TEXT NOT NULL (ISO-8601) (optional)

Indexes (optional):
- `idx_parts_active(active)`
- `idx_parts_name(name)`

### Migration rules
- If Feature 5 already created `parts`, Feature 7 migrations must be additive only.
- Do not rename columns or change semantics without a migration-safety review.

### Seed strategy
- `seed=none`: no parts inserted.
- `seed=baseline`: deterministic baseline parts.
- `seed=reset`: local demo only.

**Gap to close:**
- Confirm whether existing reservation demos/tests depend on specific SKUs; if yes, baseline seed must include them.

---

## 6) Frontend design

### Route
- `/parts/manage` (or align to existing routing patterns).

### Components
- `PartsManagementComponent`
  - Search input
  - Active-only toggle
  - Parts table
  - Add/Edit/Delete actions

- `PartFormComponent` (dialog or inline)
  - Create includes `sku`
  - Edit disables `sku`

### Service
`PartsApiService`
- `listParts(q?: string, activeOnly: boolean = true)`
- `getPart(sku: string)`
- `createPart(req)`
- `updatePart(sku, req)`
- `deletePart(sku)`

### Test hooks
Add `data-testid` attributes for key controls (per spec).

---

## 7) Risks and mitigations

- Risk: Feature 7 assumes SQLite but repo overview mentions in-memory mock data.
  - Mitigation: treat Feature 5 as the authoritative pivot; ensure project decisions/rules explicitly allow SQLite and define seed behavior.

- Risk: Delete guard couples to unknown reservation schema.
  - Mitigation: make guard conditional and optional; default to soft delete without guard.

- Risk: Search behavior differs across SQLite collations.
  - Mitigation: implement explicit case-insensitive search using `LOWER()`.

---

## 8) Approval status

- Critical issues: None
- Non-blocking updates recommended: Yes (error mapping clarity, search semantics, delete guard conditionality, seed compatibility)
