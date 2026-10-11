# Feature 9 — Technician onboarding and management

Architecture Design

Source spec: pipeline/features/feature-9-technician-onboarding-and-management.md

## 1) High-level component diagram (textual)

- Frontend (Angular)
  - `TechnicianManagementPageComponent`
    - Technician list table (search + status filter)
    - Actions: Add, Edit, Deactivate/Reactivate
  - `TechnicianFormComponent` (create/edit form)
    - Fields: name, age, area served, vehicle details, skills
  - `TechnicianApiService` (Angular service)
    - Encapsulates all HTTP calls; components do not call HTTP directly
  - Models
    - `Technician` UI model aligned to backend DTO

- Backend (Spring Boot)
  - Controller layer
    - `TechnicianManagementController` (or extend existing `TechnicianController`)
      - CRUD-ish endpoints for technicians
  - Service layer
    - `TechnicianManagementService`
      - Validation, create/update, activate/deactivate
  - Data layer
    - If repo is still in-memory for technicians: extend `MockDataStore`
    - If repo has moved to SQLite persistence (project decision 2026-10-05): implement repository + migrations

## 2) Data model approach (migration-safe)

### Option A (preferred if keeping scope minimal): Additive fields on existing `Technician`
- Add fields:
  - `active: boolean`
  - `age: Integer`
  - `areaServed: String`
  - `vehicleDetails: VehicleDetails` (nested object)
  - `skills: List<String>`

Pros: minimal disruption; easiest to keep Feature 1 stable.
Cons: `skills` as free-text may drift.

### Option B (if a skills catalog is desired): Introduce `Skill` model + references
- Add `Skill` catalog (seeded)
- Technician stores `skillIds: List<String>`

Pros: consistent skill names.
Cons: adds extra endpoints/UI; risk of scope creep.

Decision for Feature 9: **Option A** (skills as list of strings) to avoid inventing scope beyond intent.

## 3) REST API design (proposed)

Keep existing `GET /api/technicians` stable.

### Endpoints
- `GET /api/technicians`
  - Returns list of technicians including new fields (additive)
  - Query params (optional):
    - `q` (name contains)
    - `status=active|inactive|all` (default: all)

- `POST /api/technicians`
  - Creates technician
  - Request body: `TechnicianUpsertRequest`
  - Response: `TechnicianDto` (201)

- `GET /api/technicians/{id}`
  - Response: `TechnicianDto` (200) or `ApiError` (404)

- `PUT /api/technicians/{id}`
  - Updates technician (full replace semantics for managed fields)
  - Response: `TechnicianDto` (200) or `ApiError` (400/404)

- `POST /api/technicians/{id}/deactivate`
  - Response: updated `TechnicianDto` (200) or `ApiError` (404)

- `POST /api/technicians/{id}/reactivate`
  - Response: updated `TechnicianDto` (200) or `ApiError` (404)

### DTOs
- `TechnicianDto`
  - `id`, `name`, `active`, `age`, `areaServed`, `vehicleDetails`, `skills`
- `VehicleDetailsDto`
  - `type`, `make`, `model`, `licensePlate`
- `TechnicianUpsertRequest`
  - `name`, `age`, `areaServed`, `vehicleDetails`, `skills`

### Validation rules (server-side)
- `name`: required, trimmed, length 1..100
- `age`: required, integer, bounds 16..80 (assumption; can be adjusted)
- `areaServed`: required, trimmed, length 1..120
- `skills`: optional; if present, each entry trimmed, length 1..50; max 50 skills
- `vehicleDetails`: optional; if present, each field length-limited

## 4) Persistence strategy

Project decision indicates SQLite is allowed. However, Feature 9 intent does not require persistence beyond existing patterns.

Recommended approach:
- If technicians are already persisted in SQLite in current codebase: implement via repository + migration adding new columns.
- If technicians are still in `MockDataStore`: implement in-memory mutations with deterministic seeding.

Either way, keep API contract stable.

## 5) UI flow

- Route: `/technicians/manage` (or similar; final route per existing navigation conventions)
- List view:
  - Search box + status filter
  - Table with actions per row
- Create:
  - Modal dialog or dedicated page (choose based on existing UI patterns)
- Edit:
  - From row action; prefill form
- Deactivate/reactivate:
  - Confirmation prompt; update status in list

## 6) Migration-safety concerns

Because `Technician` is already used by Feature 1:
- Additive fields only; do not rename/remove existing fields.
- Update all seeded technicians to include defaults:
  - `active=true`
  - `age` default (e.g., 30) if required; or make nullable and enforce required only on create/update.
  - `areaServed` default (e.g., "Unknown") if required.
  - `skills=[]`, `vehicleDetails=null`

Risk: making `age` required may break existing seeds and UI.
Mitigation: allow null for existing records but require on create/update; or backfill seeds.

## 7) Risks and mitigations

- Risk: breaking existing UI that expects a smaller technician payload.
  - Mitigation: additive JSON fields are safe; ensure frontend models tolerate extra fields.
- Risk: ambiguity of age bounds.
  - Mitigation: document bounds in spec; keep bounds configurable if needed.
- Risk: persistence mismatch (MockDataStore vs SQLite).
  - Mitigation: implementation plan includes Phase 0 discovery to confirm current persistence.

## Handoff update (for Design Review Agent)
- Status: Architecture Complete
- Owner: Design Review Agent
- Artifacts:
  - pipeline/features/feature-9-technician-onboarding-and-management.md
  - pipeline/architecture/feature-9-architecture.md
