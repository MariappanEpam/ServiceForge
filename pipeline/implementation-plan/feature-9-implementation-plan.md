# Implementation Plan — Feature 9: Technician onboarding and management

Source artifacts:
- Spec: pipeline/features/feature-9-technician-onboarding-and-management.md
- Architecture: pipeline/architecture/feature-9-architecture.md
- Design review: pipeline/reviews/feature-9-review.md

## Phase 0 — Discovery (required)

1. Confirm current technician persistence approach:
   - Is `Technician` still sourced from `MockDataStore`, or has it moved to SQLite per project decision?
2. Locate existing technician API/controller:
   - Confirm current `GET /api/technicians` response shape and DTO usage.
3. Identify all consumers of `Technician` fields in frontend and backend.

Exit criteria:
- Documented decision: MockDataStore path vs SQLite path.
- List of impacted files/components.

## Phase 1 — Backend API (no UI yet)

### 1.1 Model/DTO updates (migration-safe)
- Add additive fields to technician representation:
  - `active`, `age`, `areaServed`, `vehicleDetails`, `skills`
- Ensure defaults for existing seeded technicians.

### 1.2 Endpoints
Implement endpoints per architecture:
- `POST /api/technicians`
- `GET /api/technicians/{id}`
- `PUT /api/technicians/{id}`
- `POST /api/technicians/{id}/deactivate`
- `POST /api/technicians/{id}/reactivate`

### 1.3 Validation + error handling
- Enforce validation rules; return `ApiError` on 400/404.

### 1.4 Tests
- Controller/service tests:
  - create success
  - create validation failures
  - update success
  - update not-found
  - deactivate/reactivate

## Phase 2 — Frontend UI

### 2.1 Routing + page scaffold
- Add Technician Management page route.

### 2.2 Technician list
- Table/grid with:
  - search by name
  - status filter
  - actions: add/edit/deactivate/reactivate

### 2.3 Technician form
- Create/edit form with validation.
- Skills add/remove UI.

### 2.4 Frontend tests
- Component tests for:
  - validation messages
  - list filtering
  - deactivate/reactivate UI state

## Phase 3 — Regression + documentation

1. Regression test Feature 1 flows:
   - `GET /api/technicians` still works
   - technician availability calendar screens still load
2. Update any seed/demo documentation if present.

## Deliverables checklist
- Backend endpoints implemented and tested.
- Frontend page implemented and tested.
- No breaking changes to existing features.
- Migration-safety steps completed (seed updates + consumer grep).
