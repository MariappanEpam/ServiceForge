# Forward engineering handoff — ServiceForge (Migrated)

Status: SPECS + ADRS READY
Owner: architecture-design-agent
Updated: 2026-10-14

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

NEEDS-EVIDENCE:
- Remaining confirmed features still need migrated specs (job parts reservation, parts inventory management, technician onboarding/management) if they are in scope.

## Risks / unknowns
- Entra auth integration details (tenant/app registration, roles/scopes) need explicit decisions.
- Locked NFR/compliance baselines are placeholders; treat as risks until measured.
