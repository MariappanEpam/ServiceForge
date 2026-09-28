# Project decisions (cross-feature)

## 2026-09-27 — Allow SQLite persistence (training override)

### Decision
Enable **SQLite** as an optional persistence layer for:
- jobs
- technicians
- parts inventory
- job↔technician assignments
- unassigned jobs

### Rationale
- Reduce token usage by persisting operational state outside chat.
- Support repeatable demos across restarts.

### Constraints / Notes
- This repo originally used in-memory mock data only.
- SQLite is now the persistence option; keep it local and simple.
- Schema is auto-created on startup (no migrations).

### Configuration
- `SERVICEFORGE_PERSISTENCE=sqlite`
- `SERVICEFORGE_SQLITE_PATH=backend/.data/serviceforge.db`

### Consequences
- Backend services must read/write via a persistence abstraction.
- Tests should run in in-memory mode by default.
