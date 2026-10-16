# Domain glossary — ServiceForge (system overview)

Scope: Whole repository (ServiceForge-Start)
Status: populated
Updated: 2026-10-14

## Terms

- Technician: A field worker record with profile fields and an active flag.
	- Evidence: backend/src/main/java/com/serviceforge/model/Technician.java
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql (technicians table)
	- Evidence: backend/src/main/resources/db/migration/V3__technicians_profile_fields.sql (profile columns)

- Job (Work Order): A scheduled unit of work assigned to a technician, with `customerName`, `startTime`, `endTime`.
	- Evidence: backend/src/main/java/com/serviceforge/model/Job.java
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql (jobs table)
	- Evidence: backend/src/main/resources/db/migration/V2__jobs_schedule_fields.sql (start_time/end_time)
	- Evidence: backend/src/main/java/com/serviceforge/controller/JobController.java (POST /api/jobs)

- Availability (derived): In this codebase, availability is inferred from a technician's scheduled jobs and overlap rules (no separate availability entity found in reviewed backend model).
	- Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java (GET /api/technicians/{id}/jobs)
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java (overlap + travel buffer)

- Part: A catalog/inventory item identified by `sku`, with `active`, `orderable`, and `quantity_on_hand`.
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql (parts table)
	- Evidence: backend/src/main/java/com/serviceforge/model/Part.java
	- Evidence: backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java (GET /api/parts)
	- Evidence: backend/src/main/java/com/serviceforge/controller/PartsManagementController.java (manage endpoints)

- Job part reservation: A hold on parts inventory for a specific job, created by the job's technician, with cancel support.
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql (job_part_reservations table)
	- Evidence: backend/src/main/java/com/serviceforge/model/JobPartReservation.java
	- Evidence: backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java

## Provenance
- backend/src/main/java/com/serviceforge/model/Technician.java
- backend/src/main/java/com/serviceforge/model/Job.java
- backend/src/main/java/com/serviceforge/model/Part.java
- backend/src/main/java/com/serviceforge/model/JobPartReservation.java
- backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- backend/src/main/java/com/serviceforge/controller/JobController.java
- backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
- backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
- backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
- backend/src/main/java/com/serviceforge/service/JobBookingService.java
- backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- backend/src/main/resources/db/migration/V1__init.sql
- backend/src/main/resources/db/migration/V2__jobs_schedule_fields.sql
- backend/src/main/resources/db/migration/V3__technicians_profile_fields.sql
