# Feature 5 — Decision Log

Decisions made while building Feature 5 (SQLite persistence) that later features may need to retrieve.

## Decision: SQLite is the only allowed persistent datastore

**What was decided:** ServiceForge may use SQLite for persistence, and it must be a **local file** by default under the repo workspace.

**Constraints:**
- No external DB services.
- No networked DB endpoints.
- No cloud-managed databases.

**Why it matters:** later features must not introduce Postgres/MySQL/cloud DB dependencies without an explicit new decision.

## Decision: schema must be versioned via migrations

**What was decided:** schema is managed via versioned migrations (e.g., Flyway/Liquibase). Auto-creating schema without migrations is not allowed.

**Why it matters:** later features must add migrations for schema changes and keep startup fail-fast on incompatibility.

## Decision: seed modes are explicit (none/baseline/reset)

**What was decided:** the app supports explicit seed modes:
- `none` (no seed)
- `baseline` (minimal deterministic dataset)
- `reset` (delete/replace local DB and seed baseline; local demo only)

**Why it matters:** prevents flaky tests and accidental data loss in CI.

## Decision: API compatibility is preserved

**What was decided:** switching to SQLite must not change existing REST endpoint paths, DTO shapes, or error semantics.

**Why it matters:** frontend and existing feature specs assume stable contracts.
