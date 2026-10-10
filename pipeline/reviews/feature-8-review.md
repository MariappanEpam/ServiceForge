# Feature 8 — Design Review

> Canonical copy for the orchestration workflow.
> Source of truth content is maintained in: `pipeline/design-review-logs/feature-8-2026-10-08.md`

---

# Design Review Log — Feature 8 (Job ↔ Parts Mapping & UI Control Conditions)

Date: 2026-10-08

## Inputs reviewed
- Spec: pipeline/features/feature-8-job-parts-mapping-and-ui-conditions.md
- Architecture: pipeline/architecture-design/feature-8-architecture.md
- Rules:
  - pipeline/rules/parts-reservation-against-job.md
  - pipeline/rules/parts-count-inventory.md
  - pipeline/rules/navigation-site-map.md
  - pipeline/rules/how-controls-should-look-like.md

## Key findings

### 1) Rule alignment: job-mandatory + immutability
- Spec and architecture correctly enforce:
  - `jobId` required on create
  - validate job existence
  - prevent `jobId` changes
- Note: If the system has no reservation update endpoint, immutability is naturally satisfied; do not add an update endpoint unless required.

### 2) API drift risk (Feature 2 vs Feature 4)
- Existing specs define different reservation endpoint shapes:
  - Feature 2 uses `/api/parts/reserve` and `/api/jobs/{jobId}/parts`
  - Feature 4 uses `/api/jobs/{jobId}/parts-reservations`
- Architecture calls out the need to select a canonical set for UI and keep compatibility if both exist.

### 3) Job status rules ambiguity
- Rule PR-006 requires “job status must allow parts reservation”, but job statuses are not defined in the rule.
- Spec includes an explicit assumption; implementation should confirm actual statuses and centralize the allow/deny logic in backend service.

### 4) UI control conditions are implementable and testable
- Conditions are expressed in a way that can be unit-tested (view-model state + bindings).
- Ensure disabled reasons are user-visible and consistent.

## Required changes before implementation
- None (no critical blockers).

## Recommended changes (non-blocking)
- In the spec, explicitly name the canonical reservation endpoints to avoid ambiguity during implementation.
- Add a short “job status allowlist” section once actual statuses are confirmed.

## Approval status
APPROVED
