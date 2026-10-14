# Scope decision — ServiceForge (system overview)

Scope: Whole repository (ServiceForge-Start)
Decision: FULL (tentative)
Updated: 2026-10-14

## The Scope Decision Gates Everything
This reverse-engineering run targets a whole-repo understanding (backend + frontend + tools + pipeline artifacts).

## Evidence used
- Repository structure indicates multiple major subsystems (backend, frontend, tools, pipeline).
  - Evidence: workspace tree (see repo root)

## Constraints
- Evidence-first: any claim without file:line citations must be marked NEEDS-EVIDENCE.
- Guardrail: write only under pipeline/repo-understanding/serviceforge-system-overview/.

## Open questions
- Confirm whether the intended scope includes the duplicated /main/ subtree.
  - Status: NEEDS-EVIDENCE

## Sign-off
- Prepared by: reverse-engineering orchestrator
- Status: pending human confirmation
