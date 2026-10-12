# Feature 6 — Implementation Plan (Restore Technician Availability Calendar, SQLite-backed)

## Goal
Restore Feature 1 booking + schedule endpoints and UI, backed by SQLite.

## Epic
### EPIC-6.1 Restore Technician Availability Calendar

## Stories & Tasks

### Story 6.1 — Database schema supports scheduled jobs
**Tasks**
1. Inspect current Flyway schema for `jobs` table.
2. If missing schedule fields, add migration:
   - add `start_time` (TEXT or DATETIME-compatible)
   - add `end_time`
   - add index on `(technician_id, start_time, end_time)`
3. Update `DbSeeder` baseline seed to include technicians and optionally sample jobs.

**Acceptance**
- App starts with Flyway migrations applied.
- Schema contains required fields.

### Story 6.2 — Backend: technician + job booking APIs restored
**Tasks**
1. Add `JdbcTechnicianRepository`.
2. Extend/add `JdbcJobRepository` for schedule listing + insert + overlap check.
3. Add `TechnicianService` and `JobBookingService`.
4. Add controllers:
   - `GET /api/technicians`
   - `GET /api/technicians/{id}/jobs`
   - `POST /api/jobs`
5. Implement overlap rule with 45-minute travel buffer.
6. Ensure error responses use `ApiError` and correct HTTP codes.

**Acceptance**
- Endpoints respond and use SQLite.
- Booking conflicts return 409.

### Story 6.3 — Frontend: calendar UI wired to backend
**Tasks**
1. Restore/implement Feature 1 calendar component(s) in Angular.
2. Add Angular services for technicians and jobs.
3. Replace any mock data usage with HTTP calls.
4. Display conflict errors.

**Acceptance**
- UI lists technicians and jobs.
- Booking creates jobs and refreshes view.

### Story 6.4 — Tests + smoke validation
**Tasks**
1. Backend tests:
   - booking success
   - booking conflict (overlap + buffer)
   - technician not found
   - list jobs
2. Frontend tests (basic component/service tests).
3. Smoke test script/manual steps:
   - Start backend + frontend
   - Book job for technician 2 at 2026-09-26T10:00 for 60 minutes for Acme Corp
   - Verify it appears

**Acceptance**
- `mvn test` passes.
- `npm test -- --watch=false` passes.
- Manual smoke steps pass.

## Delivery notes
- Do not remove Feature 4/5 endpoints.
- Keep SQLite-only rule.
