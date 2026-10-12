# Feature 5 — SQLite persistence (replace MockDataStore): Implementation Plan

## Executive Summary

Implement local-only SQLite persistence for the backend, replacing `MockDataStore`, while preserving existing REST API behavior for:

- Technician Availability Calendar
- Parts reservation by technician

Add schema migrations, deterministic seeding, and tests validating persistence and concurrency.

## Inputs

- Spec: [pipeline/features/feature-5-sqlite-persistence.md](../features/feature-5-sqlite-persistence.md)
- Architecture: [pipeline/architecture/feature-5-architecture.md](../architecture/feature-5-architecture.md)
- Review: [pipeline/reviews/feature-5-review.md](../reviews/feature-5-review.md)
- Decisions: [pipeline/decisions/project-decisions.md](../decisions/project-decisions.md), [pipeline/decisions/feature-5-decisions.md](../decisions/feature-5-decisions.md)

## Delivery phases

### Phase 0 — Repo wiring (dependencies + config)

Backend tasks:
- Add SQLite JDBC driver dependency.
- Add Spring Data (JDBC preferred) dependency.
- Add Flyway (or Liquibase) dependency.
- Add application configuration:
  - `SERVICEFORGE_SQLITE_PATH`
  - `SERVICEFORGE_DB_SEED`

Acceptance:
- App starts and connects to a local SQLite file.
- Migrations run on startup.

### Phase 1 — Schema + migrations

Tasks:
- Create initial migration:
  - `technicians`, `jobs`, `parts`, `job_part_reservations`
  - indexes
  - optional CHECK constraints

Acceptance:
- Fresh DB can be created solely via migrations.
- Startup fails fast if migration fails.

### Phase 2 — Seed strategy implementation

Tasks:
- Implement seed modes:
  - `none`: no seed
  - `baseline`: seed if tables empty
  - `reset`: delete DB file then migrate + seed (guarded; not in CI)
- Ensure baseline seed uses stable IDs and SKUs.

Acceptance:
- Baseline seed produces deterministic dataset.
- Reset mode requires explicit opt-in and is blocked in CI.

### Phase 3 — Repository layer

Tasks:
- Create repository interfaces for:
  - technicians
  - jobs
  - parts
  - reservations
- Map persistence entities to existing domain/DTOs.

Acceptance:
- Services can read/write via repositories without `MockDataStore`.

### Phase 4 — Service refactor (preserve business rules)

Technician availability:
- Refactor `TechnicianAvailabilityService` to use repositories.
- Preserve travel buffer policy and overlap rules.

Parts reservation:
- Refactor reservation service to use DB transactions.
- Implement conditional inventory decrement:
  - update inventory only if sufficient quantity
  - insert reservation in same transaction
- Cancel reservation:
  - guard against double cancel
  - increment inventory in same transaction

Acceptance:
- Behavior matches existing specs (400/404/409 semantics).
- Concurrency safety holds.

### Phase 5 — Controllers and error mapping

Tasks:
- Keep endpoints unchanged.
- Ensure error mapping remains consistent with `ApiError` conventions.

Acceptance:
- No frontend changes required due to API drift.

### Phase 6 — Tests

Backend tests:
- Migration smoke test:
  - run migrations against a temp SQLite file
- Persistence integration tests:
  - create job, restart context (or re-open DB), verify job persists
  - create reservation, verify inventory decremented and persists
- Concurrency test:
  - concurrent reservation attempts for same SKU never result in negative inventory

Frontend tests:
- Only if any DTO shape changes (not expected).

Acceptance:
- Tests pass reliably and deterministically.

## Epics and backlog-ready stories

### Epic A — Persistence foundation
1. Add SQLite + migration tooling dependencies and config
2. Create initial schema migration
3. Implement seed modes (none/baseline/reset)

### Epic B — Replace MockDataStore with repositories
4. Implement repositories for technicians/jobs
5. Implement repositories for parts/reservations
6. Refactor services to use repositories

### Epic C — Transactional correctness
7. Implement conditional inventory decrement in a transaction
8. Implement cancel reservation transaction (idempotency rules)

### Epic D — Quality
9. Migration smoke test
10. Persistence integration tests
11. Concurrency test for reservations

## Notes on Spring Data JDBC vs JPA

- Prefer Spring Data JDBC for this repo’s simplicity.
- If JPA is already present, keep it consistent and avoid mixing persistence paradigms.
