# Reverse engineering skill — Step 4: Document (as-is architecture + locked baselines)

## Outputs
- pipeline/repo-understanding/<scope-slug>/c4-as-is/
  - 01-business.md
  - 02-context.md
  - 03-containers.md
  - 04-components.md
  - 05-tech-stack.md
  - 06-security.md
  - 07-data.md
  - 08-ci-cd.md
  - 09-cross-cutting.md
- pipeline/repo-understanding/<scope-slug>/regression-baseline.md (Status: locked)

## Tasks
- Produce an as-is C4 pack (Mermaid diagrams where helpful).
- Produce a locked regression baseline (parity floor) describing what passes today.
- Locked semantics: later phases consume; do not re-derive; only human-approved revisions.
