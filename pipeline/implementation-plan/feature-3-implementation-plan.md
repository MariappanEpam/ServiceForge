# Feature 3 — Technician Onboarding and Management: Implementation Plan

Summary
- Purpose: convert the approved Feature 3 spec and architecture into a prioritized implementation plan (epics, stories, tasks).
- Scope: backend mock-backed onboarding + lifecycle + booking guard, frontend screens, and tests.

Inputs
- Spec: [pipeline/features/feature-3-technician-onboarding-management.md](../features/feature-3-technician-onboarding-management.md)
- Architecture: [pipeline/architecture/feature-3-architecture.md](../architecture/feature-3-architecture.md)
- Review: [pipeline/reviews/feature-3-review.md](../reviews/feature-3-review.md)

Assumptions
- No real database; use `MockDataStore` only.
- Demo-only authorization via a simple role header (documented in API/README).

## Epics (priority order)

1) Backend — Domain model + mock store
- Add technician lifecycle status (additive-only) and invitation record storage.
- Seed at least 10 technicians across states.
- Acceptance: app starts with seeded technicians; no token data in list/detail.

2) Backend — Services (onboarding + lifecycle)
- Implement invite token generation (store hash only) and activation.
- Centralize lifecycle transition validation.
- Acceptance: invalid transitions return 409; token reuse rejected.

3) Backend — Booking guard integration
- Add eligibility check used by job booking/assignment flow.
- Acceptance: suspended/offboarded technicians cannot be assigned new jobs (409).

4) Backend — Controllers + DTO validation
- Implement endpoints listed in the spec.
- Add request/response DTOs and validation.
- Acceptance: 400/404/409 errors follow `ApiError` contract.

5) Frontend — Technician management UI
- Technician list with status filter.
- Technician detail/edit.
- Invite/reinvite action that shows token once.
- Activation screen (token entry).
- Acceptance: all HTTP calls via Angular service; components are thin.

6) Tests
- Unit tests for lifecycle transitions and token activation.
- Integration test for booking guard rejection.
- Acceptance: `mvn test` and `npm test` (if present) pass.

7) Documentation + handoff
- Update handoff status and record any decisions/constraints.
- Acceptance: pipeline artifacts link correctly and are consistent.

Estimates (rough)
- Backend: 3–5 days
- Frontend: 2–4 days
- Tests + polish: 1–2 days

Definition of Done (plan)
- All epics complete.
- Tests passing.
- No invitation tokens leaked in logs or list/detail endpoints.
- Handoff updated to Development Ready.
