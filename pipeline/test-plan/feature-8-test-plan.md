# Test Plan — Feature 8: Job ↔ Parts Mapping & UI Control Conditions

| Field | Value |
|---|---|
| Feature | 8 |
| Spec | pipeline/features/feature-8-job-parts-mapping-and-ui-conditions.md |
| Implementation plan | pipeline/implementation-plan/feature-8-implementation-plan.md |
| Date | 2026-10-08 |

## Scope
Validate Feature 8 alignment requirements:
- Job-scoped parts reservation endpoints behave correctly (400/404 semantics).
- UI control conditions do not regress existing parts flows.

## Backend tests (Spring Boot)
1. Create reservation missing `sku` → 400
2. Create reservation unknown job → 404
3. List reservations unknown job → 404
4. List reservations returns JSON array (job-scoped endpoint is invocable)

Evidence:
- JUnit: `JobPartsReservationControllerFeature8Test`

## Frontend tests (Angular)
1. Existing unit tests pass after UI control-condition changes.

Evidence:
- `npm test` (Karma) run

## End-to-end / UI regression (Playwright)
1. Parts management UI flow still works (create/edit/delete) after UI control-condition changes.

Evidence:
- .NET Playwright suite filtered by `FullyQualifiedName~Feature7PartsInventory` (used as Feature 8 UI regression)

## Definition of Done mapping
- Automated tests exist for backend + frontend.
- Execution evidence captured as TRX + HTML report.
