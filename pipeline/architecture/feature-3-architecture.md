# Feature 3 — Technician Onboarding and Management

Architecture Design

Source spec: [pipeline/features/feature-3-technician-onboarding-management.md](../features/feature-3-technician-onboarding-management.md)
Related review: [pipeline/reviews/feature-3-review.md](../reviews/feature-3-review.md)

## 1) High-level component diagram (textual)

- Frontend (Angular)
  - `TechnicianDirectoryComponent` (list/search/filter by status)
  - `TechnicianDetailComponent` (view/edit profile)
  - `TechnicianInviteComponent` (invite/reinvite, show token once)
  - `TechnicianActivationComponent` (enter token and activate)
  - `TechniciansApiService` (Angular service for all HTTP calls)

- Backend (Spring Boot)
  - Controller layer
    - `TechnicianManagementController` — CRUD + lifecycle endpoints
    - `TechnicianOnboardingController` — activation endpoint (token)
  - Service layer
    - `TechnicianService` — profile CRUD and read models
    - `TechnicianOnboardingService` — invite token generation/validation, activation
    - `TechnicianLifecycleService` — transition validation (ACTIVE↔SUSPENDED, OFFBOARD)
    - `TechnicianEligibilityService` — booking guard checks used by job booking flow
  - Data layer (mocked)
    - `MockDataStore` extension: technicians collection + invitation token store

## 2) Domain model (in-memory)

- `Technician`
  - Existing fields remain stable (migration-safe).
  - Additive optional fields only for onboarding/management:
    - `status` (INVITED | ACTIVE | SUSPENDED | OFFBOARDED)
    - `email` (optional)
    - `phone` (optional)
    - `skills` / `tags` (optional list)
    - `homeBase` (optional)
    - `notes` (optional)

- `TechnicianInvitation`
  - `technicianId`
  - `tokenHash` (store hash, not raw token)
  - `createdAt`
  - `expiresAt`
  - `usedAt` (nullable)

Rationale: keep raw invitation token ephemeral; only return it once in invite response.

## 3) REST API surface (proposed)

Technician management
- POST /api/technicians
- GET /api/technicians
- GET /api/technicians/{id}
- PUT /api/technicians/{id}

Onboarding
- POST /api/technicians/{id}/invite
- POST /api/technicians/activate

Lifecycle
- POST /api/technicians/{id}/suspend
- POST /api/technicians/{id}/reactivate
- POST /api/technicians/{id}/offboard

Error handling
- 400 validation → `ApiError`
- 404 not found → `ApiError`
- 409 invalid transition / ineligible booking / token invalid → `ApiError`

## 4) Key flows

Invite
1. Admin calls invite endpoint.
2. Service generates random token, stores only hash in `MockDataStore` with expiry.
3. Response returns token once.

Activate
1. Technician submits token.
2. Service hashes token, finds matching invitation, validates not expired/used.
3. Marks invitation used and sets technician status ACTIVE.

Suspend/offboard
- `TechnicianLifecycleService` validates transitions centrally.

Booking guard
- Job booking service calls `TechnicianEligibilityService.assertEligible(technicianId)`.
- If status is SUSPENDED/OFFBOARDED → throw domain exception mapped to 409.

## 5) Non-functional requirements (NFRs)

- Security (demo): never log tokens; store token hashes only.
- Migration safety: additive-only changes to `Technician`; seed data updated for all technicians.
- Testability: services use constructor injection; `MockDataStore` can be reset/seeded deterministically.

## 6) Risks and mitigations

- Risk: breaking existing technician UI bindings when adding fields.
  - Mitigation: keep existing fields unchanged; new fields optional; ensure list DTO remains compatible.

- Risk: token leakage via logs or list endpoints.
  - Mitigation: never include token in list/detail; redact logs; return token only on invite.

## Handoff update (for Design Review Agent)

- Status: Architecture Complete
- Owner: Design Review Agent
- Artifacts:
  - [pipeline/features/feature-3-technician-onboarding-management.md](../features/feature-3-technician-onboarding-management.md)
  - [pipeline/architecture/feature-3-architecture.md](feature-3-architecture.md)
