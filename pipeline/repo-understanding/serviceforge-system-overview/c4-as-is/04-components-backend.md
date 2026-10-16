# C4 As-Is — 04 Components (backend)

Scope: backend/
Status: populated
Updated: 2026-10-14

## Layering

Package-by-layer structure (confirmed):
- `com.serviceforge.controller`
	- Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- `com.serviceforge.service`
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java
- `com.serviceforge.data`
	- Evidence: backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java
- `com.serviceforge.model`
	- Evidence: backend/src/main/java/com/serviceforge/model/Technician.java
- `com.serviceforge.dto`
	- Evidence: backend/src/main/java/com/serviceforge/dto/JobDto.java (NEEDS-EVIDENCE: file not yet opened)

## Key components

### Controllers (HTTP API)
- `TechnicianController` (`/api/technicians`) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- `JobController` (`/api/jobs`) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/JobController.java
- `PartsCatalogController` (`/api/parts`) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
- `PartsManagementController` (`/api/parts/manage`) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
- `JobPartsReservationController` (`/api/jobs/{jobId}/parts-reservations`) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java

### Services (business rules)
- `JobBookingService` (booking validation + conflict detection) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java
- `JobPartsReservationService` (reserve/cancel parts with inventory updates) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- `TechnicianService` (technician CRUD + activation + job listing) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/service/TechnicianService.java

### Data access (JDBC)
- `JdbcJobRepository` (jobs queries + overlap predicate) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java
- `JdbcTechnicianRepository` (technicians CRUD + activation) (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/data/JdbcTechnicianRepository.java

### Cross-cutting
- `ApiExceptionHandler` maps exceptions to HTTP status + `ApiError` body (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/ApiExceptionHandler.java

## Provenance
- backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- backend/src/main/java/com/serviceforge/controller/JobController.java
- backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
- backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
- backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
- backend/src/main/java/com/serviceforge/controller/ApiExceptionHandler.java
- backend/src/main/java/com/serviceforge/service/JobBookingService.java
- backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- backend/src/main/java/com/serviceforge/service/TechnicianService.java
- backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java
- backend/src/main/java/com/serviceforge/data/JdbcTechnicianRepository.java
- backend/src/main/java/com/serviceforge/model/Technician.java
