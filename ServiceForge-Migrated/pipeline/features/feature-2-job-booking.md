# Feature 2 — Job booking (create job) (React + Java 21)

Status: Draft (forward-engineered)
Last updated: 2026-10-14

## Problem statement
Dispatchers need to book a job for a technician at a specific time for a customer, while preventing schedule conflicts (including travel buffer).

## In-scope
- Create a job for a technician with customer name and start/end time.
- Enforce booking validation and conflict detection with 45-minute travel buffer.
- Return created job id and location.

## Out-of-scope
- Editing/rescheduling existing jobs (NEEDS-EVIDENCE)
- Cancelling jobs (NEEDS-EVIDENCE)
- Assigning parts during booking (handled by parts reservation feature)

## Users and permissions
- Dispatcher can create jobs.

NEEDS-EVIDENCE:
- Role names and authorization rules.

## UI flows (React)
### Flow A: Create job
1. User opens “Book Job”.
2. User selects technician.
3. User enters `customerName`.
4. User selects start and end time.
5. User submits.
6. UI shows success toast and navigates to technician schedule.

### Flow B: Conflict
- If API returns 409 conflict, UI shows conflict message and suggests choosing another time.

### Flow C: Validation
- If API returns 400, UI highlights invalid fields.

## API contracts (target stack)
Base URL: `/api`

### POST /api/jobs
Request body `CreateJobRequest`:
- `technicianId: number`
- `customerName: string`
- `startTime: string` (ISO-8601)
- `endTime: string` (ISO-8601)

Responses:
- 201 Created
  - `Location: /api/jobs/{jobId}`
  - Body: `Job`
- 400 Bad Request: `ApiError`
- 404 Not Found: `ApiError` (technician not found)
- 409 Conflict: `ApiError` (overlap conflict)
- 401/403: auth errors

`Job`:
- `id: number`
- `technicianId: number`
- `customerName: string`
- `startTime: string`
- `endTime: string`

## Data model (SQLite)
Baseline tables/columns (evidenced):
- `jobs(id, technician_id, customer_name, start_time, end_time)`
- `technicians(id, ...)`

No new tables required.

## Business rules (ported)
- BR-1: booking requires existing technician → 404.
- BR-2: `customerName` non-empty → 400.
- BR-3: valid time range (`endTime` after `startTime`) → 400.
- BR-4: conflict detection includes 45-minute travel buffer.
- BR-5: overlap predicate: `existing.start < expandedEnd` AND `existing.end > expandedStart`.
- BR-14: error mapping (400/404/409).

## Acceptance criteria
### Positive
AC-1: Dispatcher can create a job for an existing technician with valid customer name and time range.
AC-2: API returns 201 with Location header and created job payload.

### Negative
AC-3: If `technicianId` does not exist, API returns 404.
AC-4: If `customerName` is blank, API returns 400.
AC-5: If `endTime` is not after `startTime`, API returns 400.
AC-6: If requested time overlaps an existing job when applying the 45-minute travel buffer, API returns 409.
AC-7: If user is unauthenticated, API returns 401.

### Boundary
AC-8: If requested job starts exactly at `existing.endTime + 45m`, booking succeeds (no overlap).
AC-9: If requested job starts at `existing.endTime + 44m 59s`, booking fails with 409.

NEEDS-EVIDENCE:
- Time precision (minutes vs seconds) and timezone handling are not specified in repo-understanding.

## Definition of Done (DoD)
- React booking form implemented with validation and conflict display.
- Backend endpoint implemented in Java 21 with Entra token validation.
- Conflict detection uses 45-minute buffer.
- SQLite persistence writes job row.
- Unit tests cover validation and overlap boundaries.

## Traceability (AC → repo-understanding evidence)
| AC | Evidence |
|---|---|
| AC-1 | pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md (POST /api/jobs exists) |
| AC-2 | pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md (201 Created + Location header) |
| AC-3 | pipeline/repo-understanding/serviceforge-system-overview/business-rules.md (BR-1) |
| AC-4 | pipeline/repo-understanding/serviceforge-system-overview/business-rules.md (BR-2) |
| AC-5 | pipeline/repo-understanding/serviceforge-system-overview/business-rules.md (BR-3) |
| AC-6 | pipeline/repo-understanding/serviceforge-system-overview/business-rules.md (BR-4, BR-5) |
| AC-7 | ServiceForge-Migrated/pipeline/decisions/adr-0002-auth-azure-entra.md (forward-looking auth decision) |
| AC-8 | pipeline/repo-understanding/serviceforge-system-overview/business-rules.md (BR-4, BR-5) + NEEDS-EVIDENCE (time precision) |
| AC-9 | pipeline/repo-understanding/serviceforge-system-overview/business-rules.md (BR-4, BR-5) + NEEDS-EVIDENCE (time precision) |
