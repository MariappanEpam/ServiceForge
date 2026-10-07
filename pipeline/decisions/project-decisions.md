# Project decisions (cross-feature)

## 2026-10-05 — Allow SQLite persistence (Feature 5)

### Decision
ServiceForge is allowed to use **SQLite** as its persistence layer, replacing the standing “mock/in-memory only” constraint.

This is a **local-only** database used for training/demo persistence and integration testing.

### Scope (what must be persisted)
- technicians
- jobs + job assignments
- parts catalog/inventory
- job parts reservations

### Constraints
1. **Local file only**
	- SQLite must be a local file on disk.
	- The DB file must live under the repo workspace by default (configurable path).
	- No external DB services (cloud DBs, hosted DBs, networked DB endpoints).

2. **No external dependencies**
	- No reliance on external services for migrations, seeding, or runtime.

3. **Schema versioning is required**
	- Schema must be managed via versioned migrations (e.g., Flyway/Liquibase).
	- “Auto-create schema with no migrations” is no longer acceptable.
	- Startup must fail fast on incompatible schema.

4. **Seed data strategy is explicit and controllable**
	- Deterministic seed for tests.
	- Optional baseline seed for demos.
	- A reset/reseed mode may exist for local demos but must not be used in CI.

5. **Compatibility constraints**
	- Existing REST API contracts must remain stable unless a future feature explicitly changes them.
	- Seeded IDs used by UI/tests must remain stable.

### Configuration (proposed)
- `SERVICEFORGE_PERSISTENCE=sqlite`
- `SERVICEFORGE_SQLITE_PATH=backend/.data/serviceforge.db`
- `SERVICEFORGE_DB_SEED=none|baseline|reset`

### Consequences
- Backend services must read/write via repositories (not `MockDataStore`).
- Tests should default to SQLite (ephemeral temp DB) for integration coverage, with an option to run fast unit tests without DB where appropriate.

