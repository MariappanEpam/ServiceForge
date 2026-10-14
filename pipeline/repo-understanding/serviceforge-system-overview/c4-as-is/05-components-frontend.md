# C4 As-Is — 05 Components (frontend)

Scope: frontend/
Status: populated
Updated: 2026-10-14

## App structure

### Routes
- Default route: `TechnicianAvailabilityComponent` (confirmed)
	- Evidence: frontend/src/app/app.routes.ts
- Technician management page route: `/technicians/manage` (confirmed)
	- Evidence: frontend/src/app/app.routes.ts
- Parts reservations page route: `/parts/reservations` (confirmed)
	- Evidence: frontend/src/app/app.routes.ts
- Parts management route: `/parts/manage` (confirmed)
	- Evidence: frontend/src/app/app.routes.ts

### Services (HTTP API clients)
- `JobsApiService` (confirmed)
	- Evidence: frontend/src/app/services/jobs-api.service.ts
- `TechniciansApiService` (confirmed)
	- Evidence: frontend/src/app/services/technicians-api.service.ts
- `PartsApiService` (confirmed)
	- Evidence: frontend/src/app/services/parts-api.service.ts
- `PartsReservationsApiService` (confirmed)
	- Evidence: frontend/src/app/services/parts-reservations-api.service.ts

## Key components

### Pages / screens
- `TechnicianAvailabilityComponent` (confirmed)
	- Evidence: frontend/src/app/app.routes.ts
- `TechniciansManagementPageComponent` (confirmed)
	- Evidence: frontend/src/app/app.routes.ts
- `PartsReservationsPageComponent` (confirmed)
	- Evidence: frontend/src/app/app.routes.ts
- `PartsManagementComponent` (confirmed)
	- Evidence: frontend/src/app/app.routes.ts

## Provenance
- frontend/src/app/app.routes.ts
- frontend/src/app/services/jobs-api.service.ts
- frontend/src/app/services/technicians-api.service.ts
- frontend/src/app/services/parts-api.service.ts
- frontend/src/app/services/parts-reservations-api.service.ts
