# Compliance map — ServiceForge (system overview)

Scope: Whole repository (ServiceForge-Start)
Status: locked
Updated: 2026-10-14

## Locked semantics
- This document is a locked baseline.
- Later phases consume it and MUST NOT re-derive it.
- Missing regulated data flows are risks to flag (NEEDS-EVIDENCE), not targets to invent.
- Only a human-approved revision may change this file; record changes in the changelog.

## Regulated data flow map (capture → storage → transmission)
NEEDS-EVIDENCE
- Identify PII fields (if any) in DTOs/forms
- Identify storage locations (SQLite tables/files)
- Identify transmission paths (API endpoints, logs, exports)

## Changelog
- 2026-10-14: Created locked baseline placeholder (no regulated flows mapped yet).

## Provenance
- NEEDS-EVIDENCE: requires code-level field tracing with file:line citations.
