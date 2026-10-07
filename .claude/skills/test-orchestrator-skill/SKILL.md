# Test Orchestrator Skill — ServiceForge

Use this skill whenever the user asks to **plan, automate, execute, and report tests** for a feature.

This repo’s test orchestration is split into 4 agents:
- `test-plan-agent`
- `test-automation-agent`
- `test-execution-agent`
- `test-reporting-agent`

This skill standardizes:
- Folder/file conventions
- C# Playwright automation approach (API + UI)
- Local execution steps
- ReportPortal reporting approach
- How each agent must reference the other artifacts

---

## 1) Repository conventions (MANDATORY)

### 1.1 Pipeline artifacts
Only these two live under `pipeline/` and must be feature-scoped:
- Test plan folder: `pipeline/test-plan/<feature-slug>/`
  - File: `pipeline/test-plan/<feature-slug>/test-plan.md`
- Test report folder: `pipeline/test-report/<feature-slug>/`
  - File: `pipeline/test-report/<feature-slug>/test-report.md`

Do **not** create `pipeline/test-automation/...` or `pipeline/test-execution/...` folders.

### 1.2 Automation + execution artifacts (NOT under pipeline)
Automation and execution live as sibling solutions to `backend/` and `frontend/`:
- `test-automation/` (C# solution)
- `test-execution/` (optional scripts/logs; keep minimal)

Workspace root layout (required):
- `backend/`
- `frontend/`
- `test-automation/`
- `test-execution/`

### 1.3 Feature slug
Use the existing feature slug from `pipeline/features/feature-N-<slug>.md`.
Example: `feature-4-parts-reservation-by-technician`

---

## 2) Test plan requirements (for `test-plan-agent`)

Create/update:
- `pipeline/test-plan/<feature-slug>/test-plan.md`

Must include:
- Scope (in/out)
- DoD traceability matrix
- API test cases (contract-level)
- UI test cases (user flows)
- Data strategy (SQLite seed/reset expectations)
- Execution readiness checklist

Each test case must include:
- ID (TP-<feature>-<area>-<nnn>)
- Preconditions
- Steps
- Expected results
- Test data
- Traceability (DoD item)

---

## 3) Test automation requirements (C# + Playwright) (for `test-automation-agent`)

### 3.1 Create solution structure
Create a .NET solution under `test-automation/`:
- `test-automation/ServiceForge.TestAutomation.sln`
- `test-automation/src/ServiceForge.Tests.Api/` (Playwright API tests)
- `test-automation/src/ServiceForge.Tests.Ui/` (Playwright UI tests)
- `test-automation/src/ServiceForge.Tests.Shared/` (shared fixtures/config)

### 3.2 Playwright usage
Use Microsoft Playwright for .NET:
- UI tests: `IPage` navigation + assertions
- API tests: `IAPIRequestContext` with base URL

Rules:
- No hard-coded ports; read from config/env.
- Use deterministic test data; do not depend on execution order.
- Prefer stable selectors (data-testid) for UI; if missing, document as a product improvement.

### 3.3 Configuration
Support these environment variables:
- `SERVICEFORGE_API_BASE_URL` (default `http://localhost:4200`)
- `SERVICEFORGE_UI_BASE_URL` (default `http://localhost:4200`)
- `SERVICEFORGE_HEADLESS` (default `true`)

### 3.4 ReportPortal integration
Integrate ReportPortal for .NET test runs:
- Use a supported ReportPortal client/adapter for the chosen test framework.
- Capture:
  - launch name (feature slug)
  - suite name (api/ui)
  - test case IDs
  - attachments (screenshots on failure, request/response snippets when safe)

If ReportPortal server is not available locally:
- Keep integration code/config in place
- Mark reporting as “configured, requires RP endpoint” in the report

### 3.5 Automation documentation
Create/update a short automation README:
- `test-automation/README.md`
Include:
- prerequisites
- how to run API tests
- how to run UI tests
- how to enable ReportPortal

---

## 4) Test execution requirements (for `test-execution-agent`)

### 4.1 Local execution
Execution evidence must be recorded in:
- `pipeline/test-report/<feature-slug>/test-report.md` (summary)

Additionally, store raw logs (optional) under:
- `test-execution/<feature-slug>/` (NOT under pipeline)

### 4.2 Evidence format
For each suite:
- command
- timestamp
- exit code
- summary (pass/fail counts)
- link to logs/artifacts

### 4.3 Host hygiene
Before running UI/E2E:
- stop conflicting hosts using `tools/stop-local-hosts.ps1` when relevant

---

## 5) Test reporting requirements (for `test-reporting-agent`)

Create/update:
- `pipeline/test-report/<feature-slug>/test-report.md`

Must include:
- Executive summary
- Results by suite (backend unit/integration, API Playwright, UI Playwright)
- DoD traceability (PASS/FAIL/NOT RUN)
- ReportPortal launch link (if available)
- Defects/risks + next steps

---

## 6) Cross-agent linking rules (MANDATORY)

Each of the 4 test agents must:
- Link to the other artifacts it depends on.
- Update the feature handoff with links to:
  - `pipeline/test-plan/<feature-slug>/test-plan.md`
  - `pipeline/test-report/<feature-slug>/test-report.md`
  - `test-automation/README.md`
  - `test-execution/<feature-slug>/` (if created)

---

## 7) Guardrails
- Do not invent scope beyond the committed feature spec/plan.
- Do not modify application code unless explicitly routed to developer.
- Keep tests deterministic; avoid seed-order coupling.
- Keep pipeline folder clean: only plan + report are feature-scoped folders.
