# Design Review — Feature 4: Parts reservation for a job by the technician

- **Reviewed design:** [pipeline/architecture/feature-4-architecture.md](../architecture/feature-4-architecture.md)
- **Against spec:** [pipeline/features/feature-4-parts-reservation-by-technician.md](../features/feature-4-parts-reservation-by-technician.md)
- **Review date:** 2026-10-05

## 1) Executive summary

The design is aligned with repo constraints (mock/in-memory, Spring Boot + Angular) and keeps scope tight to job-tied reservations with strict inventory enforcement.

**Approval status:** **APPROVED**

## 2) Requirement coverage

✅ Covered:
- Create/list/cancel reservations tied to a job
- Inventory validation (no negative inventory)
- Concurrency safety via per-SKU locking
- Clear 400/404/409 error semantics
- UI via Angular service + job panel

⚠️ Clarifications (non-blocking):
- “Current technician” identity source: MVP derives from job assignment; later can use auth context.

## 3) Risks & findings

### Medium — Existing pipeline rule conflicts (partial reservation)
- Existing rule file [pipeline/rules/parts-count-inventory.md](../rules/parts-count-inventory.md) describes partial reservation + replenishment.
- Spec for Feature 4 explicitly excludes partial reservation and replenishment.
- Recommendation: treat the rule file as aspirational for a later feature; do not implement partial reservation in Feature 4.

### Low — Hot SKU contention
- Per-SKU locking is fine for demo scale.

## 4) Recommendations

1. Ensure cancel endpoint verifies reservation belongs to the jobId (avoid cross-job cancellation).
2. Add a concurrency unit test to prove no negative inventory.
3. Keep DTO validation strict (quantity bounds, sku pattern).

## 5) Decision

APPROVED
