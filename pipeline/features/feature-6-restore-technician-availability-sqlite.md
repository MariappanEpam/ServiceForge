# Feature 6 — Restore Technician Availability Calendar (SQLite-backed)

## Intent
Re-introduce **Feature 1 — Technician Availability Calendar** API + UI wiring into the current codebase after the backend was intentionally trimmed down during SQLite persistence work.

This feature restores the ability to **book jobs for technicians** and **view technician schedules**, using the **SQLite database as the source of truth** (no in-memory mock store).

## Background / Why this exists
- The repository originally shipped with Feature 1 as the worked reference.
- During Feature 5 (SQLite persistence) and subsequent refactors, the Feature 1 booking endpoints/controllers were removed/are no longer reachable.
- The user intentionally cleared Feature 1 to validate the repo’s BA→Dev→Test pipeline.

## Scope
### In scope
1. Restore backend endpoints for technician availability and job booking.
2. Persist and read all technician/job schedule data from SQLite.
3. Enforce the **no-overlap booking rule** including the **travel buffer**.
4. Wire the Angular UI to the restored endpoints (no mock data).

### Out of scope
- Advanced rescheduling, cancellation workflows, or dispatcher optimization.
- Multi-day jobs, time zones beyond local server time.
- External calendar integrations.

## Key rules
### No-overlap booking (must enforce)
A technician cannot have overlapping jobs.

Additionally, apply the **travel buffer** from Feature 1 decisions:
- Travel buffer: **45 minutes**
- A new job conflicts if its time window overlaps any existing job window expanded by ±45 minutes.

References:
- Travel buffer decision: [pipeline/decisions/feature-1-decisions.md](../decisions/feature-1-decisions.md)
- Standing rule: [pipeline/rules/no-overlap-booking.md](../rules/no-overlap-booking.md)

## Data model (SQLite)
This feature builds on the SQLite persistence foundation (Feature 5).

### Tables
- `technicians`
- `jobs`

If the current schema does not include the required columns for Feature 1 (start/end timestamps), add a migration.

### Required job fields
- `id` (stable numeric id)
- `technician_id` (FK)
- `customer_name`
- `start_time` (local datetime)
- `end_time` (local datetime)

## Backend API
### 1) List technicians
`GET /api/technicians`

Response: `200 OK`
```json
[
  { "id": 1, "name": "Tech One" },
  { "id": 2, "name": "Tech Two" }
]
```

### 2) List jobs for a technician
`GET /api/technicians/{technicianId}/jobs`

Query params (optional):
- `from` (inclusive local datetime)
- `to` (exclusive local datetime)

Response: `200 OK`
```json
[
  {
    "id": 123,
    "technicianId": 2,
    "customerName": "Acme Corp",
    "startTime": "2026-09-26T10:00:00",
    "endTime": "2026-09-26T11:00:00"
  }
]
```

### 3) Book a job
`POST /api/jobs`

Request:
```json
{
  "technicianId": 2,
  "customerName": "Acme Corp",
  "startTime": "2026-09-26T10:00:00",
  "endTime": "2026-09-26T11:00:00"
}
```

Responses:
- `201 Created` with created job
- `400 Bad Request` with `ApiError` for validation errors
- `404 Not Found` with `ApiError` if technician does not exist
- `409 Conflict` with `ApiError` if overlaps existing job (including travel buffer)

## Frontend (Angular)
### Screens/Components
- Restore/implement the Technician Availability Calendar screen (as per Feature 1 reference).
- UI must call backend endpoints (no mock data).

### UX requirements
- Select technician
- View existing jobs in a calendar/list
- Create a booking
- Show conflict errors (409) clearly

## Seed behavior
- Baseline seed must include at least 2 technicians.
- Jobs may be empty by default.
- Seed must be explicit and deterministic.

## Definition of Done
### Backend
- Endpoints exist and respond:
  - `GET /api/technicians`
  - `GET /api/technicians/{id}/jobs`
  - `POST /api/jobs`
- All reads/writes use SQLite.
- Overlap rule enforced with 45-minute travel buffer.
- Error semantics: 400/404/409 return `ApiError`.

### Frontend
- Calendar UI loads technicians and jobs from backend.
- Booking creates a job via `POST /api/jobs`.
- Conflict is displayed.

### Tests
- Unit/integration tests cover:
  - booking success
  - booking conflict (overlap + travel buffer)
  - technician not found
  - list jobs by technician

### Smoke checks
- Start backend + frontend locally.
- Book a job for technician 2 at `2026-09-26T10:00` for 60 minutes for `Acme Corp`.
- Verify it appears in the technician’s job list.
