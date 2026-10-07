# Feature 5 — SQLite persistence (replace MockDataStore)

Architecture Design

Source spec: [pipeline/features/feature-5-sqlite-persistence.md](../features/feature-5-sqlite-persistence.md)
Related decisions:
- [pipeline/decisions/project-decisions.md](../decisions/project-decisions.md)
- [pipeline/decisions/feature-5-decisions.md](../decisions/feature-5-decisions.md)

## 1) Architecture overview

Replace the in-memory `MockDataStore` with a persistent SQLite-backed data layer while keeping:

- Controller endpoints unchanged.
- Service-layer business rules unchanged (overlap booking rules, travel buffer, strict inventory enforcement).
- Frontend unchanged (unless DTOs/IDs require minor adjustments; not expected).

The backend will adopt a repository-based persistence abstraction and run schema migrations at startup.

## 2) Technology choices

### Persistence

**Recommended:** Spring Data JDBC + SQLite JDBC driver.

Rationale:
- Simpler mapping model than JPA for a training repo.
- Fewer surprises around lazy loading and proxies.
- Works well with explicit SQL migrations.

**Acceptable alternative:** Spring Data JPA if the repo already uses it elsewhere.

### Migrations

**Recommended:** Flyway.

Rationale:
- Simple, ordered SQL migrations.
- Clear schema versioning and fail-fast behavior.

## 3) Data model (relational)

This section defines the minimum schema needed to persist features implemented so far.

### Tables

1. `technicians`
   - `id` TEXT PRIMARY KEY
   - `name` TEXT NOT NULL
   - other display fields as needed by existing DTOs

2. `jobs`
   - `id` TEXT PRIMARY KEY
   - `technician_id` TEXT NULL REFERENCES technicians(id)
   - `start_time` TEXT NOT NULL  (ISO-8601)
   - `end_time` TEXT NOT NULL    (ISO-8601)
   - `status` TEXT NOT NULL

3. `parts`
   - `sku` TEXT PRIMARY KEY
   - `name` TEXT NOT NULL
   - `description` TEXT NULL
   - `quantity_on_hand` INTEGER NOT NULL
   - `active` INTEGER NOT NULL  (0/1)

4. `job_part_reservations`
   - `id` TEXT PRIMARY KEY
   - `job_id` TEXT NOT NULL REFERENCES jobs(id)
   - `sku` TEXT NOT NULL REFERENCES parts(sku)
   - `quantity_reserved` INTEGER NOT NULL
   - `reserved_by_technician_id` TEXT NOT NULL REFERENCES technicians(id)
   - `reserved_at` TEXT NOT NULL
   - `status` TEXT NOT NULL

### Indexes / constraints

- `jobs(technician_id, start_time)` index for listing jobs by technician.
- `job_part_reservations(job_id)` index for listing reservations by job.
- `job_part_reservations(sku)` index for reporting/lookup.

Inventory integrity:
- `parts.quantity_on_hand >= 0` enforced via application logic and optionally a CHECK constraint.

## 4) Transaction and concurrency strategy

### Parts reservation atomicity

Goal: prevent over-allocation under concurrent requests.

Approach:

- Use a single DB transaction for “check inventory → decrement → insert reservation”.
- Ensure the decrement is conditional.

Implementation options (choose one and document in code later):

1. **Optimistic conditional update** (preferred)
   - `UPDATE parts SET quantity_on_hand = quantity_on_hand - :qty WHERE sku = :sku AND quantity_on_hand >= :qty;`
   - Verify affected rows == 1; otherwise treat as insufficient stock (409).
   - Then insert reservation row.

2. **Pessimistic locking**
   - SQLite has limited row-level locking; avoid relying on `SELECT ... FOR UPDATE`.
   - Prefer conditional update semantics.

Cancel reservation:
- Transaction: mark reservation cancelled (guard against double cancel) and increment inventory.

### Job booking overlap enforcement

Overlap checks remain in service logic.

Data access must support:
- list jobs by technician within a time window
- insert job

## 5) Application layering changes

### Controllers

No endpoint changes.

### Services

Services keep business rules but switch dependencies:

- From: `MockDataStore`
- To: repositories (e.g., `TechnicianRepository`, `JobRepository`, `PartRepository`, `JobPartReservationRepository`)

### Data layer

- Introduce repository interfaces and persistence entities.
- Remove or deprecate `MockDataStore` usage.

## 6) Configuration and runtime modes

### SQLite file location

- Default path: `backend/.data/serviceforge.db`
- Configurable via `SERVICEFORGE_SQLITE_PATH`.

### Seed modes

- `SERVICEFORGE_DB_SEED=none|baseline|reset`

Behavior:
- `none`: migrations only.
- `baseline`: migrations + insert baseline dataset if tables are empty.
- `reset`: delete DB file (local only) then migrations + baseline seed.

## 7) Migration strategy

- Use Flyway SQL migrations committed to the repo.
- First migration creates all tables and indexes.
- Subsequent migrations are additive by default.
- Startup fails if migrations cannot be applied.

## 8) Observability and safety

- Log DB path and seed mode at startup.
- Never log full SQL with user-provided values.
- Provide a clear error when DB file is not writable.

## 9) Risks and mitigations

1. SQLite locking/contention under concurrency tests
   - Mitigation: conditional update approach; keep transactions short.

2. Seed data drift breaking UI assumptions
   - Mitigation: deterministic baseline seed with stable IDs.

3. Migration mistakes
   - Mitigation: migration smoke test in CI; fail-fast startup.

Handoff update (for Design Review Agent)
- Status: Architecture Complete
- Owner: Design Review Agent
- Artifacts:
  - [pipeline/features/feature-5-sqlite-persistence.md](../features/feature-5-sqlite-persistence.md)
  - [pipeline/architecture/feature-5-architecture.md](feature-5-architecture.md)
