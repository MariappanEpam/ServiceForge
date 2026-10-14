# Aggregate boundaries — ServiceForge (system overview)

Scope: Whole repository (ServiceForge-Start)
Status: populated
Updated: 2026-10-14

## Candidate aggregates

### A1: Technician
- Root entity: `Technician` (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/model/Technician.java
- Persistence: `technicians` table (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql
	- Evidence: backend/src/main/resources/db/migration/V3__technicians_profile_fields.sql

### A2: Job (Work Order)
- Root entity: `Job` (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/model/Job.java
- Persistence: `jobs` table with schedule fields (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql
	- Evidence: backend/src/main/resources/db/migration/V2__jobs_schedule_fields.sql

### A3: Part (Inventory item)
- Root entity: `Part` (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/model/Part.java
- Persistence: `parts` table with `quantity_on_hand` constraint (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql

### A4: JobPartReservation
- Entity: `JobPartReservation` (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/model/JobPartReservation.java
- Persistence: `job_part_reservations` table (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql

### Availability
- Availability is represented indirectly via scheduled jobs (no separate availability entity found in reviewed backend model) (partial)
	- Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java (list jobs)
	- Evidence: backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java (listByTechnician)

## Aggregate invariants

### Job booking invariants
- No overlapping jobs for a technician within an expanded window (45-minute travel buffer) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java
	- Evidence: backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java

### Reservation invariants
- Reservation quantity must be > 0 (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- Part must be active and orderable to reserve (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- Inventory must be sufficient; decrement is conditional (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- Cancel is not allowed twice (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java

### Transaction boundaries
- Reservation create/cancel are transactional (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java (@Transactional)

## Provenance
- backend/src/main/java/com/serviceforge/model/Technician.java
- backend/src/main/java/com/serviceforge/model/Job.java
- backend/src/main/java/com/serviceforge/model/Part.java
- backend/src/main/java/com/serviceforge/model/JobPartReservation.java
- backend/src/main/java/com/serviceforge/service/JobBookingService.java
- backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java
- backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- backend/src/main/resources/db/migration/V1__init.sql
- backend/src/main/resources/db/migration/V2__jobs_schedule_fields.sql
- backend/src/main/resources/db/migration/V3__technicians_profile_fields.sql
