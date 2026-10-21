# Secrets scan (pre-write)

Purpose: block accidental secret commits by scanning **proposed file write content** before it lands on disk.

The project registers a real Claude Code **PreToolUse** command hook in `.claude/settings.json`. It runs before supported file mutation tools execute and denies the tool call if proposed content contains likely secrets or cannot be inspected.

## Scanner

- Hook: `tools/secrets-scan/pretooluse-hook.js`
- Scanner: `tools/secrets-scan/scan-content.js`
- Covered tools: `Write`, `Edit`, `MultiEdit`, and `NotebookEdit`
- Input: a file path + a temp file containing the proposed content
- Output:
  - stdout: single-line JSON `{ ok, path, findings }`
  - stderr: diagnostics
- Exit codes:
  - `0` clean
  - `2` secrets detected (block)
  - `3` invalid usage

## Verify the hook contract

Run `node --test tools/secrets-scan/test-pretooluse-hook.js` to exercise planted secret payloads, verify the Claude Code deny response, and confirm a denied write does not reach disk.

For a clean proposed payload, the hook exits successfully without a deny response. Invalid hook input or an unrecognized mutation payload is denied closed.

## Notes
- This is heuristic scanning, not a full replacement for gitleaks/trufflehog.
- Keep stdout clean in MCP contexts; this tool writes diagnostics to stderr.
