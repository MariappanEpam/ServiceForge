# Persistence entities — ServiceForge (system overview)

Scope: Whole repository (ServiceForge-Start)
Status: populated
Updated: 2026-10-14

## Storage technologies

### SQLite + Flyway
- SQLite JDBC datasource (confirmed)
  - Evidence: backend/src/main/resources/application.yml (spring.datasource.driver-class-name, spring.datasource.url)
- Flyway migrations enabled (confirmed)
  - Evidence: backend/src/main/resources/application.yml (spring.flyway.enabled, spring.flyway.locations)

### JPA / ORM
- No JPA entities detected in the reviewed backend model layer (plain POJOs used) (confirmed)
  - Evidence: backend/src/main/java/com/serviceforge/model/Technician.java
  - Evidence: backend/src/main/java/com/serviceforge/model/Job.java
  - Evidence: backend/src/main/java/com/serviceforge/model/Part.java
  - Evidence: backend/src/main/java/com/serviceforge/model/JobPartReservation.java

## Flyway migrations → tables/columns

### V1__init.sql
- Creates tables (confirmed)
  - `technicians(id INTEGER PRIMARY KEY, name TEXT NOT NULL)`
  - `jobs(id INTEGER PRIMARY KEY, technician_id INTEGER NOT NULL, customer_name TEXT NOT NULL, FOREIGN KEY technician_id → technicians.id)`
  - `parts(sku TEXT PRIMARY KEY, name TEXT NOT NULL, description TEXT NULL, active INTEGER NOT NULL, orderable INTEGER NOT NULL, quantity_on_hand INTEGER NOT NULL, CHECK quantity_on_hand >= 0)`
  - `job_part_reservations(id INTEGER PRIMARY KEY, job_id INTEGER NOT NULL, reserved_by_technician_id INTEGER NOT NULL, sku TEXT NOT NULL, quantity_reserved INTEGER NOT NULL, created_at TEXT NOT NULL, cancelled_at TEXT NULL, FKs → jobs/technicians/parts)`
  - Evidence: backend/src/main/resources/db/migration/V1__init.sql

### V2__jobs_schedule_fields.sql
- Adds schedule fields to `jobs` (confirmed)
  - `jobs.start_time TEXT NOT NULL DEFAULT '1970-01-01T00:00:00'`
  - `jobs.end_time TEXT NOT NULL DEFAULT '1970-01-01T00:00:00'`
  - Evidence: backend/src/main/resources/db/migration/V2__jobs_schedule_fields.sql

### V3__technicians_profile_fields.sql
- Adds technician profile fields (confirmed)
  - `technicians.active INTEGER NOT NULL DEFAULT 1`
  - `technicians.age INTEGER NULL`
  - `technicians.area_served TEXT NULL`
  - `technicians.vehicle_type TEXT NULL`
  - `technicians.vehicle_make TEXT NULL`
  - `technicians.vehicle_model TEXT NULL`
  - `technicians.vehicle_license_plate TEXT NULL`
  - `technicians.skills_csv TEXT NULL`
  - Evidence: backend/src/main/resources/db/migration/V3__technicians_profile_fields.sql

## Repository / gateway mapping

### Technicians
- `JdbcTechnicianRepository` reads/writes `technicians` including profile fields (confirmed)
  - Evidence: backend/src/main/java/com/serviceforge/data/JdbcTechnicianRepository.java

### Jobs
- `JdbcJobRepository` reads/writes `jobs` including `start_time` and `end_time` (confirmed)
  - Evidence: backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java

### Parts + inventory
- Parts are exposed via controllers/services; persistence uses JDBC repositories/gateways (confirmed)
  - Evidence: backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
  - Evidence: backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
  - Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
  - Evidence: backend/src/main/java/com/serviceforge/data/JdbcPartRepository.java (NEEDS-EVIDENCE: not yet cited line-level)
  - Evidence: backend/src/main/java/com/serviceforge/data/JdbcPartsInventoryGateway.java (NEEDS-EVIDENCE: not yet cited line-level)

### Job part reservations
- `JdbcJobPartReservationRepository` persists `job_part_reservations` (confirmed)
  - Evidence: backend/src/main/java/com/serviceforge/data/JdbcJobPartReservationRepository.java (NEEDS-EVIDENCE: not yet cited line-level)

## Provenance
- backend/src/main/resources/application.yml
- backend/src/main/resources/db/migration/V1__init.sql
- backend/src/main/resources/db/migration/V2__jobs_schedule_fields.sql
- backend/src/main/resources/db/migration/V3__technicians_profile_fields.sql
- backend/src/main/java/com/serviceforge/data/JdbcTechnicianRepository.java
- backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java
- backend/src/main/java/com/serviceforge/model/*.java
