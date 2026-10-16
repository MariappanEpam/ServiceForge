---
name: target-architecture
description: Use when adapting reverse-engineered ServiceForge requirements into forward-engineering specifications, ADRs, and architecture handoffs for the approved ServiceForge-Migrated target architecture.
---

# Target Architecture — ServiceForge-Migrated

## Purpose
This skill contains project-specific target-stack decisions and artifact conventions for the generic ADR adapter agent. Keep the adapter workflow generic; keep this target context here.

## Approved target architecture
- Backend: Java 21 with Spring Boot 3.
- Frontend: React with TypeScript and Vite.
- Authentication: Microsoft Entra ID.
- Persistence: local-file SQLite only, managed through versioned migrations; no external database services.

These decisions are established by the existing target architecture and project conventions. Do not silently substitute technologies or expand the persistence boundary. If a requirement conflicts with an approved decision, identify the conflict and ask for a decision rather than changing the target.

## Reverse-engineering inputs
Use the current, committed evidence under `pipeline/repo-understanding/serviceforge-system-overview/` as the source of current-system behavior:
- `feature-catalog.md`
- `business-rules.md`
- `persistence-entities.md`
- `aggregate-boundaries.md`
- `bounded-contexts.md`
- `domain-glossary.md`
- `c4-as-is/`
- `nfr-baseline.md` (locked)
- `compliance-map.md` (locked)

Also consult `README.md`, `AGENTS.md`, and existing project decisions as needed. Verify each cited fact against the evidence; carry unknowns forward as `NEEDS-EVIDENCE` instead of inventing details.

## Target decisions and ADRs
Maintain the following decisions in `ServiceForge-Migrated/pipeline/decisions/`:
- `adr-0001-target-tech-stack.md`
- `adr-0002-auth-azure-entra.md`
- `adr-0003-persistence-sqlite-local-only.md`

Each ADR records Context, Decision, Alternatives considered, Consequences, and links to the source reverse-engineering evidence. Preserve approved decisions unless an explicit user-approved decision supersedes them.

## Feature specifications
Create one specification per confirmed feature in `ServiceForge-Migrated/pipeline/features/`; never merge unrelated features. The current confirmed feature set is:
1. Technician availability / scheduling view.
2. Job booking.
3. Parts catalog.
4. Job parts reservation.
5. Parts inventory management.
6. Technician onboarding / management.

Each spec includes:
- Problem statement and business outcome.
- In-scope and out-of-scope behavior.
- React UI flow and target-stack API contracts.
- SQLite data model and migration implications.
- Business rules traceable to reverse-engineered evidence.
- Positive, negative, and boundary acceptance criteria.
- Definition of Done.
- Traceability table mapping every acceptance criterion to evidence or marking it `NEEDS-EVIDENCE`.

Do not add features beyond the confirmed catalog without explicit approval.

## Handoff
Update `ServiceForge-Migrated/pipeline/handoffs/feature-forward-engineering-handoff.md` with:
- Links to generated specs and ADRs.
- Target architecture summary.
- Risks, assumptions, and unresolved evidence gaps.
- Readiness and ownership transfer to the architecture-design agent.

## Output and safety constraints
- Write forward-engineering artifacts only under `ServiceForge-Migrated/pipeline/features/`, `ServiceForge-Migrated/pipeline/decisions/`, and `ServiceForge-Migrated/pipeline/handoffs/`.
- Do not modify application source code.
- Do not overwrite reverse-engineering evidence.
- Do not treat the target architecture as evidence for current-system behavior; label forward-looking decisions distinctly.
