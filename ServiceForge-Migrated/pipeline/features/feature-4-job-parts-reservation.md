# Feature 4 — Job parts reservation

Status: Draft (forward-engineered from current-state evidence)

## Problem and outcome
Allow an authorized user to reserve an orderable part for an existing job, check available inventory, review reservations, and cancel a reservation with inventory restored. Identity roles and exact user permissions remain NEEDS-EVIDENCE.

## Scope
**In scope:** list reservations for a job; create reservation; cancel reservation; inventory sufficiency and restoration; distinguish validation, not-found, and conflict errors.

**Out of scope:** supplier purchasing, restocking workflows, partial fulfillment, reservation expiry, and concurrent reservation guarantees beyond the current service/repository behavior (NEEDS-EVIDENCE).

## Target-stack flow
React page calls a typed API client; Java 21 / Spring Boot service validates the request and uses JDBC repositories/gateway; local SQLite schema remains migration-managed. Auth uses the approved Entra decision; exact role/claim authorization is NEEDS-EVIDENCE.

## API contract (target)
Base path: `/api`.
- `GET /api/jobs/{jobId}/parts-reservations` → `200 Reservation[]`; `404 ApiError` if job does not exist.
- `POST /api/jobs/{jobId}/parts-reservations` with `{ sku, quantity }` → `201 Reservation`; `400` for quantity <= 0, `404` for unknown job or non-reservable part, `409` if inventory is insufficient.
- `DELETE /api/jobs/{jobId}/parts-reservations/{reservationId}` → `204`; `404` if job/reservation is missing; `409` if already cancelled.
Exact status/body details should preserve current API contracts; response shape needs verification against controller implementation before coding.

## Data model / persistence
Use existing local SQLite `job_part_reservations` fields: `id`, `job_id`, `reserved_by_technician_id`, `sku`, `quantity_reserved`, `created_at`, nullable `cancelled_at`; relationships reference jobs, technicians, and parts. Inventory quantity remains on `parts.quantity_on_hand`. No schema change is presumed; any missing constraint/transaction behavior is NEEDS-EVIDENCE.

## Acceptance criteria
- AC-1: For an existing job, user can retrieve its reservations. Evidence: feature catalog confirms list endpoint.
- AC-2: A reservation with quantity > 0 for an active, orderable part and sufficient inventory is created. Evidence: BR-6, BR-8, BR-9.
- AC-3: Quantity <= 0 is rejected as a validation error. Evidence: BR-6.
- AC-4: Unknown job or missing/inactive/non-orderable part is rejected as not found. Evidence: BR-7, BR-8.
- AC-5: Insufficient inventory rejects the reservation as a conflict and does not allow inventory below zero. Evidence: BR-9, BR-12.
- AC-6: Cancelling an active reservation marks it cancelled and restores its reserved quantity to inventory. Evidence: BR-11.
- AC-7: Cancelling an already cancelled reservation returns conflict and does not restore inventory a second time. Evidence: BR-10.
- AC-8: Requests without an authenticated/authorized identity follow the approved Entra policy. Exact role access matrix: NEEDS-EVIDENCE.

## Definition of Done
- React reservation UI with list/create/cancel, pending/success/error states.
- Java service/controller and repository preserve the evidenced rules and use the established API error contract.
- SQLite changes only if evidence-based schema gaps require a versioned migration; no external DB.
- Unit/API tests cover AC-1–AC-8, including idempotent cancellation and insufficient inventory.
- Final endpoint status/body and transaction/concurrency semantics verified against current code before implementation.

## Traceability
| AC | Reverse-engineering evidence |
|---|---|
| AC-1 | [feature catalog](../../../pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md#L37-L45) |
| AC-2, AC-3 | [business rules BR-6](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L35-L39) and [BR-8–BR-9](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L45-L54) |
| AC-4 | [business rules BR-7–BR-8](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L40-L49) |
| AC-5 | [business rules BR-9 and BR-12](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L50-L68) |
| AC-6–AC-7 | [business rules BR-10–BR-11](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L55-L64) |
| AC-8 | NEEDS-EVIDENCE: role/claim authorization matrix is not established by current-state evidence. |
| Existing reservation storage | [persistence entities](../../../pipeline/repo-understanding/serviceforge-system-overview/persistence-entities.md#L21-L31) |
