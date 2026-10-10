# Implementation Plan — Feature 7: Parts Inventory Management (CRUD)

| Field | Value |
|---|---|
| Feature | 7 |
| Spec | pipeline/features/feature-7-parts-inventory-management.md |
| Reviewed architecture | pipeline/architecture-design/feature-7-architecture.md |
| Depends on | Feature 5 (SQLite persistence foundation) |
| Plan output | pipeline/implementation-plan/feature-7-implementation-plan.md |

## Executive Summary

### Scope (from spec)
- Backend REST CRUD for parts under `/api/parts`:
  - list/search (`q`, `activeOnly` default `true`)
  - get by SKU
  - create
  - update (full replace)
  - delete (soft delete via `active=false`)
- Angular Parts Management screen:
  - list/search
  - add/edit/delete
  - uses a dedicated Angular service for HTTP
  - shows backend validation/conflict errors
  - adds stable `data-testid` attributes
- Tests:
  - backend tests for core API flows and error semantics
  - frontend tests for basic component/service flows

### Assumptions (explicit in spec/decisions)
- SQLite is allowed and is the persistence source of truth (Feature 5 + project decisions).
- Seed behavior is explicit (none/baseline/reset) and must not break existing flows.
- Soft delete is the default delete behavior.

### Constraints
- Do not invent scope beyond the Feature 7 spec.
- Preserve REST conventions: `ResponseEntity<T>` and `ApiError` for 400/404/409.
- Local-only SQLite with versioned migrations (per pipeline/rules/local-sqlite-only.md).
- Do not break existing parts reservation flows (pipeline/rules/parts-count-inventory.md).

---

## Delivery Roadmap

### Phase 1 (MVP — backend API + persistence)
1. Add/confirm SQLite schema support for `parts` (migration if needed).
2. Implement repository + service + controller for `/api/parts` endpoints.
3. Implement validation + error mapping for 400/404/409.
4. Add backend tests for CRUD + search + error semantics.

### Phase 2 (MVP — frontend screen)
1. Add Angular route + Parts Management component(s).
2. Add `PartsApiService` with typed DTOs.
3. Wire UI flows (list/search/create/update/delete + confirmation).
4. Add frontend unit tests (component/service interaction).

### Phase 3 (Hardening)
1. Ensure seed compatibility with any existing reservation demos/tests.
2. Add optional delete guard only if reservation schema/status is already stable and available (architecture allows conditional guard).

---

## Epic Breakdown

### EPIC: Feature 7 — Parts Inventory Management (CRUD)

#### Feature 7.1 — Backend: Parts CRUD API (SQLite)

##### Story 7.1.1 — Persistence: parts table + migration alignment
**Goal:** Ensure the SQLite schema supports the `Part` logical model required by the spec.

**Tasks**
- T7.1.1.1 Inspect existing Feature 5 schema/migrations for `parts` table.
- T7.1.1.2 If missing/incomplete, add a new versioned migration to create/extend `parts`:
  - `sku` TEXT PK
  - `name` TEXT NOT NULL
  - `description` TEXT NULL
  - `quantity_on_hand` INTEGER NOT NULL with non-negative constraint
  - `active` INTEGER NOT NULL (0/1)
- T7.1.1.3 Confirm seed modes remain explicit (none/baseline/reset) and do not introduce new seed behavior beyond what Feature 5 already supports.

**Acceptance Criteria (maps to DoD)**
- Schema supports all fields in the spec’s `Part` model.
- Migrations are versioned and run cleanly on a fresh DB.

##### Story 7.1.2 — Repository layer for parts
**Goal:** Provide data access methods needed by the service/controller.

**Tasks**
- T7.1.2.1 Create `PartEntity`/row mapping (or equivalent) aligned to schema.
- T7.1.2.2 Implement repository methods:
  - `findAll(q?, activeOnly=true)` (contains match on SKU or name)
  - `findBySku(sku)`
  - `insert(part)` with uniqueness enforcement
  - `updateBySku(sku, update)`
  - `softDeleteBySku(sku)` (set `active=false`)
- T7.1.2.3 Implement case-insensitive search per reviewed architecture recommendation (e.g., `LOWER()`), while keeping spec semantics (contains match).

**Acceptance Criteria**
- Repository supports all endpoint needs.
- Search returns expected results for SKU/name contains.

##### Story 7.1.3 — Service layer: business rules + error semantics
**Goal:** Centralize validation rules that are not purely DTO validation and map domain errors to API errors.

**Tasks**
- T7.1.3.1 Implement service methods:
  - `listParts(q, activeOnly)`
  - `getPartBySku(sku)`
  - `createPart(req)`
  - `updatePart(sku, req)`
  - `deletePart(sku)`
- T7.1.3.2 Enforce SKU uniqueness on create:
  - duplicate SKU → 409 `ApiError`
- T7.1.3.3 Enforce not-found semantics:
  - get/update/delete missing SKU → 404 `ApiError`
- T7.1.3.4 Implement soft delete:
  - delete sets `active=false`
- T7.1.3.5 Optional delete guard (only if existing reservation schema/status supports it without new scope):
  - if ACTIVE reservations reference SKU → 409 `ApiError`
  - otherwise omit guard

**Acceptance Criteria**
- Service returns correct outcomes and throws domain exceptions that map to 400/404/409.

##### Story 7.1.4 — Controller layer: REST endpoints + DTO validation
**Goal:** Expose the spec-defined endpoints under `/api/parts`.

**Tasks**
- T7.1.4.1 Create DTOs:
  - `PartDto`
  - `PartCreateRequest`
  - `PartUpdateRequest`
- T7.1.4.2 Add Jakarta validation annotations per spec:
  - `sku` required, pattern `^[A-Z0-9-]{3,32}$`
  - `name` required, max length 120
  - `description` max length 500
  - `quantityOnHand` required, >= 0
  - `active` required
- T7.1.4.3 Implement endpoints:
  - `GET /api/parts?q=&activeOnly=` → 200 `PartDto[]`
  - `GET /api/parts/{sku}` → 200 `PartDto` or 404
  - `POST /api/parts` → 201 `PartDto` or 400/409
  - `PUT /api/parts/{sku}` → 200 `PartDto` or 400/404
  - `DELETE /api/parts/{sku}` → 204 or 404/409
- T7.1.4.4 Enforce “path SKU authoritative” rule:
  - if body contains `sku` and it differs → 400 `ApiError` (or omit `sku` from update DTO)

**Acceptance Criteria**
- Endpoints match spec paths, status codes, and response shapes.
- Validation errors return 400 with `ApiError`.

##### Story 7.1.5 — Backend tests (API)
**Goal:** Provide basic automated coverage for the API flows in the spec DoD.

**Tasks**
- T7.1.5.1 Add integration tests for:
  - create success → 201
  - create duplicate SKU → 409
  - update missing SKU → 404
  - delete success → 204
  - list/search returns expected results
- T7.1.5.2 Add validation tests (at least one): invalid SKU pattern or negative `quantityOnHand` → 400.

**Acceptance Criteria**
- Tests cover the DoD-required backend scenarios.

---

#### Feature 7.2 — Frontend: Parts Management UI

##### Story 7.2.1 — Angular service: `PartsApiService`
**Goal:** Encapsulate all HTTP calls for parts CRUD.

**Tasks**
- T7.2.1.1 Create typed interfaces matching backend DTOs (`PartDto`, create/update requests).
- T7.2.1.2 Implement service methods:
  - `listParts(q?: string, activeOnly: boolean = true)`
  - `getPart(sku: string)`
  - `createPart(req)`
  - `updatePart(sku, req)`
  - `deletePart(sku)`
- T7.2.1.3 Ensure error responses (`ApiError`) are surfaced to components for display.

**Acceptance Criteria**
- Components do not call `HttpClient` directly.

##### Story 7.2.2 — Parts Management screen (route + component)
**Goal:** Provide a minimal UI to list/search and manage parts.

**Tasks**
- T7.2.2.1 Add route (per spec suggestion: `/parts/manage`) aligned to existing routing conventions.
- T7.2.2.2 Implement `PartsManagementComponent`:
  - search input (`q`)
  - active-only toggle
  - parts table
  - add/edit/delete actions
- T7.2.2.3 Implement add/edit form (modal or inline) with required fields.
- T7.2.2.4 Implement delete confirmation.
- T7.2.2.5 Add stable `data-testid` attributes for key controls:
  - search input
  - active-only toggle
  - add button
  - save button
  - delete button
  - confirm delete
  - parts table rows

**Acceptance Criteria**
- Screen is reachable and CRUD flows work end-to-end.
- Validation/conflict errors are shown.

##### Story 7.2.3 — Frontend tests
**Goal:** Provide basic unit tests per DoD.

**Tasks**
- T7.2.3.1 Component renders list (mock service returns parts).
- T7.2.3.2 Create flow calls service with expected payload.
- T7.2.3.3 Delete confirmation triggers service call.

**Acceptance Criteria**
- Tests cover the DoD-required frontend scenarios.

---

## Dependencies

| Dependency | Impact | Resolution |
|---|---|---|
| Feature 5 (SQLite persistence foundation) | Required for schema/migrations/seed modes | Ensure Feature 5 is merged and migrations run in dev/test |
| Existing parts reservation flows (inventory rules) | CRUD must not break reservation behavior | Soft delete preserves history; avoid changing reservation endpoints/contracts |
| Seed stability (local-sqlite-only rule) | UI/tests may rely on stable SKUs | If baseline seed includes parts used elsewhere, keep them stable |

---

## Risks & Mitigations (within spec scope)

- Risk: Delete guard depends on reservation schema/status not guaranteed by Feature 7.
  - Mitigation: Implement guard only if existing schema/status is already present; otherwise omit (still compliant with spec’s “optional guard”).

- Risk: Search behavior differs by SQLite collation/case.
  - Mitigation: Implement explicit case-insensitive contains search (e.g., `LOWER()`), consistent with reviewed architecture.

- Risk: Soft-deleted parts visibility.
  - Mitigation: Default `activeOnly=true` for list; ensure UI toggle controls it.

---

## Definition of Done (DoD) Mapping

### Backend DoD (spec)
- ✅ Endpoints implemented: list/search, get-by-sku, create, update, delete.
  - Covered by Stories 7.1.2–7.1.4
- ✅ Validation enforced; errors return `ApiError` with 400/404/409.
  - Covered by Stories 7.1.3–7.1.4
- ✅ SKU uniqueness enforced.
  - Covered by Story 7.1.3
- ✅ Delete is soft delete (`active=false`).
  - Covered by Story 7.1.3

### Frontend DoD (spec)
- ✅ Parts management screen exists and is reachable.
  - Covered by Story 7.2.2
- ✅ Add/edit/delete flows work end-to-end.
  - Covered by Story 7.2.2
- ✅ All HTTP via Angular service.
  - Covered by Story 7.2.1
- ✅ `data-testid` attributes added for key controls.
  - Covered by Story 7.2.2

### Tests DoD (spec)
- ✅ Backend tests cover: create success, create duplicate -> 409, update missing -> 404, delete -> 204, list/search.
  - Covered by Story 7.1.5
- ✅ Frontend tests cover: component renders list, create calls service, delete confirmation triggers service.
  - Covered by Story 7.2.3
