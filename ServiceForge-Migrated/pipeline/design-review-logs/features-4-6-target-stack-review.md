# Design review — Features 4–6 target-stack architecture

Date: 2026-10-16  
Scope: Features 4–6 architecture design  
Status: APPROVE WITH COMMENTS — design direction accepted for planning; not implementation-ready until mandatory decisions below are resolved

## Review summary
The architecture preserves the approved Java 21 / Spring Boot, React, Entra ID, and local SQLite/Flyway decisions. It reuses the existing reservation, parts, and technician concepts and does not propose an external service or an unsupported schema change.

## Findings

### Blocking before affected implementation
1. **Authorization policy is unresolved.** Specify roles/scopes and least-privilege behavior for reading/writing reservations, stock, and technician profiles.
2. **Reservation consistency needs verification.** Demonstrate transaction/rollback behavior when inventory decrement and reservation persistence fail separately; establish the concurrency behavior under SQLite.
3. **Endpoint contracts require source-level confirmation.** Confirm exact status codes, payloads, route methods, and deletion/state-transition behavior before frontend implementation.

### Non-blocking for architecture approval; resolve during planning
4. Part/technician validation requirements, search/filter/pagination semantics, and behavior for deleting referenced entities are not fully represented by current-state evidence.
5. NFR and compliance baselines remain locked with unknown/placeholder targets. No production-readiness conclusion is supported.

## Checks
- Target technology choices align with approved ADRs: PASS.
- SQLite local-only boundary preserved: PASS.
- Feature scope limited to confirmed capabilities: PASS.
- Business rules trace to reverse-engineering artifacts: PASS, subject to endpoint verification.
- Security authorization sufficiently specified for implementation: FAIL / open decision.
- Performance/availability targets evidenced: NEEDS-EVIDENCE.

## Decision and next steps
Architecture direction is approved with comments for creation of a gated implementation plan only. Do not start the gated tasks until items 1–3 are resolved and recorded in the handoff. This approval does not authorize source changes or assert production readiness.

## Evidence
- [Feature catalog](../../../pipeline/repo-understanding/serviceforge-system-overview/feature-catalog.md#L37-L60)
- [Business rules](../../../pipeline/repo-understanding/serviceforge-system-overview/business-rules.md#L35-L80)
- [Persistence entities](../../../pipeline/repo-understanding/serviceforge-system-overview/persistence-entities.md#L21-L43)
- [Architecture design](../architecture-design/architecture-migrated-target-stack-features-4-6.md)
