Feature 2 — Parts Reservation

Architecture Design

Source spec: [pipeline/features/feature-2-parts-reservation.md](pipeline/features/feature-2-parts-reservation.md)
Related decision: [pipeline/decisions/feature-1-decisions.md](pipeline/decisions/feature-1-decisions.md)
Migration guidance: [.claude/skills/migration-safety-skill/SKILL.md](.claude/skills/migration-safety-skill/SKILL.md)

1) High-level component diagram (textual)

- Frontend
  - `PartsInventoryComponent` (views inventory, search, SKU detail)
  - `PartReservationDialog` (select quantity/serials and submit reservation)
  - `JobPartsPanel` (integrated into job screen to list/create/cancel reservations)
  - `PartsServiceClient` (Angular service that calls backend REST endpoints)

- Backend
  - Controller layer
    - `PartsController` — exposes REST endpoints: GET /api/parts, GET /api/parts/{sku}, POST /api/parts/reserve, GET /api/jobs/{jobId}/parts, DELETE /api/parts/reservations/{reservationId}, POST /api/parts/{sku}/restock
  - Service layer
    - `PartsInventoryService` — read-only inventory operations, search, SKU metadata
    - `ReservationService` — reservation lifecycle: create, list for job, cancel, restock
  - Data layer (mocked)
    - `MockDataStore` extension: add parts inventory collections and reservation store
    - In-memory domain records: `PartRecord` (quantity, serials, flags), `ReservationRecord`
  - DTOs / API models
    - `PartDto`, `PartDetailDto`, `ReservationDto`, `ReservationCreateRequest`, `RestockRequest`, `ApiError`

Interaction flow (create reservation): `PartsController` -> `ReservationService.createReservation()` -> transactional check-and-decrement on `MockDataStore`/`PartRecord` -> on success persist `ReservationRecord` in `MockDataStore` and return `ReservationDto` (201). On failure return 409 with `ApiError`.

2) Package-by-layer layout and class names

Backend (Java, package-by-layer):
- com.serviceforge.model.parts
  - Part.java (fields: `sku`, `description`, `serialTracked` (boolean), `minimumStockThreshold`)
  - PartSerial.java (fields: `serial`, `sku`, `status`)
  - Reservation.java (fields: `id`, `jobId`, `sku`, `quantity`, `serials` (optional list), `reservedBy`, `reservedAt`, `status`)

- com.serviceforge.data
  - MockDataStore.java (extend existing store; add `ConcurrentHashMap<String, PartRecord> partsBySku`, `ConcurrentHashMap<String, Reservation> reservationsById`, helper seed methods)
  - PartRecord.java (in-memory representation: `AtomicInteger quantityOnHand`, `ConcurrentLinkedDeque<String> availableSerials`, metadata)

- com.serviceforge.service
  - PartsInventoryService.java (list/search/get SKU details)
  - ReservationService.java (createReservation, listByJob, cancelReservation, restock)

- com.serviceforge.controller
  - PartsController.java (REST endpoints and request validation)

- com.serviceforge.dto
  - PartDto.java, PartDetailDto.java, ReservationDto.java
  - ReservationCreateRequest.java (jobId, sku, quantity, optional serials[])
  - RestockRequest.java (quantity)
  - ApiError.java (standard error shape used across backend)

Frontend (Angular):
- src/app/components/parts-inventory/PartsInventoryComponent
- src/app/components/part-reservation-dialog/PartReservationDialog
- src/app/components/job-parts-panel/JobPartsPanel
- src/app/services/parts-api.service.ts (HTTP client wrapper returning typed DTOs)
- src/app/models/part.model.ts, reservation.model.ts

3) Detailed concurrency / mocking approach

Design goals: deterministic, thread-safe mutations on the in-memory store; behavior should match a simple transactional check-and-decrement semantics.

Data structures
- Use `ConcurrentHashMap<String, PartRecord> partsBySku` in `MockDataStore`.
- Implement `PartRecord` with `AtomicInteger quantityOnHand` and, for serial-tracked SKUs, `ConcurrentLinkedDeque<String> availableSerials`.

Atomic reservation algorithm
- `ReservationService.createReservation(ReservationCreateRequest req)` should:
  1. Validate request (`quantity > 0`, SKU exists, serial list consistency for serial-tracked SKUs).
  2. Locate `PartRecord part = mockDataStore.getPartRecord(req.getSku())`.
  3. Acquire intrinsic lock on `part` (synchronized on `part`) to serialize modifications for that SKU only:
     - Within the synchronized block perform check: if `part.quantityOnHand.get() < req.quantity` then throw `InsufficientStockException`.
     - If serial-tracked, ensure `availableSerials.size() >= req.quantity` and remove the exact serials (or auto-assign by polling `availableSerials`).
     - Otherwise call `part.quantityOnHand.addAndGet(-req.quantity)`.
     - Create `Reservation` with status `ACTIVE` and store it in `MockDataStore.reservationsById`.
  4. Return created `ReservationDto`.

Rationale: synchronizing on the `PartRecord` instance limits lock contention to the affected SKU and avoids coarse global locking. Using `AtomicInteger` provides safe arithmetic for concurrent reads and is used alongside the synchronized block for clarity and safety.

Cancellation and restock
- `cancelReservation` should also synchronize on the associated `PartRecord` to restore quantity and (for serials) push serials back into `availableSerials` in a deterministic order; mark reservation status `CANCELLED` and append to reservation history.
- `restock` (admin) must synchronize on `PartRecord` and call `quantityOnHand.addAndGet(+N)` and add serials when provided.

MockDataStore seeding
- Seed SKUs SKU-1000..SKU-1100 with randomized non-zero `quantityOnHand` and mark a few SKUs as `serialTracked = true` with pre-populated `availableSerials` lists. Use ISO-8601 timestamps for seeded `reservedAt` where necessary.

Testing guidance
- Unit tests: mock or use an in-memory `MockDataStore` instance seeded deterministically. Cover `createReservation` success path, insufficient-stock 409 path, serial selection, cancellation, and restock.
- Concurrency tests: create tests that run N threads attempting to reserve overlapping quantities for the same SKU. Use `CountDownLatch` to start threads simultaneously and assert:
  - No `part.quantityOnHand` becomes negative
  - Sum of successful reserved quantities + quantityOnHand == initial quantity
  - Exactly one thread receives `InsufficientStockException` when applicable
- Deterministic simulation: seed small SKUs with low quantities (e.g., 3) and run many threads requesting 1 unit each to reproduce race conditions. Assert stable final state after joins.

4) Non-functional requirements (NFRs)

- Thread-safety: all mutations to inventory and reservations must be synchronized per-`PartRecord`; reads may be lock-free but must use up-to-date `AtomicInteger` values.
- Testability: services must accept a `MockDataStore` instance via constructor injection (no static singletons) to allow deterministic tests.
- Performance: in-memory operations expected to handle demo-scale concurrency (tens to low hundreds of concurrent requests). The per-SKU synchronized lock avoids global contention; avoid serializing all inventory operations.
- Observability: service methods should log reservation attempts, successes, failures, and restocks with SKU and requestId to aid debugging in tests and demos.
- Non-production constraints: the system is intentionally in-memory. Data is ephemeral; operations are not durable and should not be relied upon for production correctness. If durability is required, record a decision under `pipeline/decisions/` and plan a migration.

5) Migration-safety notes

Follow the migration-safety guidance at [.claude/skills/migration-safety-skill/SKILL.md](.claude/skills/migration-safety-skill/SKILL.md).
Any change to the shape of the following models requires a migration-safety follow-up and update to seed data and consumers:
- `Part` (renaming `sku`, changing `serialTracked` semantics)
- `Reservation` (adding/removing fields such as `serials`, `reservedBy`, `reservedAt`)
- `PartRecord` (changing how quantity is represented, e.g., switching from integer to complex availability map)

Specific actions when changing model shape
- Additive-first changes only; populate new fields in `MockDataStore` seeds for all existing records.
- Grep for model class names and DTO names across backend and frontend before merging.
- If serial-tracking is converted from optional to mandatory, update frontend UI and tests and record a decision in `pipeline/decisions/` because behavior changes materially.

6) Risks and mitigation

- Race conditions on concurrent reservations for the same SKU — mitigated by per-`PartRecord` synchronization and comprehensive concurrency tests.
- Developers forget to update seeded data after model changes — mitigated by migration-safety checklist and automated grep in CI if possible.
- Serial-tracking complexity grows and diverges from quantity-tracked logic — mitigate by keeping both flows explicit in `ReservationService` and unit-tested separately.
- In-memory limits cause surprising test flakiness at high concurrency — mitigate by limiting concurrency in CI tests and documenting that stress testing is out-of-scope for in-memory mode.

Handoff update (for Design Review Agent)
- Status: Architecture Complete
- Owner: Design Review Agent
- Artifacts:
  - [pipeline/architecture/feature-2-architecture.md](pipeline/architecture/feature-2-architecture.md)
  - [pipeline/features/feature-2-parts-reservation.md](pipeline/features/feature-2-parts-reservation.md)
  - [pipeline/decisions/feature-1-decisions.md](pipeline/decisions/feature-1-decisions.md)
  - [.claude/skills/migration-safety-skill/SKILL.md](.claude/skills/migration-safety-skill/SKILL.md)

Concise risks (for reviewers):
- concurrent reservations race (per-SKU locking proposed)
- model shape changes require migration-safety steps
- in-memory store not durable; future DB decision will require data migration

-- END
Feature 2 — Parts Reservation
Architecture Design

Source spec: [pipeline/features/feature-2-parts-reservation.md](pipeline/features/feature-2-parts-reservation.md)
Related decision: [pipeline/decisions/feature-1-decisions.md](pipeline/decisions/feature-1-decisions.md)
Migration guidance: [.claude/skills/migration-safety-skill/SKILL.md](.claude/skills/migration-safety-skill/SKILL.md)

1) High-level component diagram (textual)

- Frontend
  - `PartsInventoryComponent` (views inventory, search, SKU detail)
  - `PartReservationDialog` (select quantity/serials and submit reservation)
  - `JobPartsPanel` (integrated into job screen to list/create/cancel reservations)
  - `PartsServiceClient` (Angular service that calls backend REST endpoints)

- Backend
  - Controller layer
    - `PartsController` — exposes REST endpoints: GET /api/parts, GET /api/parts/{sku}, POST /api/parts/reserve, GET /api/jobs/{jobId}/parts, DELETE /api/parts/reservations/{reservationId}, POST /api/parts/{sku}/restock
  - Service layer
    - `PartsInventoryService` — read-only inventory operations, search, SKU metadata
    - `ReservationService` — reservation lifecycle: create, list for job, cancel, restock effects
  - Data layer (mocked)
    - `MockDataStore` extension: add parts inventory collections and reservation store
    - In-memory domain records: `PartRecord` (quantity, serials, flags), `ReservationRecord`
  - DTOs / API models
    - `PartDto`, `PartDetailDto`, `ReservationDto`, `ReservationCreateRequest`, `RestockRequest`, `ApiError`

Interaction flow (create reservation): `PartsController` -> `ReservationService.createReservation()` -> transactional check-and-decrement on `MockDataStore`/`PartRecord` -> on success persist `ReservationRecord` in `MockDataStore` and return `ReservationDto` (201). On failure return 409 with `ApiError`.

2) Package-by-layer layout and class names

Backend (Java, package-by-layer):
- com.serviceforge.model.parts
  - Part.java (fields: `sku`, `description`, `serialTracked` (boolean), `minimumStockThreshold`)
  - PartSerial.java (fields: `serial`, `sku`, `status`)
  - Reservation.java (fields: `id`, `jobId`, `sku`, `quantity`, `serials` (optional list), `reservedBy`, `reservedAt`, `status`)

- com.serviceforge.data
  - MockDataStore.java (extend existing store; add `ConcurrentHashMap<String, PartRecord> partsBySku`, `ConcurrentHashMap<String, Reservation> reservationsById`, helper seed methods)
  - PartRecord.java (in-memory representation: `AtomicInteger quantityOnHand`, `ConcurrentLinkedDeque<String> availableSerials`, metadata)

- com.serviceforge.service
  - PartsInventoryService.java (list/search/get SKU details)
  - ReservationService.java (createReservation, listByJob, cancelReservation, restock)

- com.serviceforge.controller
  - PartsController.java (REST endpoints and request validation)

- com.serviceforge.dto
  - PartDto.java, PartDetailDto.java, ReservationDto.java
  - ReservationCreateRequest.java (jobId, sku, quantity, optional serials[])
  - RestockRequest.java (quantity)
  - ApiError.java (standard error shape used across backend)

Frontend (Angular):
- src/app/components/parts-inventory/PartsInventoryComponent
- src/app/components/part-reservation-dialog/PartReservationDialog
- src/app/components/job-parts-panel/JobPartsPanel
- src/app/services/parts-api.service.ts (HTTP client wrapper returning typed DTOs)
- src/app/models/part.model.ts, reservation.model.ts

3) Detailed concurrency / mocking approach

Design goals: deterministic, thread-safe mutations on the in-memory store; behavior should match a simple transactional check-and-decrement semantics.

Data structures
- Use `ConcurrentHashMap<String, PartRecord> partsBySku` in `MockDataStore`.
- Implement `PartRecord` with `AtomicInteger quantityOnHand` and, for serial-tracked SKUs, `ConcurrentLinkedDeque<String> availableSerials`.

Atomic reservation algorithm
- `ReservationService.createReservation(ReservationCreateRequest req)` should:
  1. Validate request (`quantity > 0`, SKU exists, serial list consistency for serial-tracked SKUs).
  2. Locate `PartRecord part = mockDataStore.getPartRecord(req.getSku())`.
  3. Acquire intrinsic lock on `part` (synchronized on `part`) to serialize modifications for that SKU only:
     - Within the synchronized block perform check: if `part.quantityOnHand.get() < req.quantity` then throw `InsufficientStockException`.
     - If serial-tracked, ensure `availableSerials.size() >= req.quantity` and remove the exact serials (or auto-assign by polling `availableSerials`).
     - Otherwise call `part.quantityOnHand.addAndGet(-req.quantity)`.
     - Create `Reservation` with status `ACTIVE` and store it in `MockDataStore.reservationsById`.
  4. Return created `ReservationDto`.

Rationale: synchronizing on the `PartRecord` instance limits lock contention to the affected SKU and avoids coarse global locking. Using `AtomicInteger` provides safe arithmetic for concurrent reads and is used alongside the synchronized block for clarity and safety.

Cancellation and restock
- `cancelReservation` should also synchronize on the associated `PartRecord` to restore quantity and (for serials) push serials back into `availableSerials` in a deterministic order; mark reservation status `CANCELLED` and append to reservation history.
- `restock` (admin) must synchronize on `PartRecord` and call `quantityOnHand.addAndGet(+N)` and add serials when provided.

MockDataStore seeding
- Seed SKUs SKU-1000..SKU-1100 with randomized non-zero `quantityOnHand` and mark a few SKUs as `serialTracked = true` with pre-populated `availableSerials` lists. Use ISO-8601 timestamps for seeded `reservedAt` where necessary.

Testing guidance
- Unit tests: mock or use an in-memory `MockDataStore` instance seeded deterministically. Cover `createReservation` success path, insufficient-stock 409 path, serial selection, cancellation, and restock.
- Concurrency tests: create tests that run N threads attempting to reserve overlapping quantities for the same SKU. Use `CountDownLatch` to start threads simultaneously and assert:
  - No `part.quantityOnHand` becomes negative
  - Sum of successful reserved quantities + quantityOnHand == initial quantity
  - Exactly one thread receives `InsufficientStockException` when applicable
- Deterministic simulation: seed small SKUs with low quantities (e.g., 3) and run many threads requesting 1 unit each to reproduce race conditions. Assert stable final state after joins.

4) Non-functional requirements (NFRs)

- Thread-safety: all mutations to inventory and reservations must be synchronized per-`PartRecord`; reads may be lock-free but must use up-to-date `AtomicInteger` values.
- Testability: services must accept a `MockDataStore` instance via constructor injection (no static singletons) to allow deterministic tests.
- Performance: in-memory operations expected to handle demo-scale concurrency (tens to low hundreds of concurrent requests). The per-SKU synchronized lock avoids global contention; avoid serializing all inventory operations.
- Observability: service methods should log reservation attempts, successes, failures, and restocks with SKU and requestId to aid debugging in tests and demos.
- Non-production constraints: the system is intentionally in-memory. Data is ephemeral; operations are not durable and should not be relied upon for production correctness. If durability is required, record a decision under `pipeline/decisions/` and plan a migration.

5) Migration-safety notes

Follow the migration-safety guidance at [.claude/skills/migration-safety-skill/SKILL.md](.claude/skills/migration-safety-skill/SKILL.md).
Any change to the shape of the following models requires a migration-safety follow-up and update to seed data and consumers:
- `Part` (renaming `sku`, changing `serialTracked` semantics)
- `Reservation` (adding/removing fields such as `serials`, `reservedBy`, `reservedAt`)
- `PartRecord` (changing how quantity is represented, e.g., switching from integer to complex availability map)

Specific actions when changing model shape
- Additive-first changes only; populate new fields in `MockDataStore` seeds for all existing records.
- Grep for model class names and DTO names across backend and frontend before merging.
- If serial-tracking is converted from optional to mandatory, update frontend UI and tests and record a decision in `pipeline/decisions/` because behavior changes materially.

6) Risks and mitigation

- Race conditions on concurrent reservations for the same SKU — mitigated by per-`PartRecord` synchronization and comprehensive concurrency tests.
- Developers forget to update seeded data after model changes — mitigated by migration-safety checklist and automated grep in CI if possible.
- Serial-tracking complexity grows and diverges from quantity-tracked logic — mitigate by keeping both flows explicit in `ReservationService` and unit-tested separately.
- In-memory limits cause surprising test flakiness at high concurrency — mitigate by limiting concurrency in CI tests and documenting that stress testing is out-of-scope for in-memory mode.

Handoff update (for Design Review Agent)
- Status: Architecture Complete
- Owner: Design Review Agent
- Artifacts:
  - [pipeline/architecture/feature-2-architecture.md](pipeline/architecture/feature-2-architecture.md)
  - [pipeline/features/feature-2-parts-reservation.md](pipeline/features/feature-2-parts-reservation.md)
  - [pipeline/decisions/feature-1-decisions.md](pipeline/decisions/feature-1-decisions.md)
  - [.claude/skills/migration-safety-skill/SKILL.md](.claude/skills/migration-safety-skill/SKILL.md)

Concise risks (for reviewers):
- concurrent reservations race (per-SKU locking proposed)
- model shape changes require migration-safety steps
- in-memory store not durable; future DB decision will require data migration

End of architecture document.
