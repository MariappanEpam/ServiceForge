# Test Plan — Feature 2: Parts Reservation

## References
- Spec: [pipeline/features/feature-2-parts-reservation.md](../../features/feature-2-parts-reservation.md)
- Architecture: [pipeline/architecture/feature-2-architecture.md](../../architecture/feature-2-architecture.md)
- Implementation plan: [pipeline/implementation-plan/feature-2-implementation-plan.md](../../implementation-plan/feature-2-implementation-plan.md)

## Scope

### In-scope
- Parts catalog listing (GET /api/parts)
- Job parts reservations:
  - list (GET /api/jobs/{jobId}/parts-reservations)
  - create (POST /api/jobs/{jobId}/parts-reservations)
  - cancel (DELETE /api/jobs/{jobId}/parts-reservations/{reservationId})

### Out of scope
- UI automation (until Feature 2 UI exists)
- ReportPortal publishing (until configured)

## Automated suites

### API automation (C# Playwright)
- `ListParts_ShouldReturn200_AndNonEmptyArray`
- `CreateAndCancelJobPartsReservation_ShouldSucceed_WhenStockAvailable`

### UI automation (C# Playwright + NUnit)
- `ReservePart_ShouldCreateReservation_WhenValidInput`
  - Happy path: open the app, navigate to the Parts panel, reserve 1 unit of the first available SKU for seeded `jobId=1`, and verify the reservation appears in the table.
  - Preconditions:
    - Frontend running on `SERVICEFORGE_UI_BASE_URL` (default http://localhost:4200)
    - Backend running on `SERVICEFORGE_API_BASE_URL` (default http://localhost:8080)
  - Notes:
    - Uses `data-testid` selectors on the Parts panel for stability.

## Execution readiness
- Backend running on `SERVICEFORGE_API_BASE_URL` (default http://localhost:8080)
- Playwright browsers installed

## Evidence
- Execution log: [test-execution/feature-2-parts-reservation/run-2026-10-07.md](../../../test-execution/feature-2-parts-reservation/run-2026-10-07.md)
