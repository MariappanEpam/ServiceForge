# Feature 9 — Test Plan (Technician onboarding and management)

Date: 2026-10-11

## Scope
Validate Feature 9 technician onboarding and management:
- Master page to add/manage technicians
- Fields: name, active, age (16..80), area served, vehicle details, skills
- Backend CRUD + activate/deactivate endpoints

Out of scope:
- Authentication/authorization
- External DB services (only local SQLite)

## Test approach
- Backend: API-level integration tests (HTTP) + repository/service unit tests where feasible.
- Frontend: Build verification + manual UI smoke (Karma is unstable on this machine).

## Environments
- Backend: Spring Boot on `http://localhost:8081`
- Frontend: Angular dev server (dynamic port if 4200 is in use)

## Acceptance Criteria coverage matrix
Map to spec ACs in `pipeline/features/feature-9-technician-onboarding-and-management.md`.

### AC-9.1 List technicians
- Verify `GET /api/technicians` returns 200 and includes seeded technicians.

### AC-9.2 View technician details
- Verify `GET /api/technicians/{id}` returns 200 for existing id.
- Verify 404 + `ApiError` for missing id.

### AC-9.3 Create technician
- Verify `POST /api/technicians` returns 201 and echoes created technician.
- Verify optional fields accepted.

### AC-9.4 Update technician
- Verify `PUT /api/technicians/{id}` returns 200 and persists changes.
- Verify 404 for missing id.

### AC-9.5 Deactivate technician
- Verify `POST /api/technicians/{id}/deactivate` returns 204.
- Verify subsequent list/details show `active=false`.

### AC-9.6 Reactivate technician
- Verify `POST /api/technicians/{id}/reactivate` returns 204.
- Verify subsequent list/details show `active=true`.

### AC-9.7 Age validation
- Verify age < 16 returns 400 with validation message.
- Verify age > 80 returns 400 with validation message.
- Verify age 16 and 80 succeed.

### AC-9.8 Skills handling
- Verify skills round-trip as list of strings.

### AC-9.9 Vehicle details
- Verify vehicle fields round-trip.

### AC-9.10 UI workflow
- Manual smoke: list, create, edit, deactivate/reactivate from `/technicians/manage`.

## Evidence to capture
- Backend: command outputs + HTTP responses for key scenarios.
- Frontend: `npm run build` output.
- Manual UI: brief checklist with observed results.
