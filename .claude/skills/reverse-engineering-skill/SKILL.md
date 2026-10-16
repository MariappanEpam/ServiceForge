# reverse-engineering-skill

## Purpose
This folder contains the reusable, generic reverse-engineering workflow guidance for this repo.

To enable parallel agent execution, each step is split into its own skill file:
- `common.md` — shared guardrails, evidence rules, and output conventions
- `step-1-scan.md`
- `step-2-analyze.md`
- `step-3-specify.md`
- `step-4-document.md`
- `step-5-review.md`

Agents should reference the specific step file plus `common.md`.
