# Implementation Plan — Feature 9: Technician onboarding and management

## Executive Summary

Deliver the technician master-management workflow (list/search/filter, create/edit, and soft deactivate/reactivate) using the approved additive API/data contract. The approved review is **APPROVED (with minor comments)**. This plan resolves its persistence confirmation using repository evidence: target the primary ServiceForge-Start application at the repository root, whose documented stack is Java 17 + Spring Boot 3 + Angular and whose technician persistence is already local-file SQLite through JDBC/Flyway. Do not target the separate `ServiceForge-Migrated/` forward-engineering application (Java 21/React/Entra); it is a distinct target stack, not this feature's implementation scope.

### Scope
- Technician management fields and behavior from the approved spec; retain existing technician identifiers and `GET /api/technicians` compatibility.
- Local SQLite only, with versioned Flyway schema changes where needed; no external services or authentication work.
- Skills remain a trimmed/deduplicated list of strings; no catalog. Age is an integer per spec.

### Assumptions and constraints
- Repository evidence identifies root `backend/` as the canonical application and identifies `JdbcTechnicianRepository`, Flyway migrations, SQLite configuration, and migration `V3__technicians_profile_fields.sql` for technician profile fields. Feature 9 decisions also select SQLite/Flyway/JDBC.
- Existing technicians may retain null age and area served; only create/update require the fields, per Feature 9 decisions. Do not add a forced seed backfill.
- Keep changes additive and preserve Feature 1 consumers. No application code changes are part of this planning task.

## Persistence confirmation (review action resolved)

**Decision for implementation: primary root application (`backend/`, not `ServiceForge-Migrated/`), using its local SQLite/JDBC/Flyway path.** The repository system-overview artifacts identify Java 17 + Spring Boot 3 at root and cite `backend/src/main/resources/application.yml`, `backend/pom.xml`, `backend/src/main/java/com/serviceforge/data/JdbcTechnicianRepository.java`, and `backend/src/main/resources/db/migration/V3__technicians_profile_fields.sql`. The persistence-entities overview records V3's additive `active`, `age`, `area_served`, vehicle, and `skills_csv` columns. `pipeline/decisions/feature-9-decisions.md` independently records the same SQLite/Flyway/JDBC choice and nullable legacy age/area. By contrast, `ServiceForge-Migrated/` contains a separate Java 21 / React / Entra target app with its own repositories and schema. It is not the Feature 9 target.

**Discovery exit:** persistence ambiguity is closed. Before implementation, confirm current migration/repository contents in the checked-out root backend and reuse the existing V3/profile-field implementation if present; do not create a duplicate migration or duplicate persistence path.

## Development Phases

### Phase 0 — Target and contract confirmation
- **Objective:** Start in the correct app and establish the current root backend/frontend contract.
- **Deliverables:** Confirm root technician controller/service/repository/DTO and `GET /api/technicians` consumers; verify V3 and existing profile field mappings; record affected files; confirm null legacy age/area behavior and seed compatibility.
- **Exit criteria:** Root Java 17/Angular target confirmed; no duplicate schema migration planned; consumer-impact list recorded.

### Phase 1 — Backend contract and persistence
- **Objective:** Complete profile CRUD and lifecycle behavior through the established root application layers.
- **Deliverables:** Additive DTO/model mapping; list query by name and `active|inactive|all`; create/get/update; soft deactivate/reactivate; server-side validation and existing `ApiError` semantics. Reuse the root JDBC repository and V3 schema; only add a new migration if verified missing schema requires one.
- **Validation:** Name required, trimmed, length 1–100; age required on create/update, integer 16–80 inclusive; area served required and length-limited; optional vehicle fields length-limited; skills optional, trimmed, deduplicated, 1–50 chars each, at most 50 entries.
- **Exit criteria:** API tests cover success, invalid input, missing IDs, lifecycle transitions, search/status filters, and persistence across reload/restart where integration setup permits.

### Phase 2 — Angular management workflow
- **Objective:** Provide the admin workflow without putting HTTP/business logic in components.
- **Deliverables:** Management route/page; list with name search and status filter; create/edit form; skills add/remove; vehicle and area fields; confirmation and status actions; clear validation, loading, empty, and API-error states. Route HTTP through the Angular API service.
- **Exit criteria:** UI behavior works against the root API; keyboard-accessible labels and skill controls are present; focused component tests/build pass.

### Phase 3 — Regression, release readiness, and evidence
- **Objective:** Demonstrate AC coverage and protect existing technician consumers.
- **Deliverables:** Feature 1 availability/calendar and job-booking regression checks; API/UI tests mapped to AC-9.1–AC-9.10; test report and handoff updated; migration/seed behavior documented.
- **Exit criteria:** Existing list consumers remain compatible, inactive filtering works, and no schema or API contract regression remains.

## Epics, Features, and Stories

### Epic: EPIC: Feature 9 — Technician onboarding and management
- **Goal:** Maintain complete technician profiles and operational activation status.
- **Feature 9.1 — Find technicians:** search by name and filter by active status.
  - **Story US-9.1:** As an admin, view/search/filter technician records with status and profile summaries.
  - **Acceptance:** AC-9.1; list response remains compatible with existing consumers.
- **Feature 9.2 — Maintain technician profile:** create and edit technician profile fields.
  - **Stories US-9.2 / US-9.3:** As an admin, create and update name, age, area, vehicle details, and skills.
  - **Acceptance:** AC-9.2–AC-9.5, AC-9.8–AC-9.10; 400 validation and 404 not-found behavior use `ApiError`.
- **Feature 9.3 — Control active status:** soft-deactivate and reactivate records.
  - **Story US-9.4:** As an admin, change technician operational status without deleting the record.
  - **Acceptance:** AC-9.6–AC-9.7; status persists and the record remains visible under the matching filter.

## Technical Tasks and ownership

| Sequence | Task | Component | Owner type | Completion evidence |
|---|---|---|---|---|
| 0.1 | Confirm root target, current API contract, model consumers, and migration V3/repository status; explicitly exclude migrated app | Backend + frontend discovery | Developer | Affected-component checklist; no duplicated migration |
| 1.1 | Complete additive technician model/DTO/repository mapping using existing SQLite profile columns | Backend data/model/DTO | Backend developer | Repository/service tests; stable existing JSON fields |
| 1.2 | Implement list query, get/create/update and activate/deactivate operations | Controller/service | Backend developer | Controller/service tests for ACs 9.1–9.7 |
| 1.3 | Apply authoritative validation and normalize/dedupe skills | Service/validation | Backend developer | Boundary, invalid, missing and normalization tests |
| 1.4 | Verify SQLite persistence, legacy null values, migration ordering, and seed modes | Repository/migrations | Backend developer + tester | SQLite integration evidence; no unnecessary backfill |
| 2.1 | Add management route/page and API-service operations | Angular route/service | Frontend developer | Service tests; route loads successfully |
| 2.2 | Build filterable list and create/edit form, including skills and status actions | Angular components | Frontend developer | Component/UI tests for ACs 9.1–9.10 |
| 2.3 | Implement accessibility and loading/empty/error/not-found states | Angular components | Frontend developer + tester | Keyboard and negative-path checks |
| 3.1 | Run Feature 1/calendar and job-booking regression suite | Full stack | Tester | Regression results and API compatibility evidence |
| 3.2 | Map tests/evidence to ACs; update release handoff | Pipeline artifacts | Tester / planner | Completed AC matrix and updated handoff |

## Dependency Matrix

| Dependency | Impact | Resolution |
|---|---|---|
| Feature 1 technician model and endpoints → Feature 9 | Existing availability and booking consumers may break | Add fields only; preserve IDs and existing response fields; run regression suite |
| Root SQLite schema V3 → profile repository mappings | Mismatched/missing columns could block profile persistence | Verify root migration/repository; reuse existing schema; only create a migration if evidence shows a gap |
| Backend API/service → Angular API service and forms | UI depends on stable DTO/error contract | Deliver API contract and tests before UI integration |
| Existing technician records with null age/area → list/edit behavior | Legacy records must remain readable although create/update require values | Preserve null for legacy records; validate required fields on writes; avoid forced backfill |

## Risks and mitigations

- **Wrong application target (high):** Separate migrated app has parallel SQLite repos. **Mitigation:** Feature 9 is scoped to root Java 17/Spring Boot + Angular application; do not implement against `ServiceForge-Migrated/`.
- **Breaking Feature 1 data consumers (high):** model growth can change assumptions. **Mitigation:** additive-only DTO/model changes; search all consumers; regression-test calendar and booking.
- **Schema duplication/drift (medium):** profile migration V3 is already documented. **Mitigation:** inspect and reuse existing V3/repository mapping; never add a duplicate migration.
- **Legacy null values (medium):** seeded/persisted records may lack newly required-on-write fields. **Mitigation:** permit null when reading existing rows and require on create/update, matching Feature 9 decisions.
- **Age and free-text skill semantics (minor review comments):** integer age ages over time and free text can vary. **Mitigation:** retain approved scope, normalize/trim/dedupe skills; DOB/catalog are future features, not this delivery.

## Suggested Delivery Plan

1. **Sprint 1 — Persistence and API:** Phase 0 verification, backend mappings/endpoints, validation, repository/API tests.
2. **Sprint 2 — Management UI:** route, list/filter, create/edit form, skill controls, lifecycle actions, UI tests.
3. **Sprint 3 — Release confidence:** AC traceability, Feature 1 regression, accessibility/error-path checks, handoff and test evidence.

## Traceability and source artifacts

- Spec: `pipeline/features/feature-9-technician-onboarding-and-management.md`
- Architecture: `pipeline/architecture/feature-9-architecture.md`
- Design review: `pipeline/reviews/feature-9-review.md`
- Persistence decision: `pipeline/decisions/feature-9-decisions.md` and `pipeline/decisions/project-decisions.md`
- Repository evidence: `pipeline/repo-understanding/serviceforge-system-overview/technology-context.md`, `persistence-entities.md`, `c4-as-is/03-containers.md`, and `c4-as-is/04-components-backend.md`
- Handoff: `pipeline/handoffs/feature-9-handoff.md`

## OpenProject publication

Publication is required by the planner workflow. This session's available tools do not expose the required OpenProject MCP operations, so work-package existence and description updates could not be verified or performed. Previously recorded work-package URLs are retained in the Feature 9 handoff as historical references pending MCP verification.
