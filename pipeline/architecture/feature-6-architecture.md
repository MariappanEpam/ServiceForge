# Feature 6 — Architecture (Restore Technician Availability Calendar, SQLite-backed)

## Goal
Restore Feature 1 booking + schedule endpoints and UI, backed by SQLite persistence.

## Constraints / Repo conventions
- Java 17, Spring Boot 3
- Local-file SQLite only
- Flyway migrations for schema changes
- Backend layering: `controller` → `service` → `data`
- Constructor injection only
- REST errors use `ApiError` with `ResponseEntity`

## Data
### Tables
- `technicians`
- `jobs`

If `jobs` lacks `start_time` / `end_time`, add Flyway migration `V2__jobs_schedule_fields.sql` (or next version).

### Indexes
- `jobs(technician_id, start_time, end_time)` for schedule queries and overlap checks.

## Backend components
### Controllers
- `TechnicianController`
  - `GET /api/technicians`
  - `GET /api/technicians/{id}/jobs?from&to`
- `JobController`
  - `POST /api/jobs`

### Services
- `TechnicianService`
  - list technicians
  - list jobs for technician
- `JobBookingService`
  - validate request
  - enforce overlap rule with travel buffer
  - create job

### Data layer (JdbcTemplate)
- `JdbcTechnicianRepository`
  - `listAll()`
  - `existsById(long id)`
- `JdbcJobRepository`
  - `listByTechnician(long technicianId, Optional<LocalDateTime> from, Optional<LocalDateTime> to)`
  - `insert(Job)`
  - `existsOverlapping(long technicianId, LocalDateTime start, LocalDateTime end, Duration buffer)`

## Overlap rule implementation
- Travel buffer: 45 minutes (from Feature 1 decisions)
- Compute expanded window:
  - `expandedStart = start.minusMinutes(45)`
  - `expandedEnd = end.plusMinutes(45)`
- Overlap query (SQLite):
  - conflict if `existing.start_time < expandedEnd AND existing.end_time > expandedStart`

Return `409 Conflict` with an `ApiError` message including the conflicting window.

## Frontend
- Reintroduce/restore Feature 1 calendar component(s)
- Angular service `technicians-api.service.ts` and `jobs-api.service.ts`
- Components call services only; no HTTP in components

## Migration / compatibility
- Do not break Feature 4/5 endpoints.
- If schema changes are required, add a new Flyway migration and update seeding accordingly.

## Observability
- Log booking attempts and conflicts at INFO.

