# Feature 3 — Parts catalog (orderable parts list) (React + Java 21)

Status: Draft (forward-engineered)
Last updated: 2026-10-14

## Problem statement
Users need to browse/search a catalog of parts that are active and orderable so they can reserve parts for jobs.

## In-scope
- List parts that are active and orderable.
- Display part details (SKU, name, description).

## Out-of-scope
- Inventory adjustments (handled by parts inventory management)
- Ordering workflow / supplier integration (NEEDS-EVIDENCE)

## Users and permissions
- Dispatcher and technician can view parts catalog.

NEEDS-EVIDENCE:
- Role definitions and access rules.

## UI flows (React)
### Flow A: Browse parts
1. User navigates to “Parts Catalog”.
2. UI loads parts list.
3. User can filter/search by SKU/name (NEEDS-EVIDENCE: search not confirmed).
4. User selects a part to view details.

### Flow B: Error states
- 401/403 handled via auth UI.

## API contracts (target stack)
Base URL: `/api`

### GET /api/parts
Purpose: list active/orderable parts.
- Response 200: `PartCatalogItem[]`
- Response 401/403: auth errors

`PartCatalogItem`:
- `sku: string`
- `name: string`
- `description?: string`

NEEDS-EVIDENCE:
- Whether quantity on hand is included in catalog response.

## Data model (SQLite)
Baseline table/columns (evidenced):
- `parts(sku, name, description, active, orderable, quantity_on_hand)`

## Business rules (ported)
- BR-8: reservable parts must be active and orderable (catalog should reflect this).

## Acceptance criteria
### Positive
AC-1: User can retrieve a list of parts that are active and orderable.
AC-2: UI displays SKU, name, and description (if present).

### Negative
AC-3: Inactive or non-orderable parts are not returned by the catalog endpoint.
AC-4: If user is unauthenticated, API returns 401.

### Boundary
AC-5: If catalog is empty, UI shows an empty state.

NEEDS-EVIDENCE:
- Sorting/paging behavior is not specified.

## Definition of Done (DoD)
- React parts catalog screen implemented with loading/empty/error states.
- Backend endpoint implemented in Java 21 with Entra token validation.
- Unit tests verify filtering of inactive/non-orderable parts.

## Traceability (AC → repo-understanding evidence)
| AC | Evidence |
|---|---|
| AC-1 | pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md (GET /api/parts exists) |
| AC-2 | pipeline/repo-understanding/serviceforge-system-overview/persistence-entities.md (parts columns) |
| AC-3 | pipeline/repo-understanding/serviceforge-system-overview/business-rules.md (BR-8) |
| AC-4 | ServiceForge-Migrated/pipeline/decisions/adr-0002-auth-azure-entra.md (forward-looking auth decision) |
| AC-5 | NEEDS-EVIDENCE (empty state behavior not captured) |
