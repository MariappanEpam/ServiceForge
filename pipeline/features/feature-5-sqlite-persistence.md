# Feature 5 — SQLite persistence (replace MockDataStore)

| Field | Value |
|---|---|
| **Status** | Draft |
| **One-line intent** | Replace the in-memory `MockDataStore` with a persistent, local-only SQLite datasource while preserving existing API behavior for Technician Availability and Parts Reservation |
| **Depends on** | Feature 1 (Technician Availability Calendar), Feature 4 (Parts reservation by technician) |
| **In scope** | Backend persistence for existing models and behaviors; schema versioning + migrations; seed strategy; test strategy; minimal/no frontend changes |
| **Out of scope** | External DB services; multi-node deployments; advanced auth/RBAC; data warehouse/analytics; offline-first mobile sync |
| **Definition of Done** | Existing endpoints behave the same across restarts, backed by SQLite; schema is versioned and migratable; seed strategy is deterministic and controllable; tests cover persistence + migration basics |

## Problem statement

ServiceForge currently relies on an in-memory `MockDataStore` and a standing “no real database” constraint. This prevents:

- Persisting demo state across restarts.
- Running realistic integration tests that validate data integrity.
- Evolving the domain model with explicit schema versioning.

This feature introduces **SQLite persistence** as the default local datasource and updates repo guardrails accordingly.

## Goals

1. Persist all data required by the features implemented so far:
   - Technician Availability Calendar (technicians, jobs, job assignments, job status, travel-buffer semantics).
   - Parts reservation by technician (parts catalog/inventory, reservations, reservation status).
2. Preserve existing REST API contracts and error semantics (400/404/409 with `ApiError`).
3. Provide a safe migration approach for schema evolution.
4. Provide a deterministic seed strategy for demos and tests.
5. Keep the solution **local-only** (no external services).

## Non-goals

- Running SQLite as a network service.
- Supporting Postgres/MySQL or cloud-managed databases.
- Multi-tenant data separation.
- High availability, replication, or clustering.
- Changing UI flows (unless required to handle new IDs/fields).

## Scope (backend)

### Persisted domain areas (existing features)

#### A) Technician availability calendar

Persist at minimum:

- `Technician`
  - `id`
  - display fields used by UI (name, etc.)
- `Job`
  - `id`
  - `technicianId` (assignment)
  - `startTime`, `endTime`
  - `status`
  - any fields already exposed by API DTOs

Behavioral constraints to preserve:

- No overlapping bookings for the same technician (including travel buffer policy from Feature 1 decisions).
- Listing technicians and a technician’s jobs returns the same shapes as before.

#### B) Parts reservation by technician

Persist at minimum:

- `Part`
  - `sku` (primary identifier)
  - `name`, `description` (if present)
  - `quantityOnHand`
  - `active`
- `JobPartReservation`
  - `id`
  - `jobId`
  - `sku`
  - `quantityReserved`
  - `reservedByTechnicianId`
  - `reservedAt`
  - `status` (ACTIVE/CANCELLED)

Behavioral constraints to preserve:

- Strict inventory enforcement (409 on insufficient stock) per Feature 4 spec.
- Concurrency safety: concurrent reservations must not over-allocate inventory.

### Persistence technology choice (decision to be recorded)

The backend must use one of:

- Spring Data JDBC (preferred for simplicity), or
- Spring Data JPA (acceptable if already present),

with SQLite JDBC driver.

The architecture doc will finalize the choice and justify it.

### Configuration

- SQLite database is a **local file** within the repo workspace (path configurable).
- Provide environment/property configuration for:
  - DB file path
  - seed mode
  - migration mode

## Migration approach (schema versioning)

### Requirements

1. Schema must be versioned.
2. Migrations must be repeatable and ordered.
3. App startup must fail fast if schema is incompatible.
4. Tests must validate at least one migration path.

### Proposed approach

- Use a migration tool (Flyway or Liquibase) with SQL migration scripts committed under backend resources.
- Maintain a `schema_version` table (tool-managed).
- Provide a “reset + reseed” mode for local demos.

### Backward compatibility expectations

- Additive changes should be the default.
- Destructive changes require explicit migration scripts and a decision entry.

## Seed data strategy

### Requirements

- Deterministic seed data for tests.
- Optional seed for demos.
- Ability to start with an empty DB.

### Modes

1. `seed=none` — do not seed.
2. `seed=baseline` — seed a minimal baseline dataset (technicians, a few jobs, parts catalog).
3. `seed=reset` — delete/replace local DB file and seed baseline (demo convenience; not for CI).

## API compatibility constraints

- Endpoint paths and response DTO shapes remain unchanged.
- Error semantics remain unchanged.
- IDs must remain stable and predictable where previously assumed by UI/tests.
  - If IDs were previously hard-coded in seed data, the seed must preserve them.

## Testing requirements

Backend tests must cover:

- CRUD flows used by existing endpoints against SQLite.
- Concurrency test for parts reservation (no negative inventory).
- Migration smoke test (apply migrations to empty DB).

Frontend:

- No required changes unless API shapes change (not expected).

## Risks / open questions

1. **Existing project decision conflict:** [pipeline/decisions/project-decisions.md](../decisions/project-decisions.md) currently states “Schema is auto-created on startup (no migrations)”. This feature requires schema versioning; decision must be updated.
2. **SQLite concurrency semantics:** ensure reservation create/cancel uses transactions and appropriate locking/constraints.
3. **Feature numbering:** Feature 5 is assumed next; adjust if a different numbering convention is required.
