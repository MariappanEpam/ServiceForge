# Secrets scan hook (PreToolUse)

## Purpose
Prevent secrets from being written to disk by agent tooling.

This rule exists because **blocking must happen before the write executes**. A post-write scan is too late.

## Requirement
Wire a hook that fires **before** any file write/edit and scans the proposed content for secrets.

- Hook type: `PreToolUse`
- Applies to tools: `Write`, `Edit` (and any other tool that writes files)
- Behavior:
  1) Extract the proposed file path and content from the tool call.
  2) Run a secrets scan on the proposed content.
  3) If secrets are detected, **block** the tool execution and return an error to the agent.
  4) Log diagnostics to stderr (never stdout in MCP stdio contexts).

## Reference implementation (repo-local)
- Scanner: `tools/secrets-scan/scan-content.js`
- Contract:
  - Exit code `0`: allow
  - Exit code `2`: block
  - stdout: single-line JSON summary
  - stderr: diagnostics

## Notes
- This is a heuristic scanner; it complements (not replaces) repo-level secret scanning.
- Do not store detected secrets in logs; redact values and report only the finding type.
