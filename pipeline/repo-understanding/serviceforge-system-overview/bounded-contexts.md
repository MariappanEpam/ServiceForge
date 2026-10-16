# Bounded contexts — ServiceForge (system overview)

Scope: Whole repository (ServiceForge-Start)
Status: populated
Updated: 2026-10-14

## Candidate bounded contexts

### Scheduling / Availability
- UI entry route defaults to technician availability screen (confirmed)
	- Evidence: frontend/src/app/app.routes.ts
- Backend supports listing a technician's jobs within a time window (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java
	- Evidence: backend/src/main/java/com/serviceforge/service/TechnicianService.java
	- Evidence: backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java

### Work Orders / Jobs
- Backend supports creating a job (booking) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/JobController.java
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java
- Jobs persisted in SQLite `jobs` table with schedule fields (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql
	- Evidence: backend/src/main/resources/db/migration/V2__jobs_schedule_fields.sql

### Parts / Inventory
- Public parts catalog endpoint (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
- Parts management endpoints (CRUD-ish) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
- Parts persisted in SQLite `parts` table (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql

### Parts reservation (job ↔ parts)
- Backend supports listing/creating/cancelling job part reservations (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- Reservations persisted in SQLite `job_part_reservations` table (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql

### Technician management
- UI route for technician management exists (confirmed)
	- Evidence: frontend/src/app/app.routes.ts
- Backend supports create/update/deactivate/reactivate technician (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java
	- Evidence: backend/src/main/java/com/serviceforge/service/TechnicianService.java
- Technician profile fields exist in DB schema (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V3__technicians_profile_fields.sql

### Customer portal
- Not evidenced in reviewed routes/controllers (unknown)
	- Status: NEEDS-EVIDENCE

## Context map

### Integration points
- Frontend → backend via `/api` proxy (confirmed)
	- Evidence: frontend/proxy.conf.json

### Shared concepts across contexts
- `Technician` is referenced by jobs and reservations (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql (FKs)
	- Evidence: backend/src/main/java/com/serviceforge/model/Technician.java
- `Job` is referenced by reservations (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql (FK)
	- Evidence: backend/src/main/java/com/serviceforge/model/Job.java
- `Part` is referenced by reservations (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql (FK)
	- Evidence: backend/src/main/java/com/serviceforge/model/Part.java

## Provenance
- frontend/src/app/app.routes.ts
- frontend/proxy.conf.json
- backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- backend/src/main/java/com/serviceforge/controller/JobController.java
- backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
- backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
- backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
- backend/src/main/java/com/serviceforge/service/JobBookingService.java
- backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- backend/src/main/java/com/serviceforge/service/TechnicianService.java
- backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java
- backend/src/main/resources/db/migration/V1__init.sql
- backend/src/main/resources/db/migration/V2__jobs_schedule_fields.sql
- backend/src/main/resources/db/migration/V3__technicians_profile_fields.sql
