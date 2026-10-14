# Feature 1 — Technician availability calendar / scheduling view (React + Java 21)

Status: Draft (forward-engineered)
Last updated: 2026-10-14

## Problem statement
Dispatchers need to view a technician’s scheduled jobs over a time window to understand availability and plan assignments.

## Evidence-first scope note
This feature is confirmed in the current system via routing and API evidence. The migrated spec preserves the same business intent while expressing UI flows in React and APIs in Java 21.

## In-scope
- View a technician’s scheduled jobs in a calendar/list timeline for a selected date range.
- Filter by technician.
- Backend API to list jobs for a technician with optional `from`/`to` query parameters.

## Out-of-scope
- Drag-and-drop rescheduling (NEEDS-EVIDENCE)
- Route optimization / travel time estimation beyond the existing booking buffer rule (NEEDS-EVIDENCE)
- Multi-technician comparison view (NEEDS-EVIDENCE)

## Users and permissions
- Dispatcher user can view technician schedules.
- Technician user can view their own schedule.

NEEDS-EVIDENCE:
- Exact role names and authorization rules are not evidenced in repo-understanding.

## UI flows (React)
### Flow A: View schedule
1. User navigates to “Technician Availability”.
2. User selects a technician from a dropdown.
3. User selects a date range (defaults to current week).
4. UI loads jobs and renders them in a calendar (week/day) and/or list view.
5. UI shows empty state when no jobs exist in range.

### Flow B: Error states
- If API returns 401: show sign-in prompt.
- If API returns 403: show “not authorized”.
- If API returns 404 for technician: show “technician not found”.

## API contracts (target stack)
Base URL: `/api`

### GET /api/technicians
Purpose: populate technician selector.
- Response 200: `TechnicianSummary[]`

`TechnicianSummary`:
- `id: number`
- `name: string`
- `active: boolean` (if included in list; see technician management feature)

### GET /api/technicians/{technicianId}/jobs?from={iso}&to={iso}
Purpose: list jobs for a technician in a time window.
- Query params:
  - `from` (optional, ISO-8601 datetime)
  - `to` (optional, ISO-8601 datetime)
- Response 200: `JobSummary[]`
- Response 404: `ApiError` (technician not found)
- Response 401/403: auth errors

`JobSummary`:
- `id: number`
- `technicianId: number`
- `customerName: string`
- `startTime: string` (ISO-8601)
- `endTime: string` (ISO-8601)

`ApiError` (shape aligned to current system intent; exact fields NEEDS-EVIDENCE):
- `message: string`
- `status: number`
- `timestamp: string`

## Data model (SQLite)
Baseline tables/columns (evidenced):
- `technicians(id, name, active, age, area_served, vehicle_*, skills_csv)`
- `jobs(id, technician_id, customer_name, start_time, end_time)`

No new tables required.

## Business rules (ported)
- BR-14: API error mapping patterns exist in current system (400/404/409). Auth adds 401/403.

NEEDS-EVIDENCE:
- Any additional availability rules (working hours, PTO, holidays) are not evidenced.

## Acceptance criteria
### Positive
AC-1: User can list technicians to select a technician.
AC-2: User can view the selected technician’s jobs for a given date range.
AC-3: When no jobs exist in the range, UI shows an empty state.

### Negative
AC-4: If technician does not exist, API returns 404 and UI shows “technician not found”.
AC-5: If user is not authenticated, API returns 401 and UI prompts sign-in.
AC-6: If user lacks permission, API returns 403 and UI shows “not authorized”.

### Boundary
AC-7: If `from`/`to` are omitted, API returns jobs using a server-defined default window (NEEDS-EVIDENCE: default behavior not specified in repo-understanding).
AC-8: If `from` is after `to`, API returns 400 with `ApiError`.

## Definition of Done (DoD)
- React screen implemented with:
  - technician selector
  - date range picker
  - schedule rendering (calendar or list)
  - loading/empty/error states
- Backend endpoints implemented in Java 21 with token validation (Entra) and consistent error responses.
- SQLite schema unchanged.
- Unit tests for service/controller logic.
- API contract documented (OpenAPI or equivalent) (NEEDS-EVIDENCE: repo standard not captured).

## Traceability (AC → repo-understanding evidence)
| AC | Evidence |
|---|---|
| AC-1 | pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md (Technician Availability: UI route + API client) |
| AC-2 | pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md (Backend supports listing technician jobs with from/to) |
| AC-3 | pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md (UI exists; empty state behavior NEEDS-EVIDENCE) |
| AC-4 | pipeline/repo-understanding/serviceforge-system-overview/business-rules.md (BR-14 error mapping; NotFound → 404) |
| AC-5 | ServiceForge-Migrated/pipeline/decisions/adr-0002-auth-azure-entra.md (forward-looking auth decision) |
| AC-6 | ServiceForge-Migrated/pipeline/decisions/adr-0002-auth-azure-entra.md (forward-looking auth decision) |
| AC-7 | NEEDS-EVIDENCE (default window behavior not captured) |
| AC-8 | pipeline/repo-understanding/serviceforge-system-overview/business-rules.md (BR-14 validation → 400) |
