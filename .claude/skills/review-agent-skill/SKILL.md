# Review Agent Skill — ServiceForge

## Purpose
Provide a consistent, repeatable review checklist and output format for reviewing feature work across the ServiceForge pipeline.

This skill is used by the `reviewer-agent` to:
- Validate pipeline compliance (BA → Architecture → Implementation Plan → Dev → Tests → Report)
- Validate repo conventions (backend Spring Boot, frontend Angular, local-only SQLite rules)
- Validate test evidence exists (TRX + HTML report under `test-execution/`)
- Produce a concise, actionable review result

## Inputs the reviewer should gather
- Feature spec: `pipeline/features/feature-N-*.md`
- Architecture: `pipeline/architecture/feature-N-architecture.md` (if present)
- Implementation plan: `pipeline/implementation-plan/feature-N-implementation-plan.md`
- Handoff(s): `pipeline/handoffs/feature-N-handoff*.md` (if present)
- Test plan: `pipeline/test-plan/<feature-slug>/test-plan.md`
- Test execution evidence: `test-execution/<feature-slug>/` (TRX, HTML, screenshots)
- Test report: `pipeline/test-report/<feature-slug>/test-report.md`

## Review checklist
### 1) Pipeline compliance
- [ ] Exactly one feature spec exists for the feature.
- [ ] Implementation plan exists and matches the spec scope.
- [ ] Dev changes align to the plan (no invented scope).
- [ ] Test plan exists under `pipeline/test-plan/<feature-slug>/`.
- [ ] Test execution evidence exists under `test-execution/<feature-slug>/`.
- [ ] Test report exists under `pipeline/test-report/<feature-slug>/`.

### 2) Backend conventions (Spring Boot)
- [ ] Package-by-layer (`model`, `data`, `service`, `controller`, `dto`).
- [ ] Constructor injection only.
- [ ] REST endpoints return `ResponseEntity<T>`.
- [ ] Validation errors return `400` with `ApiError`.
- [ ] Not-found returns `404` with `ApiError`.

### 3) Frontend conventions (Angular)
- [ ] HTTP calls only in `services/` (not in components).
- [ ] Components are presentation + delegation.
- [ ] Uses proxy `/api` (no hardcoded localhost ports in services).

### 4) Persistence rules
- [ ] No external DB services.
- [ ] If SQLite used: migrations are versioned; seed behavior explicit.

### 5) Test quality
- [ ] Tests are deterministic (no sleeps, stable selectors).
- [ ] UI tests use `data-testid` selectors.
- [ ] Evidence captured on failure (screenshot/HTML) and stored.

## Required output format
The reviewer must output one of:
- `APPROVED` — all checks pass
- `BLOCKED` — missing required artifacts or violations

Then include:
- Summary (3–6 bullets)
- Findings (grouped by Backend/Frontend/Tests/Docs)
- Required fixes (numbered, minimal)

## Evidence requirements
For approval, reviewer must cite:
- At least one TRX file path
- At least one HTML report path under `test-execution/<feature-slug>/reports/`
