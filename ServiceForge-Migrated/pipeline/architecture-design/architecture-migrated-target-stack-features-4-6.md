# Architecture design — ServiceForge-Migrated Features 4–6

Date: 2026-10-16  
Scope: Job parts reservation, parts inventory management, technician onboarding/management  
Status: Draft for design review; implementation gated on listed decisions

## Inputs
- Feature specs 4–6 in `ServiceForge-Migrated/pipeline/features/`.
- Target architecture ADRs 0001–0003.
- Current-state evidence in `pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md`, `business-rules.md`, and `persistence-entities.md`.

## Architecture decisions inherited
- React + TypeScript frontend; Java 21 / Spring Boot 3 REST backend.
- SQLite local-file persistence using JDBC/repositories and versioned Flyway migrations.
- Microsoft Entra ID for authentication; use the agreed backend resource-server and SPA token-validation pattern from the approved ADR/design.
- No external database services and no application-source changes in this planning artifact.

## Components and responsibilities
- **React management screens:** reservation workflow, parts inventory CRUD, and technician CRUD/state-management. Components render state and delegate HTTP to feature API services.
- **API client/services:** typed request/response contracts and common error handling; do not place network calls in UI components.
- **Spring controllers:** expose `/api/jobs/{jobId}/parts-reservations`, `/api/parts/manage`, and `/api/technicians` contracts. Preserve the existing API behavior as verified from source before implementation.
- **Application services:** enforce reservation invariants (positive quantity, existing job, active/orderable part, adequate inventory, one-time cancellation/restock) and technician state transitions.
- **JDBC repositories/gateway:** own SQL access. Reservation create/cancel and inventory quantity changes must be transactionally consistent; confirm current transaction boundary and SQLite locking behavior before implementation.
- **SQLite + Flyway:** reuse `parts`, `job_part_reservations`, and `technicians` data already present in the current schema model. Do not add tables/fields absent from approved specs without a reviewed migration.
- **Security:** enforce Entra authentication consistently. Authorization roles/scopes for stock maintenance, technician administration, and reservations remain an explicit pre-implementation decision.

## Request flows

### Feature 4 — reservation
1. UI loads reservations for the selected job and orderable parts.
2. UI posts `{sku, quantity}` to the job reservation collection.
3. Backend validates job and quantity, verifies part is active/orderable, atomically decrements only if stock is sufficient, and persists reservation.
4. Cancellation marks the reservation cancelled and restores the quantity once; repeated cancellation returns conflict.

### Feature 5 — inventory management
1. Authorized user lists/searches managed parts and opens a SKU record.
2. Create/update requests pass validation, including non-negative stock.
3. Delete behavior for referenced parts must preserve referential integrity; exact legacy response and policy need evidence before implementation.

### Feature 6 — technician management
1. Authorized user lists/searches technicians and views profile details.
2. Create/update requests persist the evidenced profile fields.
3. Deactivate/reactivate are explicit state transitions; avoid hard deletion unless a separately approved requirement establishes it.

## Contracts and error handling
- Keep existing endpoint paths and operation sets described in the three specs.
- Continue the established `ApiError` contract and status mapping; verify exact payload schema and endpoint status codes from controllers before implementing new UI/API behavior.
- Reservation validation/not-found/conflict rules are traceable to BR-6–BR-12.
- Technician and inventory validation rules not found in current evidence remain NEEDS-EVIDENCE; do not infer them from UI behavior alone.

## Data and consistency
- Reservation operations that mutate stock and reservation state must use a single transactional unit where the persistence engine supports it. Validate rollback and retry behavior using tests; do not claim concurrency guarantees until proven.
- Maintain the parts table non-negative quantity CHECK constraint.
- Preserve nullable technician profile fields as recorded; no schema evolution is implied by this architecture.
- A hard-delete of a referenced part or technician is not specified; surface as a decision rather than bypassing foreign keys.

## Security, NFR, and operations
- Entra ID authentication is the approved direction; concrete app registration values are deployment configuration, not repository defaults.
- Authorization policy is an implementation blocker until roles/scopes and least-privilege rules are agreed.
- The current locked NFR and compliance baselines are placeholders/unknown; no throughput, availability, or cost target is invented here.
- Use local-file SQLite only; backup, retention, and multi-process deployment constraints need operational confirmation before production use.

## Open decisions / implementation gates
1. Define Entra role/scope mapping and read/write permissions for features 4–6.
2. Confirm exact API request/response bodies and status codes against current controllers, including delete of referenced inventory records and technician state endpoints.
3. Verify transaction boundaries and race behavior for reservation vs. cancellation under SQLite.
4. Resolve open validation rules for part maintenance and technician profile fields.
5. Confirm supported search/filter and pagination semantics for technicians and parts.
6. Measure or explicitly accept the locked NFR/security/compliance placeholders before any production readiness assertion.

## Evidence traceability
- Features and endpoint inventories: [current-state feature catalog](../../../pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md#L37-L60).
- Reservation business rules: [business rules BR-6–BR-12](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L35-L68).
- Technician state changes: [business rules BR-13–BR-14](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L69-L80).
- SQLite tables, relationships, and technician fields: [persistence entities](../../../pipeline/repo-understanding/serviceforge-system-overview/persistence-entities.md#L21-L43).
- Target decisions: `ServiceForge-Migrated/pipeline/decisions/adr-0001-target-tech-stack.md`, `adr-0002-auth-azure-entra.md`, `adr-0003-persistence-sqlite-local-only.md`.
