Feature 2 — Parts Reservation
Design Review Report

Executive Summary
- Decision: CHANGES REQUESTED
- Reason: The architecture is well-structured and addresses core concurrency concerns for an in-memory mock store, but several gaps in error handling, API consistency, security considerations, and migration-testability need clarification before approval.

Detailed Findings

Scalability
- Strengths: per-SKU synchronization avoids global locks; use of ConcurrentHashMap and AtomicInteger is correct for demo-scale concurrency.
- Risks: synchronized on `PartRecord` may still cause contention for very hot SKUs under test loads; no sharding or partitioning strategy is proposed for scaling beyond in-memory demo. No limits or throttling on reservation creation are defined to prevent DOS-like spikes during demos.

Security
- Strengths: design uses DTOs and request validation per-controller layer.
- Gaps: missing authentication/authorization considerations for endpoints (e.g., who may `restock` or `cancel`); no mention of input sanitization for SKU strings, no rate-limiting, and no audit trail requirements beyond simple logs. Error responses may leak internal details unless `ApiError` is standardized to hide stack traces.

Maintainability
- Strengths: package-by-layer, constructor injection, and migration-safety notes align with repo conventions.
- Gaps: MockDataStore extension approach not described in code-level detail — risk of duplicated seed logic. No explicit interfaces for `ReservationService` or `PartsInventoryService` are listed (recommended). The use of synchronized blocks mixed with AtomicInteger should be clearly documented to avoid double-checked locking mistakes.

Testability
- Strengths: explicit guidance for unit and concurrency tests, constructor injection requirement is good.
- Gaps: no test-specific hooks (e.g., ability to fast-forward timestamps or deterministic serial assignment) and no guidance on seeding deterministically in CI (randomized seeds mentioned). Concurrency tests need assertion helpers and cleanup steps described.

Required Changes or Clarifications (actionable items)

1) Authorization and endpoint access control
- Action: Add an auth/authorization note and required checks in `PartsController` for `POST /api/parts/{sku}/restock` and `DELETE /api/parts/reservations/{reservationId}`. Define roles (e.g., ROLE_ADMIN for restock, ROLE_TECH for creating/canceling reservations). Update controller docstrings and API contract.
- Files/classes: [pipeline/architecture/feature-2-architecture.md](pipeline/architecture/feature-2-architecture.md#L1-L1) (update design), com.serviceforge.controller.PartsController (add authorization checks and method-level JavaDoc), ApiError.java (ensure auth failures map to 403).

2) ApiError and error handling standardization
- Action: Define `ApiError` fields (code, message, userMessage, timestamp, requestId) and ensure controllers return sanitized errors (no stack traces). Map InsufficientStockException to 409 using a centralized exception handler `GlobalExceptionHandler`.
- Files/classes: com.serviceforge.dto.ApiError, com.serviceforge.controller.PartsController, add com.serviceforge.controller.GlobalExceptionHandler (new).

3) Define Service interfaces for testability
- Action: Add interfaces `IReservationService` and `IPartsInventoryService` implemented by `ReservationService` and `PartsInventoryService` to allow mocking and clear contracts.
- Files/classes: com.serviceforge.service.IReservationService (new), com.serviceforge.service.IPartsInventoryService (new), com.serviceforge.service.ReservationService, com.serviceforge.service.PartsInventoryService.

4) Deterministic seeding and test hooks
- Action: Update `MockDataStore` seed to accept a deterministic seed flag and provide helper methods: `seedDeterministic()` and `seedRandomized()` and expose hooks for deterministic serial assignment. Provide a test-only API to reset store between tests.
- Files/classes: com.serviceforge.data.MockDataStore (update), tests/* (update concurrency tests to use deterministic seeds).

5) Concurrency edge-case: null part record
- Action: Explicitly document and implement null checks and not-found behavior in `ReservationService.createReservation()` and return 404 when SKU not found.
- Files/classes: com.serviceforge.service.ReservationService, com.serviceforge.controller.PartsController (ensure 404 mapping), pipeline/architecture/feature-2-architecture.md (note update).

6) Observability and request tracing
- Action: Require requestId propagation from controller to services and logs. Include in `ApiError` and log entries. Add a LoggingUtil to standardize log messages.
- Files/classes: com.serviceforge.controller.PartsController (extract requestId from header or generate), com.serviceforge.service.ReservationService (accept requestId param), com.serviceforge.dto.ApiError (add requestId), com.serviceforge.util.LoggingUtil (new).

7) Security input validation
- Action: Add stricter validation on incoming SKU strings and serial elements (pattern and max length), and validate quantity upper limit (prevent int overflow or unreasonably large requests). Update DTOs with validation annotations.
- Files/classes: com.serviceforge.dto.ReservationCreateRequest (add @Size/@Pattern/@Min/@Max), com.serviceforge.controller.PartsController (validate), pipeline/architecture/feature-2-architecture.md (note update).

Acceptance Checklist
- [ ] `PartsController` enforces authorization for restock and cancellation; roles defined in API docs and mapped to 403 responses when unauthorized.
- [ ] `ApiError` standardized (code, message, userMessage, timestamp, requestId); `GlobalExceptionHandler` maps exceptions safely (404, 409, 400, 500).
- [ ] `IReservationService` and `IPartsInventoryService` interfaces added and implemented; services are injected via constructors.
- [ ] `MockDataStore` supports deterministic seeding and test reset helper; concurrency tests updated to use deterministic seeds and include cleanup.
- [ ] `ReservationService.createReservation()` validates SKU existence and returns 404 when missing; synchronized block implemented per-`PartRecord` as described.
- [ ] Request tracing implemented (requestId passed through logs and `ApiError`).
- [ ] DTO validation annotations present and enforced; controllers reject invalid inputs with 400.

Design Strengths
- Clear package-by-layer layout matching repo conventions.
- Per-SKU synchronization minimizes lock contention for demo workloads.
- Explicit migration-safety guidance reduces risk when evolving models.

Design Risks (concise)
- Hot-SKU contention under higher concurrency could still bottleneck; consider partitioning or optimistic concurrency later.
- Missing auth and audit could allow unauthorized restock/cancel operations in demos.
- Randomized seeding in CI can cause flaky tests; deterministic seed must be default for tests.

Approval Decision
- Status: CHANGES REQUESTED — address the items above; re-run review after changes.

Handoff update JSON
{"feature":"feature-2-parts-reservation","status":"Changes Requested","owner":"Implementation Planner Agent","artifacts":["pipeline/architecture/feature-2-architecture.md","pipeline/features/feature-2-parts-reservation.md","pipeline/decisions/feature-1-decisions.md"],"blockers":["Auth/authorization not defined","ApiError shape not standardized","Deterministic seeding for tests missing"]}
