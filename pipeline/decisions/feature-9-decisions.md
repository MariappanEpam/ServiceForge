# Feature 9 Decisions — Technician onboarding and management

Date: 2026-10-11

## Decision 1 — Persistence mechanism
**Decision:** Use local-file SQLite persistence (Flyway migrations + JDBC repository) for technician profile fields.

**Rationale:**
- Aligns with repo rule allowing local-only SQLite persistence.
- Enables additive schema evolution via Flyway without breaking existing consumers.
- Supports CRUD + activate/deactivate endpoints with consistent behavior.

**Implementation notes:**
- Flyway migration added: `V3__technicians_profile_fields.sql`.
- Existing seed data remains valid; new fields are nullable where appropriate.

## Decision 2 — Age validation bounds
**Decision:** Enforce technician `age` bounds as **16..80 inclusive** on create/update.

**Rationale:**
- Matches updated Feature 9 spec.
- Provides clear boundary validation for UI and API.

## Decision 3 — Backward compatibility for existing technicians
**Decision:** Existing technicians may have `age` and `areaServed` as `null` until updated.

**Rationale:**
- Avoids forced backfill and keeps migration additive.
- Prevents breaking existing seed rows.

## Decision 4 — Skills storage format
**Decision:** Store skills as a CSV string in SQLite (`skills_csv`) and map to `List<String>` in API.

**Rationale:**
- Keeps schema simple for training boilerplate.
- Avoids introducing new join tables.

## Decision 5 — Vehicle details
**Decision:** Vehicle details are optional; stored as separate nullable columns.

**Rationale:**
- Supports partial technician profiles.
- Keeps API flexible.
