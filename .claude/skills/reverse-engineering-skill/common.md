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
- After the reverse-engineering artifacts are complete, run `node tools/codegraph/estimate-token-savings.mjs --scope <scope-slug>` and retain its Markdown/JSON reports in that same codegraph folder.
- Treat the result as a chars/4 proxy for potential context avoided, not actual model tokens, billed cost, or proven causal savings. It compares all source/config text in backend/, frontend/, and test-automation/ with unique source/config files cited by the generated RE Markdown; it cannot account for uncited reads or CodeGraph interaction overhead.
- Dollar estimates are produced only when `CODEGRAPH_INPUT_USD_PER_MILLION` is explicitly configured. Report coverage gaps, particularly a CodeGraph index with zero supported files.
