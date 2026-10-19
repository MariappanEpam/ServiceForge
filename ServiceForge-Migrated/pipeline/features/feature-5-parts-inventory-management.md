# Feature 5 — Parts inventory management

Status: Draft (forward-engineered from current-state evidence)

## Problem and outcome
Provide authorized staff with UI and API operations to inspect and maintain parts catalog records and their stock quantities. The documented current API supports list/get/create/update/delete under `/api/parts/manage`; exact authorization and field-level validation are not fully evidenced.

## Scope
**In scope:** list, get by SKU, create, update, and delete managed parts; preserve active/orderable flags and non-negative on-hand quantity.

**Out of scope:** supplier integrations, automated replenishment, purchase orders, stock movement audit ledger, bulk import, and separate inventory locations (NEEDS-EVIDENCE / not in confirmed catalog).

## Target-stack flow
React management screen delegates HTTP to an API service. Java 21 / Spring Boot controllers and services call JDBC repositories. Data persists in local-file SQLite via versioned migrations. Entra authentication follows the approved ADR; exact roles/claims are NEEDS-EVIDENCE.

## API contract (target)
Base path: `/api/parts/manage`.
- `GET /api/parts/manage` → `200 Part[]`.
- `GET /api/parts/manage/{sku}` → `200 Part`; `404 ApiError` when missing.
- `POST /api/parts/manage` with part attributes → `201 Part`; `400 ApiError` for invalid data or negative stock.
- `PUT /api/parts/manage/{sku}` with editable attributes → `200 Part`; `400` invalid data; `404` missing SKU.
- `DELETE /api/parts/manage/{sku}` → `204`; exact dependency/conflict behavior needs verification.
These method/status/body shapes are target contract proposals based on the confirmed operation set; verify exact existing behavior before implementation.

## Data model / persistence
Use existing `parts` table: `sku` primary key; `name` required; `description` nullable; `active`, `orderable`, and `quantity_on_hand` required; `quantity_on_hand >= 0` database check. No schema change is presumed. Preserve existing identifiers and constraints unless a reviewed migration is required.

## Acceptance criteria
- AC-1: User can list managed parts and retrieve a part by SKU. Evidence: feature catalog confirms list/get operations.
- AC-2: User can create a part with valid attributes. Evidence: feature catalog confirms create; exact validation details NEEDS-EVIDENCE.
- AC-3: User can update a part and its managed attributes. Evidence: feature catalog confirms update; immutable-field behavior NEEDS-EVIDENCE.
- AC-4: User can delete a part when current business/data constraints allow it. Evidence: feature catalog confirms delete; referenced-part behavior NEEDS-EVIDENCE.
- AC-5: On-hand quantity can never be negative; invalid updates/creates are rejected and the DB CHECK remains a final safeguard. Evidence: BR-12.
- AC-6: Only authorized identities can mutate inventory. Exact role/claim and read-vs-write policy: NEEDS-EVIDENCE.

## Definition of Done
- React CRUD UI with loading, empty, validation, success, and failure states.
- Java API and repository preserve existing route contracts and return the documented error response shape.
- SQLite schema remains local-only and versioned; non-negative CHECK retained.
- Tests cover CRUD, duplicate/missing SKU behavior, negative quantity, and deletion with referenced data (once behavior is confirmed).
- Resolve NEEDS-EVIDENCE for validation, delete constraints, and authorization before implementation.

## Traceability
| AC | Reverse-engineering evidence |
|---|---|
| AC-1–AC-4 | [feature catalog](../../../pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md#L46-L52) |
| AC-5 | [business rule BR-12](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L65-L68) and [parts schema](../../../pipeline/repo-understanding/serviceforge-system-overview/persistence-entities.md#L23-L31) |
| AC-6 | NEEDS-EVIDENCE: role/claim authorization matrix not established. |
| Existing persistence | [persistence entities](../../../pipeline/repo-understanding/serviceforge-system-overview/persistence-entities.md#L23-L31) |
