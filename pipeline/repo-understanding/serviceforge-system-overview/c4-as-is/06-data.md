# C4 As-Is — 06 Data

Scope: Whole repository
Status: partial
Updated: 2026-10-14

## Persistence
- SQLite + Flyway (confirmed)
  - Evidence: backend/pom.xml
  - Evidence: backend/src/main/resources/application.yml

## Data model

### Tables and columns (SQLite)
- `technicians` (confirmed)
  - Evidence: backend/src/main/resources/db/migration/V1__init.sql
  - Evidence: backend/src/main/resources/db/migration/V3__technicians_profile_fields.sql
- `jobs` (confirmed)
  - Evidence: backend/src/main/resources/db/migration/V1__init.sql
  - Evidence: backend/src/main/resources/db/migration/V2__jobs_schedule_fields.sql
- `parts` (confirmed)
  - Evidence: backend/src/main/resources/db/migration/V1__init.sql
- `job_part_reservations` (confirmed)
  - Evidence: backend/src/main/resources/db/migration/V1__init.sql

### DTOs / API payloads (examples)
- Create job request includes `technicianId`, `customerName`, `startTime`, `endTime` (confirmed)
  - Evidence: backend/src/main/java/com/serviceforge/controller/JobController.java
  - Evidence: frontend/src/app/services/jobs-api.service.ts
- Technician upsert includes profile fields (confirmed)
  - Evidence: frontend/src/app/services/technicians-api.service.ts
- Reservation create includes `sku`, `quantity` (confirmed)
  - Evidence: backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
  - Evidence: frontend/src/app/services/parts-reservations-api.service.ts

## Provenance
- backend/pom.xml
- backend/src/main/resources/application.yml
- backend/src/main/resources/db/migration/V1__init.sql
- backend/src/main/resources/db/migration/V2__jobs_schedule_fields.sql
- backend/src/main/resources/db/migration/V3__technicians_profile_fields.sql
- backend/src/main/java/com/serviceforge/controller/JobController.java
- backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
- frontend/src/app/services/jobs-api.service.ts
- frontend/src/app/services/technicians-api.service.ts
- frontend/src/app/services/parts-reservations-api.service.ts
