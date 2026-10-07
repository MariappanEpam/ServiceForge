# Feature 6 — Design Review (Restore Technician Availability Calendar, SQLite-backed)

## Review summary
Design is acceptable and consistent with repo conventions (JdbcTemplate + Flyway + layered services).

## Strengths
- Keeps persistence approach consistent with Feature 5 (SQLite + Flyway + JdbcTemplate).
- Clear separation: controllers thin, services enforce rules, repositories handle SQL.
- Overlap rule is expressed as a single deterministic SQL predicate.

## Gaps / required adjustments
1. **Schema verification**
   - Confirm current `jobs` table includes `start_time` and `end_time`.
   - If missing, migration must be added and seeding updated.

2. **Error contract**
   - Ensure `ApiError` shape matches existing controllers.
   - Conflict response should include enough detail for UI (message + optional conflicting job id if available).

3. **Time handling**
   - Use `LocalDateTime` consistently.
   - Document that server-local time is used.

4. **Test determinism**
   - Tests must not depend on prior DB state.
   - Prefer clearing/reseeding baseline tables for test DB path (as done in Feature 5 work).

## Approval
Approved to proceed to implementation planning.
