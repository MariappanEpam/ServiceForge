# Forward engineering handoff — ServiceForge (target stack)

Status: NOT STARTED
Owner: adr-adapter-agent
Updated: 2026-10-14

## Target stack
- Backend: Java 21
- Frontend: React
- Auth: Azure Entra ID
- Persistence: SQLite local-only

## Inputs
- pipeline/repo-understanding/serviceforge-system-overview/*

## Outputs (to be produced)
- ADRs under pipeline/decisions/
- Feature specs under pipeline/features/
- Implementation plan under pipeline/implementation-plan/

## Risks / unknowns
- Entra auth integration details (tenant/app registration, roles/scopes) need explicit decisions.
- Locked NFR/compliance baselines are currently placeholders; treat as risks until measured.
