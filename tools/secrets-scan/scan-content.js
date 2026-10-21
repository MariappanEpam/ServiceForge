#!/usr/bin/env node
/*
  scan-content.js

  Purpose:
  - Scan a proposed file write payload (string) for likely secrets.
  - Intended to be called from a PreToolUse hook BEFORE a write/edit tool executes.

  Usage:
    node tools/secrets-scan/scan-content.js --path "relative/or/absolute" --content-file "C:\\temp\\payload.txt"

  Exit codes:
    0 = clean
    2 = secrets detected (block)
    3 = invalid usage

  Output:
    - Writes diagnostics to stderr only.
    - Writes a single-line JSON summary to stdout only when clean/block decision is needed by a hook runner.
*/

const fs = require('fs');

function readArg(name) {
  const idx = process.argv.indexOf(name);
  if (idx === -1) return null;
  return process.argv[idx + 1] ?? null;
}

function failUsage(msg) {
  process.stderr.write(`[secrets-scan] ${msg}\n`);
  process.stderr.write('[secrets-scan] Usage: node tools/secrets-scan/scan-content.js --path <path> --content-file <file>\n');
  process.exit(3);
}

// Heuristic patterns (keep conservative; avoid false positives where possible)
const patterns = [
  { id: 'aws_access_key_id', re: /\bAKIA[0-9A-Z]{16}\b/g },
  { id: 'aws_secret_access_key', re: /\baws_secret_access_key\b\s*[:=]\s*['\"]?[A-Za-z0-9\/+=]{40}['\"]?/gi },
  { id: 'github_pat', re: /\bghp_[A-Za-z0-9]{36,}\b/g },
  { id: 'github_fine_grained_pat', re: /\bgithub_pat_[A-Za-z0-9_]{20,}\b/g },
  { id: 'slack_token', re: /\bxox[baprs]-[A-Za-z0-9-]{10,}\b/g },
  { id: 'stripe_secret', re: /\bsk_(live|test)_[A-Za-z0-9]{16,}\b/g },
  { id: 'private_key_block', re: /-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----/g },
  // Generic assignment (guarded by key names) - require some entropy to reduce false positives
  // Matches quoted values that are either long-ish OR contain mixed classes (letters+digits) or URL-safe base64-ish.
  { id: 'generic_secret_assignment', re: /\b(?:api[_-]?key|secret|token|password|passwd|pwd)\b\s*[:=]\s*['\"][^'\"]*(?:[A-Za-z0-9\/+=]{24,}|(?=[^'\"]*[A-Za-z])(?=[^'\"]*\d)[^'\"]{16,})[^'\"]*['\"]/gi },
  // Connection strings (common)
  { id: 'azure_storage_conn', re: /\bDefaultEndpointsProtocol=https;AccountName=[^;]+;AccountKey=[^;]+;EndpointSuffix=core\.windows\.net\b/gi },
];

function scanContent(targetPath, content) {
  const findings = [];
  for (const p of patterns) {
    const matches = content.match(p.re);
    if (matches && matches.length) {
      findings.push({ id: p.id, count: matches.length });
    }
  }

  const blocked = findings.length > 0;

  return { ok: !blocked, path: targetPath, findings };
}

module.exports = { scanContent };

if (require.main === module) {
  const targetPath = readArg('--path');
  const contentFile = readArg('--content-file');

  if (!targetPath || !contentFile) {
    failUsage('Missing required args');
  }

  let content;
  try {
    content = fs.readFileSync(contentFile, 'utf8');
  } catch (e) {
    failUsage(`Unable to read content file: ${String(e && e.message ? e.message : e)}`);
  }

  const result = scanContent(targetPath, content);
  process.stdout.write(`${JSON.stringify(result)}\n`);

  if (!result.ok) {
    process.stderr.write(`[secrets-scan] BLOCKED write to ${targetPath}: potential secrets detected: ${result.findings.map(f => f.id).join(', ')}\n`);
    process.exit(2);
  }

  process.stderr.write(`[secrets-scan] OK: ${targetPath}\n`);
  process.exit(0);
}
