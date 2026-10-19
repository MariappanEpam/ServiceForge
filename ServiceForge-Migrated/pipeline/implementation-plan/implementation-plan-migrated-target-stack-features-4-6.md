# Implementation plan — ServiceForge-Migrated Features 4–6

Date: 2026-10-16  
Status: Planned with prerequisites; implementation is not authorized until Gate 0 is signed off

## Scope
- Feature 4: Job parts reservation.
- Feature 5: Parts inventory management.
- Feature 6: Technician onboarding and management.
- Target stack and persistence/security constraints follow ADRs 0001–0003.

## Gate 0 — decisions and contract verification (must complete first)
1. Approve Entra roles/scopes and the read/write authorization matrix for all three features.
2. Verify existing controller request/response schemas, status codes, and exact route methods; document divergences from the draft specs.
3. Test reservation transaction atomicity, rollback, concurrency, and idempotent cancellation semantics.
4. Decide/verify delete behavior for referenced parts; verify technician soft-state transitions and any profile validation constraints.
5. Confirm search/filter/pagination behavior and identify gaps as explicit decisions.
6. Record the relevant locked NFR/compliance baseline status and any accepted exceptions.

**Exit:** decision owners approve all security, contract, and transaction findings in the feature handoff. No code changes before exit.

## Phase 1 — persistence and shared API contract
- Confirm no new tables/columns are required; retain local SQLite and versioned migrations.
- Lock the `ApiError` response shape and consistent 400/404/409 mapping.
- Add/adjust migration only if Gate 0 evidence establishes a need; require backward-compatible review.
- Add service/repository tests for inventory invariants and reservation consistency.

## Phase 2 — Feature 4: job parts reservation
- Implement or verify job reservation list/create/cancel endpoints and typed DTOs.
- Enforce positive quantity, existing job, active/orderable part, sufficient stock, non-negative stock, cancel conflict, and one-time restoration.
- Build React reservation UI with list, create, cancel, loading, success, and error handling.
- Verify no double-restock on repeat requests and rollback if reservation persistence fails.

## Phase 3 — Feature 5: parts inventory management
- Implement or verify managed parts list/get/create/update/delete endpoints using approved contracts.
- Build React management screen and API service.
- Preserve the non-negative stock constraint; test SKU uniqueness, missing SKU, invalid quantity, and referenced-part deletion policy.
- Do not introduce supplier/replenishment scope.

## Phase 4 — Feature 6: technician onboarding/management
- Implement or verify technician list/get/create/update/deactivate/reactivate contracts.
- Build React management screen and API service.
- Preserve evidenced profile fields and active status; test state transitions, missing IDs, validated fields, search/filter behavior, and authorization.
- Do not introduce identity provisioning or hard-delete semantics without approval.

## Phase 5 — integration and release evidence
- Run backend unit/integration tests and frontend build/lint plus available UI/API automation.
- Run cross-feature regression around scheduling/booking and technician active status, inventory decrement/restoration, and parts catalog visibility.
- Record security and NFR evidence, limitations, and configuration prerequisites.
- Update per-feature handoffs and final readiness summary.

## Dependencies and risks
- Gate 0 decisions block all protected CRUD and reservation implementation.
- Feature 4 depends on job booking, parts catalog, and inventory repositories already functioning.
- Feature 6 can affect scheduling/booking; preserve existing consumers and validate active/inactive filtering before changing model fields.
- SQLite is local-only by decision; multi-instance scaling is out of scope absent a new approved ADR.

## Acceptance gates
- No acceptance criterion added without traceable evidence or explicit forward decision.
- All business rules BR-6–BR-13 pass regression tests.
- No source changes until Gate 0 is approved.
- All evidence and handoffs are stored within the migrated pipeline artifact directories.

## Traceability
- Features 4–6 and APIs: [current-state feature catalog](../../../pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md#L37-L60).
- Reservation and inventory rules: [business rules BR-6–BR-12](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L35-L68).
- Technician state/error behavior: [business rules BR-13–BR-14](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L69-L80).
- Data structures: [persistence entities](../../../pipeline/repo-understanding/serviceforge-system-overview/persistence-entities.md#L21-L43).
- Architecture: [Features 4–6 target architecture](../architecture-design/architecture-migrated-target-stack-features-4-6.md).
- Review: [Features 4–6 architecture review](../design-review-logs/features-4-6-target-stack-review.md).
