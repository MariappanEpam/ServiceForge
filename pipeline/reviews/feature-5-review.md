# Design Review — Feature 5: SQLite persistence (replace MockDataStore)

- **Reviewed design:** [pipeline/architecture/feature-5-architecture.md](../architecture/feature-5-architecture.md)
- **Against spec:** [pipeline/features/feature-5-sqlite-persistence.md](../features/feature-5-sqlite-persistence.md)
- **Review date:** 2026-10-05

## 1) Executive summary

The design meets the intent: local-only SQLite persistence with schema versioning and a clear seed strategy, while preserving existing API behavior.

Key strengths:
- Conditional update approach for inventory prevents over-allocation without relying on unsupported SQLite row locks.
- Explicit migration requirement resolves prior “auto-create schema” ambiguity.
- Seed modes are explicit and test-friendly.

**Approval status:** **APPROVED**

## 2) Requirement coverage

✅ Covered:
- Persistence for technicians, jobs, parts, reservations
- Schema versioning via migrations
- Seed strategy (none/baseline/reset)
- Concurrency approach for reservations
- API compatibility preserved

⚠️ Clarifications (non-blocking):
- Confirm whether timestamps are stored as ISO-8601 TEXT consistently or as INTEGER epoch; either is acceptable but must be consistent.
- Confirm whether `technician_id` on `jobs` can be NULL (unassigned jobs) based on existing API behavior.

## 3) Risks & findings

### Medium — SQLite write contention under high concurrency
- SQLite serializes writes; concurrency tests may need tuning.
- Mitigation: keep transactions short; use conditional update; avoid long-running reads in the same transaction.

### Medium — Seed/reset mode safety
- `reset` mode can cause accidental data loss.
- Mitigation: require explicit opt-in via env var; document “local only”; consider refusing `reset` when `CI=true`.

### Low — Schema drift vs DTO drift
- If DTOs evolve without migrations, runtime errors will occur.
- Mitigation: enforce “migration required for model changes” via pipeline rule.

## 4) Recommendations

1. Add a CHECK constraint for `quantity_on_hand >= 0` if compatible with SQLite version.
2. Add a unique constraint or validation to prevent duplicate reservation IDs.
3. Add a migration smoke test that runs migrations against a temp DB file.

## 5) Decision

APPROVED
