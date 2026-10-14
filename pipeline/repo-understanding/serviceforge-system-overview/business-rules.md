# Business rules — ServiceForge (system overview)

Scope: Whole repository (ServiceForge-Start)
Status: populated
Updated: 2026-10-14

## Rules catalogue

### BR-1: Job booking requires an existing technician
- If `technicianId` does not exist, booking fails with not-found (confirmed)
	- Enforcement: `JobBookingService.book()` checks `technicianRepository.existsById(technicianId)` and throws `NotFoundException`
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java

### BR-2: Job booking requires non-empty customer name
- `customerName` must be non-null and non-blank (confirmed)
	- Enforcement: `JobBookingService.book()` throws `IllegalArgumentException` when blank
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java

### BR-3: Job booking requires valid time range
- `startTime` and `endTime` must be present; `endTime` must be after `startTime` (confirmed)
	- Enforcement: `JobBookingService.book()` validates and throws `IllegalArgumentException`
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java

### BR-4: Job booking conflict detection includes a 45-minute travel buffer
- Booking conflicts are checked against an expanded window: `startTime - 45m` to `endTime + 45m` (confirmed)
	- Enforcement: `JobBookingService.TRAVEL_BUFFER` and `existsOverlapping(technicianId, expandedStart, expandedEnd)`
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java
	- Evidence: backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java (overlap SQL)

### BR-5: Overlap rule (SQL)
- A job overlaps if `existing.start_time < requested.expandedEnd` AND `existing.end_time > requested.expandedStart` (confirmed)
	- Enforcement: `JdbcJobRepository.existsOverlapping()` SQL predicate
	- Evidence: backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java

### BR-6: Parts reservation quantity must be > 0
- Reservation create rejects `quantity <= 0` (confirmed)
	- Enforcement: `JobPartsReservationService.create()` throws `IllegalArgumentException`
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java

### BR-7: Parts reservation requires existing job
- Reservation list/create/cancel requires job exists (confirmed)
	- Enforcement: `JobPartsReservationService.listForJob/create/cancel` loads job via `jobRepository.findById(...).orElseThrow(NotFoundException)`
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java

### BR-8: Reservable parts must be active and orderable
- If part is missing OR not active OR not orderable, reservation create returns not-found (confirmed)
	- Enforcement: `JobPartsReservationService.create()` checks `part.active()` and `part.orderable()`; otherwise throws `NotFoundException("Part not found")`
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java

### BR-9: Inventory must be sufficient to reserve
- Reservation create fails with conflict when inventory is insufficient (confirmed)
	- Enforcement: `inventoryGateway.decrementIfSufficient(sku, quantity)` must update exactly 1 row; else throws `ConflictException` with available quantity
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java

### BR-10: Cancelling a reservation is idempotency-protected
- Cancelling an already-cancelled reservation fails with conflict (confirmed)
	- Enforcement: `JobPartsReservationService.cancel()` checks `r.isCancelled()` and throws `ConflictException`
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java

### BR-11: Cancelling a reservation restores inventory
- Cancel increments inventory by reserved quantity (confirmed)
	- Enforcement: `inventoryGateway.increment(sku, r.getQuantity())`
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java

### BR-12: Parts quantity on hand cannot be negative (DB constraint)
- `parts.quantity_on_hand >= 0` enforced by CHECK constraint (confirmed)
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql

### BR-13: Technician management supports activate/deactivate
- Technician can be deactivated/reactivated (confirmed)
	- Enforcement: `TechnicianController` endpoints call `TechnicianService.deactivateTechnician/reactivateTechnician`
	- Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java
	- Evidence: backend/src/main/java/com/serviceforge/service/TechnicianService.java
	- Evidence: backend/src/main/java/com/serviceforge/data/JdbcTechnicianRepository.java

### BR-14: API error mapping
- NotFound → 404, Conflict → 409, IllegalArgument/Validation → 400 (confirmed)
	- Enforcement: `ApiExceptionHandler`
	- Evidence: backend/src/main/java/com/serviceforge/controller/ApiExceptionHandler.java

## Rule sources

Rules are enforced across:
- Service layer: booking and reservation invariants
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java
	- Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- Repository layer: overlap predicate
	- Evidence: backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java
- DB constraints: non-negative inventory
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql
- Controller advice: consistent HTTP status mapping
	- Evidence: backend/src/main/java/com/serviceforge/controller/ApiExceptionHandler.java

## Provenance
- backend/src/main/java/com/serviceforge/service/JobBookingService.java
- backend/src/main/java/com/serviceforge/data/JdbcJobRepository.java
- backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- backend/src/main/resources/db/migration/V1__init.sql
- backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- backend/src/main/java/com/serviceforge/service/TechnicianService.java
- backend/src/main/java/com/serviceforge/data/JdbcTechnicianRepository.java
- backend/src/main/java/com/serviceforge/controller/ApiExceptionHandler.java
