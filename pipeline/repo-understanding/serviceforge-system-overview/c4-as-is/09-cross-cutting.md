# C4 As-Is — 09 Cross-cutting concerns

Scope: Whole repository
Status: populated (partial)
Updated: 2026-10-14

## Security

### Authentication / authorization
- No authentication/authorization mechanisms evidenced in reviewed controllers/services (unknown)
	- Status: NEEDS-EVIDENCE

### Input validation
- Bean validation is used on request bodies via `@Valid` / `@Validated` (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/JobController.java
	- Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java
	- Evidence: backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
- Validation errors are mapped to HTTP 400 with an `ApiError` body (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/ApiExceptionHandler.java

## Observability

### Logging
- Booking conflicts are logged at INFO level (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java

### Metrics
- Actuator endpoints enabled for `health,info` exposure (confirmed)
	- Evidence: backend/src/main/resources/application.yml

## Error handling

- Central exception handler maps:
	- NotFound → 404
	- Conflict → 409
	- IllegalArgumentException → 400
	- MethodArgumentNotValidException → 400
	- Evidence: backend/src/main/java/com/serviceforge/controller/ApiExceptionHandler.java

## Provenance
- backend/src/main/java/com/serviceforge/controller/ApiExceptionHandler.java
- backend/src/main/java/com/serviceforge/controller/JobController.java
- backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
- backend/src/main/java/com/serviceforge/service/JobBookingService.java
- backend/src/main/resources/application.yml
