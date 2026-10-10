# Test Report — Feature 8: Job ↔ Parts Mapping & UI Control Conditions

| Field | Value |
|---|---|
| Feature | 8 |
| Date | 2026-10-08 |
| Overall result | PASS |

## Evidence
- Backend (JUnit): `mvn test -Dtest=JobPartsReservationControllerFeature8Test` → PASS
- Frontend (Angular): `npm test` → PASS (6/6)
- UI regression (Playwright/.NET): TRX + HTML
  - TRX: test-automation/src/ServiceForge.Tests.Api/TestResults/feature8-ui-regression.trx
  - HTML: test-execution/feature-8-job-parts-mapping-and-ui-conditions/reports/feature8.html

## Notes
- Job status gating is currently a no-op because the `Job` model has no status field; a dedicated hook (`assertJobAllowsReservation`) was added in backend service for future enforcement.
- UI tests use `http://localhost:4200` (not `127.0.0.1`) to avoid proxy inconsistencies.
