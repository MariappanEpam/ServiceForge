#!/usr/bin/env node
/* Claude Code PreToolUse hook: inspect proposed file content before the tool runs. */

const { scanContent } = require('./scan-content.js');

const WRITE_TOOLS = new Set(['Write', 'Edit', 'MultiEdit', 'NotebookEdit']);

function deny(reason) {
  process.stdout.write(`${JSON.stringify({
    hookSpecificOutput: {
      hookEventName: 'PreToolUse',
      permissionDecision: 'deny',
      permissionDecisionReason: reason,
    },
  })}\n`);
}

function getProposedContent(toolName, input) {
  if (toolName === 'Write' && typeof input.content === 'string') return input.content;
  if (toolName === 'Edit' && typeof input.new_string === 'string') return input.new_string;
  if (toolName === 'MultiEdit' && Array.isArray(input.edits) && input.edits.length > 0) {
    if (input.edits.some(edit => !edit || typeof edit.new_string !== 'string')) return null;
    return input.edits.map(edit => edit.new_string).join('\n');
  }
  if (toolName === 'NotebookEdit' && typeof input.new_source === 'string') return input.new_source;
  return null;
}

let rawInput = '';
process.stdin.setEncoding('utf8');
process.stdin.on('data', chunk => { rawInput += chunk; });
process.stdin.on('end', () => {
  let event;
  try {
    event = JSON.parse(rawInput);
  } catch {
    process.stderr.write('[secrets-scan] BLOCKED: invalid PreToolUse input; unable to inspect proposed write.\n');
    deny('Secrets scan could not inspect the proposed write because hook input was invalid.');
    return;
  }

  const toolName = event && event.tool_name;
  if (!WRITE_TOOLS.has(toolName)) return;

  const input = event.tool_input || {};
  const targetPath = input.file_path || input.notebook_path || input.path;
  const content = getProposedContent(toolName, input);
  if (typeof targetPath !== 'string' || typeof content !== 'string') {
    process.stderr.write(`[secrets-scan] BLOCKED: unable to inspect proposed ${toolName} payload.\n`);
    deny(`Secrets scan could not inspect the proposed ${toolName} payload.`);
    return;
  }

  const result = scanContent(targetPath, content);
  if (!result.ok) {
    const types = result.findings.map(finding => finding.id).join(', ');
    process.stderr.write(`[secrets-scan] BLOCKED ${toolName} for ${targetPath}: potential secret detected (${types}).\n`);
    deny(`Potential secret detected in proposed content (${types}); remove it or use an approved secret store.`);
  }
});