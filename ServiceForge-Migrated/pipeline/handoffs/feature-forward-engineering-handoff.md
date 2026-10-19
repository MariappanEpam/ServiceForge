# Forward engineering handoff — ServiceForge (Migrated)

Status: FEATURES 1–6 SPECCED; FEATURES 4–6 PLAN READY WITH GATE 0 BLOCKERS (NOT IMPLEMENTATION-READY)
Owner: decision owners for Gate 0; then developer-orchestrator-agent
Updated: 2026-10-16

## Working root
All forward-engineering artifacts MUST be written under:
- `ServiceForge-Migrated/`

## Target stack
- Backend: Java 21
- Frontend: React
- Auth: Azure Entra ID
- Persistence: SQLite local-only

## Inputs (read-only)
- `pipeline/repo-understanding/serviceforge-system-overview/*`

## Outputs (to be produced under ServiceForge-Migrated/)
- ADRs: `ServiceForge-Migrated/pipeline/decisions/*`
- Feature specs: `ServiceForge-Migrated/pipeline/features/*`
- Implementation plan: `ServiceForge-Migrated/pipeline/implementation-plan/*`

## Produced artifacts
### ADRs
- `ServiceForge-Migrated/pipeline/decisions/adr-0001-target-tech-stack.md`
- `ServiceForge-Migrated/pipeline/decisions/adr-0002-auth-azure-entra.md`
- `ServiceForge-Migrated/pipeline/decisions/adr-0003-persistence-sqlite-local-only.md`

### Feature specs
- `ServiceForge-Migrated/pipeline/features/feature-1-technician-availability-scheduling-view.md`
- `ServiceForge-Migrated/pipeline/features/feature-2-job-booking.md`
- `ServiceForge-Migrated/pipeline/features/feature-3-parts-catalog.md`
- `ServiceForge-Migrated/pipeline/features/feature-4-job-parts-reservation.md`
- `ServiceForge-Migrated/pipeline/features/feature-5-parts-inventory-management.md`
- `ServiceForge-Migrated/pipeline/features/feature-6-technician-onboarding-and-management.md`

### Forward orchestration continuation (Features 4–6)
- Architecture: [architecture-migrated-target-stack-features-4-6.md](../architecture-design/architecture-migrated-target-stack-features-4-6.md)
- Design review: [features-4-6-target-stack-review.md](../design-review-logs/features-4-6-target-stack-review.md)
- Implementation plan: [implementation-plan-migrated-target-stack-features-4-6.md](../implementation-plan/implementation-plan-migrated-target-stack-features-4-6.md)
- Status: planning artifacts produced; implementation is gated on Gate 0 authorization, endpoint-contract, and transaction decisions. No source changes are authorized by this handoff.

## Risks / unknowns
- Entra auth integration details (tenant/app registration, roles/scopes) need explicit decisions.
- Locked NFR/compliance baselines are placeholders; treat as risks until measured.
- Feature 4–6 details not evidenced by source analysis (exact response shapes, field validation, referenced-part deletion, transaction/concurrency behavior, role matrix) are marked NEEDS-EVIDENCE and must be resolved before the affected implementation tasks.
