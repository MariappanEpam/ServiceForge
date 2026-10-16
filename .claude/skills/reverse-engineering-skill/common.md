# Reverse engineering skill — Common

## Global guardrails (apply to ALL reverse-engineering steps)
- Do not implement code.
- HARD GUARDRAIL: Do not modify any source code or non-documentation files.
- HARD GUARDRAIL: Write/modify files ONLY under pipeline/repo-understanding/.
- HARD GUARDRAIL: Do not create or update files under backend/, frontend/, tools/, pipeline/decisions/, pipeline/memory/, or any other folder.
- Do not invent behavior; derive from code/config/tests only.
- Prefer links/paths over copying large code blocks.

## Evidence rules
- Evidence is mandatory.
- Every extracted fact must cite repo file:line.
- If evidence is missing, mark the item as NEEDS-EVIDENCE / unknown (do not guess).

## Output conventions
- Use a scope slug folder: pipeline/repo-understanding/<scope-slug>/
- Each artifact must include:
  - Scope
  - Status: confirmed | partial | unknown (or Status: locked where required)
  - Evidence section with file:line citations

## CodeGraph (optional navigation aid)
- If CodeGraph is available, generate/update its index for the target scope **before** Step 1.
- Store exported snapshots/logs ONLY under: pipeline/repo-understanding/<scope-slug>/codegraph/
- CodeGraph output is NOT evidence; all facts still require repo file:line citations.
