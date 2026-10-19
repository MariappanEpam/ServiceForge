# Feature 6 — Technician onboarding and management

Status: Draft (forward-engineered from current-state evidence)

## Problem and outcome
Allow authorized operations staff to inspect, onboard, update, deactivate, and reactivate technicians through the ServiceForge UI and API. The target retains the current technician identity/profile semantics; permissions and precise validation rules remain NEEDS-EVIDENCE where not captured.

## Scope
**In scope:** list/search/filter technicians where supported by the current contract; get by ID; create; update; deactivate; reactivate; maintain evidenced profile fields.

**Out of scope:** identity provisioning in Entra, payroll, background checks, certification workflows, dispatch assignment optimization, and hard-delete behavior (NEEDS-EVIDENCE / not in confirmed catalog).

## Target-stack flow
React technician management screen uses a client service for API requests. Java 21 / Spring Boot controller/service/repository layers persist to local-file SQLite. Entra authentication follows the approved ADR; exact role and self-service policies require a separate decision/evidence.

## API contract (target)
Base path: `/api/technicians`.
- `GET /api/technicians` → `200 Technician[]` (optional supported search/filter parameters to be verified).
- `GET /api/technicians/{id}` → `200 Technician`; `404 ApiError` if absent.
- `POST /api/technicians` with profile fields → `201 Technician`; `400 ApiError` for invalid request.
- `PUT /api/technicians/{id}` → `200 Technician`; `400` invalid request; `404` missing technician.
- `POST /api/technicians/{id}/deactivate` and `/reactivate` → success response; exact status and body require verification against the existing controller.
The contracts above preserve the confirmed operation set; exact request/response fields and status codes need source verification before implementation.

## Data model / persistence
Existing technician profile includes `id`, `name`, `active`, nullable `age`, `area_served`, `vehicle_type`, `vehicle_make`, `vehicle_model`, `vehicle_license_plate`, and `skills_csv`. No new table is presumed. Data-model migration rules require consumer review if fields change.

## Acceptance criteria
- AC-1: User can list technicians and retrieve a technician by ID. Evidence: feature catalog confirms list/get operations.
- AC-2: User can onboard a technician with valid profile information. Evidence: feature catalog confirms create; required/optional fields need verification.
- AC-3: User can update a technician profile. Evidence: feature catalog confirms update; field mutability needs verification.
- AC-4: User can deactivate and reactivate a technician without deleting the record. Evidence: BR-13 confirms activate/deactivate support.
- AC-5: An unknown technician ID returns not-found for get/update/state operations. Current error semantics need confirmation per operation.
- AC-6: Unauthorized requests are rejected under Entra policy. Exact management roles and self-service access: NEEDS-EVIDENCE.

## Definition of Done
- React management UI supports list/search, create/edit, and deactivate/reactivate with loading, validation, and error states.
- Java endpoints preserve the documented legacy behavior and common error mapping.
- SQLite changes, if any, are delivered through a versioned migration and retain local-only persistence.
- Tests cover create/read/update, state transitions, invalid/missing IDs, and authorization after access policy is confirmed.
- Verify exact profile validation and query contracts before implementation; no fields or business rules are invented.

## Traceability
| AC | Reverse-engineering evidence |
|---|---|
| AC-1–AC-3 | [feature catalog](../../../pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md#L54-L60) |
| AC-4 | [business rule BR-13](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L69-L75) |
| AC-5 | [business rule BR-14](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L76-L80); exact operation behavior NEEDS-EVIDENCE. |
| AC-6 | NEEDS-EVIDENCE: role/claim authorization matrix not established. |
| Profile fields | [persistence entities](../../../pipeline/repo-understanding/serviceforge-system-overview/persistence-entities.md#L34-L43) |
