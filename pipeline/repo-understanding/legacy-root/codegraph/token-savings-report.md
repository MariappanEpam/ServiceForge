# CodeGraph-assisted context savings estimate: legacy-root

Generated: 2026-10-16T14:11:44.557Z

> Proxy estimate only; not actual token usage, billed cost, or causal proof that CodeGraph produced the reduction.

## Estimate
- Broad source/config baseline: 119 files; 1,59,220 estimated tokens (chars/4).
- Unique source/config files cited in RE artifacts: 7; 2,912 estimated tokens (chars/4).
- Estimated potential context avoided: 1,56,308 tokens (98.17%).
- USD estimate: not calculated (input-token price not configured).

## CodeGraph index coverage
- backend: 0 indexed file(s).
- frontend: 15 indexed file(s).
- test-automation: 84 indexed file(s).

## Limitations
- This is an estimate of broad source-context avoided, not actual prompt tokens, provider billing, or a causal A/B comparison.
- Cited files are a lower-bound proxy for evidence inspected; files read but not cited are not counted.
- Some cited content may not have been sent to the model, and this estimate does not subtract the CodeGraph snapshot or interaction overhead.
- The backend CodeGraph index may report zero supported files; source citations remain the evidence authority.
- Dollar values are omitted unless CODEGRAPH_INPUT_USD_PER_MILLION or --usd-per-million is explicitly provided.

## Files cited (measured proxy)
- backend/pom.xml: 647 estimated tokens
- backend/src/main/java/com/serviceforge/ServiceForgeApplication.java: 84 estimated tokens
- backend/src/main/java/com/serviceforge/controller/JobController.java: 364 estimated tokens
- frontend/src/app/app.routes.ts: 204 estimated tokens
- frontend/src/main.ts: 64 estimated tokens
- test-automation/src/ServiceForge.Tests.Api/Feature2PartsReservationUiTests.cs: 685 estimated tokens
- test-automation/src/ServiceForge.Tests.Api/UiTestBase.cs: 865 estimated tokens
