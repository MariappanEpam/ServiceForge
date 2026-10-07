# Local SQLite persistence only

## Purpose

Allow persistence for training/demo scenarios while preventing introduction of external infrastructure.

## Rule

1. ServiceForge may use **SQLite** as a persistent datastore.
2. The SQLite database must be a **local file** by default under the repo workspace.
3. **External databases and services are not allowed** (no hosted DBs, no cloud DBs, no network endpoints).
4. Schema must be managed via **versioned migrations** (Flyway/Liquibase). Auto-creating schema without migrations is not allowed.
5. Seed behavior must be explicit and controllable:
   - `none` (no seed)
   - `baseline` (deterministic baseline seed)
   - `reset` (local-only; must be blocked in CI)
6. Existing REST API contracts must remain stable unless a future feature explicitly changes them.

## Compatibility constraints

- Seeded IDs and SKUs used by UI/tests must remain stable.
- Concurrency-sensitive operations (e.g., parts reservation) must be transactional and must not allow negative inventory.
