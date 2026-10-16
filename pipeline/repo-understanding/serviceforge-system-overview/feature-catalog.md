# Feature catalog — ServiceForge (current-state)

Scope: Whole repository (ServiceForge-Start)
Status: populated (index-level)
Updated: 2026-10-14

## The Fidelity Mandate
- Only catalogue features that have direct evidence in code/config/docs.
- If evidence is missing, list the feature as a hypothesis and mark NEEDS-EVIDENCE.

## Catalogued features (index)

### Confirmed

1. Technician Availability Calendar / Scheduling view
    - UI default route points to `TechnicianAvailabilityComponent` (confirmed)
       - Evidence: frontend/src/app/app.routes.ts
    - Backend supports listing a technician's jobs with optional `from`/`to` query params (confirmed)
       - Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java
       - Evidence: backend/src/main/java/com/serviceforge/service/TechnicianService.java
       - Evidence: frontend/src/app/services/jobs-api.service.ts

2. Job booking (create job)
    - Backend endpoint `POST /api/jobs` creates a job and returns `201 Created` with Location header (confirmed)
       - Evidence: backend/src/main/java/com/serviceforge/controller/JobController.java
    - Booking conflict rule includes 45-minute travel buffer (confirmed)
       - Evidence: backend/src/main/java/com/serviceforge/service/JobBookingService.java
    - Frontend API client exists (confirmed)
       - Evidence: frontend/src/app/services/jobs-api.service.ts

3. Parts catalog (orderable parts list)
    - Backend endpoint `GET /api/parts` returns active/orderable parts (confirmed)
       - Evidence: backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
    - Frontend API client exists (confirmed)
       - Evidence: frontend/src/app/services/parts-reservations-api.service.ts (listParts)

4. Job parts reservation
    - UI route exists for parts reservations page (confirmed)
       - Evidence: frontend/src/app/app.routes.ts
    - Backend endpoints under `/api/jobs/{jobId}/parts-reservations` support list/create/cancel (confirmed)
       - Evidence: backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
       - Evidence: backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
    - Frontend API client exists (confirmed)
       - Evidence: frontend/src/app/services/parts-reservations-api.service.ts

5. Parts inventory management
    - UI route exists for parts management (confirmed)
       - Evidence: frontend/src/app/app.routes.ts
    - Backend endpoints under `/api/parts/manage` support list/get/create/update/delete (confirmed)
       - Evidence: backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
    - Frontend API client exists (confirmed)
       - Evidence: frontend/src/app/services/parts-api.service.ts

6. Technician onboarding and management
    - UI route exists for technician management (confirmed)
       - Evidence: frontend/src/app/app.routes.ts
    - Backend endpoints under `/api/technicians` support list/get/create/update/deactivate/reactivate (confirmed)
       - Evidence: backend/src/main/java/com/serviceforge/controller/TechnicianController.java
    - Frontend API client exists (confirmed)
       - Evidence: frontend/src/app/services/technicians-api.service.ts
    - Test execution log exists for Feature 9 (confirmed)
       - Evidence: pipeline/test-execution/feature-9-2026-10-11.md

## Per-feature specs
Not generated yet.
- Next: create one file per feature with acceptance criteria and a source reference table.
- Suggested filenames (under this same folder):
   - feature-1-technician-availability.md
   - feature-2-job-booking.md
   - feature-3-parts-catalog.md
   - feature-4-job-parts-reservation.md
   - feature-5-parts-management.md
   - feature-6-technician-management.md

## Provenance
- AGENTS.md
- frontend/src/app/app.routes.ts
- frontend/src/app/services/jobs-api.service.ts
- frontend/src/app/services/parts-api.service.ts
- frontend/src/app/services/parts-reservations-api.service.ts
- frontend/src/app/services/technicians-api.service.ts
- backend/src/main/java/com/serviceforge/controller/TechnicianController.java
- backend/src/main/java/com/serviceforge/controller/JobController.java
- backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java
- backend/src/main/java/com/serviceforge/controller/PartsManagementController.java
- backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java
- backend/src/main/java/com/serviceforge/service/JobBookingService.java
- backend/src/main/java/com/serviceforge/service/JobPartsReservationService.java
- pipeline/test-execution/feature-9-2026-10-11.md
