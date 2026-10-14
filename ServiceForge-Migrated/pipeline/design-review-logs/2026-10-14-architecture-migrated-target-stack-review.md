# Design Review Log — architecture-migrated-target-stack.md (Features 1–3, ADR-0001..0003)

Date: 2026-10-14  
Reviewer: Design Review Agent  
Scope: ServiceForge-Migrated only

## Inputs reviewed
- Architecture: ServiceForge-Migrated/pipeline/architecture-design/architecture-migrated-target-stack.md
- Specs:
  - ServiceForge-Migrated/pipeline/features/feature-1-technician-availability-scheduling-view.md
  - ServiceForge-Migrated/pipeline/features/feature-2-job-booking.md
  - ServiceForge-Migrated/pipeline/features/feature-3-parts-catalog.md
- ADRs:
  - ServiceForge-Migrated/pipeline/decisions/adr-0001-target-tech-stack.md
  - ServiceForge-Migrated/pipeline/decisions/adr-0002-auth-azure-entra.md
  - ServiceForge-Migrated/pipeline/decisions/adr-0003-persistence-sqlite-local-only.md

## Summary
Overall, the architecture document is aligned with ADR-0001 (Java 21 + React + Entra + SQLite local-only) and correctly reflects the feature-level API surface for Features 1–3.

However, there are several **implementation-blocking ambiguities** that are currently marked NEEDS-EVIDENCE but are required to implement and test the migrated specs consistently (notably: schedule default window, time zone/precision, and canonical error contract including Spring Security 401/403 shape).

## Findings

### 1) Requirements / spec alignment
**PASS with gaps**
- ✅ Correctly captures the three feature flows and endpoints:
  - `GET /api/technicians`
  - `GET /api/technicians/{id}/jobs?from&to`
  - `POST /api/jobs`
  - `GET /api/parts`
- ✅ Correctly includes booking conflict rule with 45-minute buffer.
- ✅ Correctly includes parts filtering `active=1 AND orderable=1`.

**Gaps that must be resolved (BLOCKING):**
- **Default window behavior** for Feature 1 when `from`/`to` omitted is required by AC-7 but not defined.
- **Time handling policy** (timezone + precision) is required by Feature 2 boundary AC-8/AC-9.
- **ApiError contract** is referenced by all specs but not defined; additionally, Spring Security 401/403 responses may not match the same shape.

### 2) ADR alignment
**PASS**
- ✅ ADR-0001: stack matches (Java 21, React TS, Entra, SQLite local-only, Flyway).
- ✅ ADR-0002: OIDC Auth Code + PKCE on frontend; resource server JWT validation on backend.
- ✅ ADR-0003: SQLite local file + Flyway migrations; no cloud DB.

**Clarifications needed (BLOCKING):**
- ADR-0002 allows app roles and/or group claims; architecture recommends role names but does not define claim mapping strategy. Specs depend on dispatcher vs technician behavior.

### 3) API contract completeness
**PARTIAL**
- ✅ Status codes enumerated (400/401/403/404/409).
- ✅ Request/response DTO names are consistent with specs.

**Missing / ambiguous (BLOCKING):**
- Canonical `ApiError` schema (fields, error codes, validation details).
- Whether auth errors (401/403) must be wrapped into `ApiError` or can be Spring defaults.
- For `GET /api/technicians/{id}/jobs`, whether `from`/`to` are datetime or date-only; and whether inclusive/exclusive boundaries apply.

### 4) Data model / persistence
**PASS (within scope)**
- ✅ Uses SQLite local-only and Flyway.
- ✅ Baseline tables listed match the migrated specs.

**Non-blocking notes:**
- Consider explicitly stating that `jobs.start_time`/`end_time` are stored as ISO-8601 strings or epoch millis (SQLite typing nuance) to avoid inconsistent comparisons.

### 5) Security
**PARTIAL**
- ✅ JWT issuer/audience validation called out.
- ✅ Token storage guidance (avoid localStorage) is appropriate.

**Missing (BLOCKING for implementation readiness):**
- RBAC enforcement rules per endpoint are not finalized:
  - Can technicians call `GET /api/technicians`?
  - Can technicians view other technicians’ schedules?
  - Exact role names and claim mapping (app roles vs groups) for Spring Security.

### 6) NFRs / operational
**PASS (evidence-limited)**
- ✅ Correctly flags NFR baseline as evidence-limited.
- ✅ Notes SQLite locking risk.

## Required changes (must-do)
These are required to make the architecture implementation-ready for Features 1–3.

1) **Define schedule default window** for `GET /api/technicians/{id}/jobs` when `from`/`to` omitted.
   - Recommendation: default to **current week in user’s timezone** or **[now, now+7d] in UTC**; pick one and document.

2) **Define time policy**:
   - Timezone: UTC-only on the wire (recommended) OR explicit local timezone.
   - Precision: seconds vs minutes; must support Feature 2 AC-8/AC-9 boundary behavior.

3) **Define canonical error contract**:
   - Finalize `ApiError` fields.
   - Decide whether Spring Security 401/403 responses are adapted into `ApiError`.
   - Define validation error representation (field errors) for 400.

4) **Finalize RBAC rules and claim mapping**:
   - Decide app roles vs groups.
   - Define role names and mapping to Spring authorities.
   - Document endpoint authorization matrix.

## Recommended improvements (non-blocking)
- Add a short “API conventions” section: ISO-8601 format, inclusive/exclusive range semantics, and pagination conventions (even if not used yet).
- Add a “seed data” section clarifying whether demo seed is required and how it is triggered.

## Approval status
BLOCKED — required changes above must be incorporated into the architecture design to remove implementation ambiguity for Features 1–3.
