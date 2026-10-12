# Feature 3 — Technician Onboarding and Management

| Field | Value |
|---|---|
| **Status** | Candidate-built |
| **One-line intent** | Enable admins/dispatchers to onboard technicians, manage their profiles, and control technician lifecycle (invited/active/suspended/offboarded) using mock/in-memory data |
| **Depends on** | Feature 1 technician/job foundations (technician listing and job booking constraints) |
| **In scope** | - Admin/dispatcher can create a technician profile (name, contact info, skills/tags, home base, notes).
- Admin/dispatcher can list technicians and view technician detail.
- Admin/dispatcher can update technician profile fields.
- Invitation-based onboarding: system generates an invitation token/link for a technician; technician can activate using the token.
- Technician lifecycle management: suspend, reactivate, offboard.
- Booking guard: suspended/offboarded technicians cannot be assigned new jobs (existing jobs remain visible).
- Seed mock data with a mix of lifecycle states for demo/testing.
- Frontend screens for technician list, technician detail/edit, and invite/activation.
- REST API endpoints (mock-backed) for all above operations.
| **Out of scope** | - Real authentication/SSO, password management, MFA.
- Persistent database or external identity provider.
- Complex RBAC beyond a simple demo role header.
- Background email/SMS sending (invitation link is returned by API for demo).
- HR workflows, payroll, time tracking.
| **Definition of Done** | - Backend exposes endpoints to create/list/get/update technicians and to manage onboarding/lifecycle:
  - POST /api/technicians
  - GET /api/technicians
  - GET /api/technicians/{id}
  - PUT /api/technicians/{id}
  - POST /api/technicians/{id}/invite
  - POST /api/technicians/activate (token)
  - POST /api/technicians/{id}/suspend
  - POST /api/technicians/{id}/reactivate
  - POST /api/technicians/{id}/offboard
- Invitation tokens are never returned in list/detail responses; only returned on invite/create flows.
- Validation errors return 400 with ApiError; not-found returns 404 with ApiError; invalid lifecycle transitions return 409 with ApiError.
- MockDataStore is seeded with at least 10 technicians across states (INVITED/ACTIVE/SUSPENDED/OFFBOARDED).
- Frontend uses Angular services for HTTP; components contain no direct HTTP calls.
- Unit tests cover lifecycle transitions and booking guard behavior.

## User stories

- As an admin, I want to create and invite a technician so they can activate their profile and start receiving jobs.
- As a dispatcher, I want to view technician status and contact details so I can assign work appropriately.
- As an admin, I want to suspend or offboard a technician so they cannot be assigned new jobs.
- As a technician, I want to activate my account using an invitation token so I can access the system.

## Acceptance criteria

1. Create technician
   - Given valid technician profile data, when POST /api/technicians is called, then a technician is created in INVITED (or DRAFT) state and returned with an id.

2. Invite technician
   - Given an existing technician in INVITED state, when POST /api/technicians/{id}/invite is called, then an invitation token is generated and returned once.

3. Activate technician
   - Given a valid invitation token, when POST /api/technicians/activate is called, then the technician becomes ACTIVE and the token is invalidated.

4. Suspend/reactivate/offboard
   - Given an ACTIVE technician, when suspend is called, then status becomes SUSPENDED.
   - Given a SUSPENDED technician, when reactivate is called, then status becomes ACTIVE.
   - Given an ACTIVE or SUSPENDED technician, when offboard is called, then status becomes OFFBOARDED.
   - Invalid transitions return 409.

5. Booking guard
   - Given a SUSPENDED or OFFBOARDED technician, when attempting to assign/book a new job for them, then the API rejects with 409 and an ApiError message indicating technician is not eligible.

## Edge cases to consider

- Re-inviting an already ACTIVE technician (should be rejected or return 409).
- Token reuse (must be rejected).
- Updating profile fields while INVITED vs ACTIVE (allowed, but activation should not overwrite admin-entered fields).
- Offboarding with future scheduled jobs (out of scope to auto-reassign; but must block new assignments).

## Assumptions & notes

- Demo-only authorization: a simple role header (e.g., X-Role) may be used to gate admin/dispatcher actions.
- No email delivery: invitation link/token is returned in API response for demo.

## Related pipeline artifacts

- Architecture: [pipeline/architecture/feature-3-architecture.md](../architecture/feature-3-architecture.md)
- Design review: [pipeline/reviews/feature-3-review.md](../reviews/feature-3-review.md)
- Implementation plan: [pipeline/implementation-plan/feature-3-implementation-plan.md](../implementation-plan/feature-3-implementation-plan.md)
