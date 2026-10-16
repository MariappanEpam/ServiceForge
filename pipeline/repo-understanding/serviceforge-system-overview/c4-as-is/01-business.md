# C4 As-Is — 01 Business context

Scope: Whole repository (ServiceForge-Start)
Status: partial
Updated: 2026-10-14

## System purpose
ServiceForge is a Field Service Management platform.
- Evidence: AGENTS.md

## Primary actors

- Dispatcher / Scheduler (inferred from scheduling workflows; partial)
	- Evidence: AGENTS.md (mentions technician scheduling)
- Technician (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/model/Technician.java
	- Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- Inventory manager (inferred from parts inventory management; partial)
	- Evidence: backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
- Customer (confirmed as a field on jobs)
	- Evidence: backend/src/main/java/com/serviceforge/model/Job.java (customerName)
	- Evidence: backend/src/main/java/com/serviceforge/controller/JobController.java

## Business capabilities (high-level)

- Work orders / jobs (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/JobController.java
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java
- Technician scheduling / availability (confirmed via job listing + overlap rules)
	- Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java
	- Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java
	- Evidence: frontend/src/app/app.routes.ts
- Parts inventory (confirmed)
	- Evidence: backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
	- Evidence: backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
	- Evidence: backend/src/main/resources/db/migration/V1__init.sql
- Customer portal (unknown)
	- Status: NEEDS-EVIDENCE
- SLA tracking (unknown)
	- Status: NEEDS-EVIDENCE

## Provenance
- AGENTS.md
- backend/src/main/java/com/serviceforge/model/Technician.java
- backend/src/main/java/com/serviceforge/model/Job.java
- backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- backend/src/main/java/com/serviceforge/controller/JobController.java
- backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
- backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
- backend/src/main/java/com/serviceforge/service/JobBookingService.java
- backend/src/main/resources/db/migration/V1__init.sql
- frontend/src/app/app.routes.ts
