# Feature 2 — Parts Reservation: Implementation Plan

Summary
- Purpose: convert the approved Feature-2 architecture and spec into a prioritized implementation plan (epics, stories, tasks) so Developer work can begin.
- Scope: backend services and interfaces for parts reservation/inventory, frontend UI for reservations, validation, tests, seeding, docs, and handoff.

Assumptions
- Design review items already applied where noted (ApiError shape, GlobalExceptionHandler, IReservationService, deterministic seeding helper).
- No production database required — continue using MockDataStore pattern.

High-level epics (priority order)
1. Backend: Parts inventory & reservation core
   - Story 1.1: Define `IPartsInventoryService` and `IReservationService` contracts (interfaces). Estimate: 0.5d
   - Story 1.2: Implement `PartsInventoryService` with in-memory `PartRecord` (reserve, release, restock). Estimate: 1.5d
   - Story 1.3: Implement `ReservationService` that uses `IPartsInventoryService` and enforces availability and travel-buffer rules when reservations tie to jobs. Estimate: 1.5d
   - Story 1.4: Add domain exceptions (`InsufficientStockException`, `ReservationNotFoundException`) and map in `GlobalExceptionHandler`. Estimate: 0.5d
   - Acceptance: services expose clean interfaces, unit tests cover reserve/release/restock happy & failure paths.

2. Backend: Authorization & request tracing
   - Story 2.1: Add simple role-checks for sensitive operations (`restock`, `cancel reservation`) — annotation-based or service-level guard. Estimate: 0.5d
   - Story 2.2: Propagate `X-Request-Id` from controllers through services and include in `ApiError` and logs (use MDC). Estimate: 0.5d
   - Story 2.3: Add SLF4J logging where exceptions are handled and on critical state changes (reserve/restock/cancel). Estimate: 0.5d
   - Acceptance: requestId appears in error responses and logs; protected actions return 403 when unauthorized.

3. Backend: Deterministic seeding, tests, and CI
   - Story 3.1: Wire `MockDataStore.seedDeterministic(...)` into tests where needed; add a test helper to reset state. Estimate: 0.5d
   - Story 3.2: Add unit tests for overlapping interval booking and parts reservation concurrency (concurrent reserve attempts). Estimate: 1.0d
   - Story 3.3: Ensure `mvn test` passes locally and configure CI job to run tests. Estimate: 0.5d
   - Acceptance: tests stable and reproducible; no flaky concurrency failures in CI smoke run.

4. Frontend: Parts reservation UI & validation
   - Story 4.1: Add models/DTOs for parts and reservations. Estimate: 0.25d
   - Story 4.2: Create reservation flow in UI (parts search, reserve quantity, attach to job). Estimate: 1.0d
   - Story 4.3: Client-side validation for quantity, date/time, and conflict detection (use same 45m buffer rule). Estimate: 0.75d
   - Story 4.4: Map server ApiError shape to friendly UI messages and include requestId in feedback. Estimate: 0.5d
   - Acceptance: user can reserve parts for a job; client surfaces conflicts before submission where possible.

5. Backend API: Controllers and DTO validation
   - Story 5.1: Add `PartsController` endpoints: GET /api/parts, POST /api/parts/{sku}/restock (auth), POST /api/parts/reservations (reserve), DELETE /api/parts/reservations/{id} (cancel/auth). Estimate: 1.0d
   - Story 5.2: Apply `@Valid` DTOs, ensure `GlobalExceptionHandler` maps validation errors to ApiError with requestId. Estimate: 0.5d
   - Acceptance: endpoints implemented, validated, and documented; errors follow ApiError contract.

6. Integration testing & concurrency validation
   - Story 6.1: Add integration test that simulates two concurrent reserve attempts for the last unit and asserts only one succeeds. Estimate: 1.0d
   - Story 6.2: Add end-to-end test that reserves parts and books job references (mocked job flow). Estimate: 0.75d
   - Acceptance: concurrency test reliably shows correct locking/atomic behavior.

7. Documentation, API contract, and handoff
   - Story 7.1: Update pipeline/decisions and pipeline/architecture to record final decisions (interfaces, exceptions, auth model). Estimate: 0.25d
   - Story 7.2: Produce handoff `pipeline/handoffs/feature-2-handoff.md` with status, remaining blockers, and test results. Estimate: 0.25d
   - Acceptance: docs updated and ready for Developer/tester handoff.

Non-functional & cross-cutting tasks
- Add structured logging and include `requestId` (MDC) in all backend logs. Estimate: 0.5d
- Add code comments and Javadoc for all new public interfaces. Estimate: 0.5d
- Small UX polish: error banner with requestId and retry link in frontend. Estimate: 0.5d

Estimates & timeline
- Rough total: ~10–12 developer days (single engineer) to deliver feature end-to-end including tests and docs. Can be split across 2 engineers in parallel (backend + frontend). Prioritize backend core + tests first.

Definition of Done
- All stories complete with unit and integration tests passing.
- ApiError and requestId present in all error responses.
- CI runs tests cleanly and E2E sanity passes.
- Handoff file created and design-review re-run to reach APPROVED.

Risks & mitigations
- Concurrency races on reserve/release: mitigate with per-part locking (`synchronized` or atomic types) and deterministic tests.
- Time-format mismatches from frontend: use ISO-8601 and document expected format in API docs.

Next steps (immediate)
1. Review and assign owners to epics/stories.
2. Create a working branch feature/feature-2-parts-reservation and open PR per story grouping.
3. Implement backend core (epic 1) and run tests; follow with controller and frontend.

Files of interest (implementation touches)
- pipeline/features/feature-2-parts-reservation.md
- backend/src/main/java/com/serviceforge/service/ (IReservationService, new IPartsInventoryService, ReservationService, PartsInventoryService)
- backend/src/main/java/com/serviceforge/controller/PartsController.java
- backend/src/main/java/com/serviceforge/dto/ (new reservation DTOs)
- backend/src/main/java/com/serviceforge/data/MockDataStore.java (seedDeterministic already present)
- frontend/src/app/services/technician.service.ts and new services for parts

If you want I can now:
- apply the minimal scaffolding patches for the interfaces and controller stubs, or
- create the branch and open the PR text for you to start coding.
