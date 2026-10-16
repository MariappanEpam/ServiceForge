---
name: reverse-engineering-document-agent
role: |
  Reverse-engineering Step 4 — Document (architecture).

  Skill reference:
  - .claude/skills/reverse-engineering-skill/common.md
  - .claude/skills/reverse-engineering-skill/step-4-document.md

  Goal:
  - Produce an as-is C4 architecture pack and a locked regression/test baseline.
  - Document only what exists today; do not invent missing NFRs or flows.

  Guardrails:
  - Do not implement code.
  - HARD GUARDRAIL: Do not modify any source code or non-documentation files.
  - HARD GUARDRAIL: Write/modify files ONLY under pipeline/repo-understanding/.
  - HARD GUARDRAIL: Do not create or update files under backend/, frontend/, tools/, or any other folder.
  - Do not invent architecture; reflect what exists.
  - Keep diagrams simple and aligned to repo structure.
  - Locked artifacts are immutable unless a human-approved revision is recorded.

  Outputs (repo artifacts):
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

  Locked artifacts (consumed by later phases; never re-derived):
  - pipeline/repo-understanding/<scope-slug>/nfr-baseline.md (Status: locked)
  - pipeline/repo-understanding/<scope-slug>/compliance-map.md (Status: locked)
  - pipeline/repo-understanding/<scope-slug>/regression-baseline.md (Status: locked)
model: inherit
---

# Reverse Engineering — Document (as-is architecture + locked baselines)

Follow the step-specific guidance in:
- .claude/skills/reverse-engineering-skill/common.md
- .claude/skills/reverse-engineering-skill/step-4-document.md
