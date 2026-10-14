# Forward engineering handoff — ServiceForge (Migrated)

Status: NOT STARTED
Owner: adr-adapter-agent
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

## Risks / unknowns
- Entra auth integration details (tenant/app registration, roles/scopes) need explicit decisions.
- Locked NFR/compliance baselines are placeholders; treat as risks until measured.
