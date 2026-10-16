# Test execution — Feature 9 (Migrated app)

Date: 2026-10-14
Scope: ServiceForge-Migrated (React + Spring Boot)

## Environment
- OS: Windows
- Backend: Spring Boot (Java 21)
- Frontend: React + Vite
- Notes: Port 8080 was already in use locally; backend was started on 8081 for smoke.

## Test runs (evidence)

### Backend — Maven tests
Command:
- `mvn -q test`

Result:
- PASS
- Suites observed in `backend/target/surefire-reports/`:
  - `com.serviceforge.migrated.service.JobBookingServiceTest` — tests=2, failures=0, errors=0, skipped=0

Notes:
- Mockito/ByteBuddy emits JDK agent warnings on Java 21; tests still pass.

### Frontend — Build
Command:
- `npm run build`

Result:
- PASS

### Frontend — Lint
Command:
- `npm run lint`

Result:
- PASS with warnings
- Warnings are `react(set-state-in-effect)` and `react-hooks(exhaustive-deps)` in existing pages; no new blocking issues.

### Security baseline — Secrets scan
Command:
- `powershell -File tools/secrets-scan/scan-working-tree.ps1`

Result:
- PASS (no secrets detected)

## Manual API smoke (Feature 9)
Backend started on `http://localhost:8081` with `-Dserviceforge.security.enabled=false`.

- POST `/api/technicians` with JSON body (name/age/areaServed/vehicle/skills)
  - Result: 201/200 with created technician payload (verified via PowerShell `Invoke-RestMethod`).

## Summary
Overall status: PASS (with non-blocking lint warnings).
