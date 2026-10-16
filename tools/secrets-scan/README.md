# Secrets scan (pre-write)

Purpose: block accidental secret commits by scanning **proposed file write content** before it lands on disk.

This is designed to be invoked from a **PreToolUse** hook (tooling-specific) that runs before any write/edit tool executes.

## Scanner

- Script: `tools/secrets-scan/scan-content.js`
- Input: a file path + a temp file containing the proposed content
- Output:
  - stdout: single-line JSON `{ ok, path, findings }`
  - stderr: diagnostics
- Exit codes:
  - `0` clean
  - `2` secrets detected (block)
  - `3` invalid usage

## Hook wiring (concept)

Your agent runtime must:
1) Intercept write/edit tool calls in `PreToolUse`
2) Serialize the proposed content to a temp file
3) Run the scanner
4) If exit code is `2`, **block** the write

Example (pseudo):
- on PreToolUse for `Write`/`Edit`:
  - write payload to temp
  - `node tools/secrets-scan/scan-content.js --path <target> --content-file <temp>`
  - if exit code != 0: abort tool execution

## Notes
- This is heuristic scanning, not a full replacement for gitleaks/trufflehog.
- Keep stdout clean in MCP contexts; this tool writes diagnostics to stderr.
