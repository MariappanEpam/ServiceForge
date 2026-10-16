# CodeGraph index (legacy root repo)

## Scope
- Repo: ServiceForge-Start (legacy root)
- Indexed folders:
  - `backend/`
  - `frontend/`
  - `test-automation/`

## Status
Status: partial

## Notes
- CodeGraph stores its working index under `.codegraph/` in the indexed directory.
- This folder (`pipeline/repo-understanding/legacy-root/codegraph/`) stores **exported snapshots** and logs used by the reverse-engineering workflow.

## How to regenerate
Run:
- `tools/codegraph/run-codegraph-index.ps1 -ScopeSlug legacy-root`

## Evidence
NEEDS-EVIDENCE: This file is a workflow artifact; evidence citations are not applicable.
