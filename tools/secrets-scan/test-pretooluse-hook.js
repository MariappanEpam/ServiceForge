#!/usr/bin/env node

const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const { spawnSync } = require('node:child_process');
const path = require('node:path');
const test = require('node:test');

const hookPath = path.join(__dirname, 'pretooluse-hook.js');
const settingsPath = path.join(__dirname, '..', '..', '.claude', 'settings.json');

function runHook(event) {
  return spawnSync(process.execPath, [hookPath], {
    input: JSON.stringify(event),
    encoding: 'utf8',
  });
}

test('planted secret in proposed Write is denied before the file reaches disk', () => {
  const testDirectory = fs.mkdtempSync(path.join(os.tmpdir(), 'serviceforge-secret-hook-'));
  const targetPath = path.join(testDirectory, 'unsafe.txt');
  const plantedSecret = 'ghp_' + 'A'.repeat(40);
  const result = runHook({
    hook_event_name: 'PreToolUse',
    tool_name: 'Write',
    tool_input: {
      file_path: targetPath,
      content: `api token: "${plantedSecret}"`,
    },
  });

  try {
    assert.equal(result.status, 0, result.stderr);
    const decision = JSON.parse(result.stdout);
    assert.equal(decision.hookSpecificOutput.hookEventName, 'PreToolUse');
    assert.equal(decision.hookSpecificOutput.permissionDecision, 'deny');
    assert.match(decision.hookSpecificOutput.permissionDecisionReason, /Potential secret detected/);

    // Model the tool runtime: execute the write only when PreToolUse allows it.
    if (decision.hookSpecificOutput.permissionDecision !== 'deny') {
      fs.writeFileSync(targetPath, `api token: "${plantedSecret}"`);
    }
    assert.equal(fs.existsSync(targetPath), false, 'denied content must not reach disk');
  } finally {
    fs.rmSync(testDirectory, { recursive: true, force: true });
  }
});

test('planted secret in proposed Edit is denied', () => {
  const result = runHook({
    hook_event_name: 'PreToolUse',
    tool_name: 'Edit',
    tool_input: {
      file_path: 'blocked-edit-test.txt',
      old_string: 'safe',
      new_string: `token = "${'abc123'.repeat(6)}"`,
    },
  });

  assert.equal(result.status, 0, result.stderr);
  assert.equal(JSON.parse(result.stdout).hookSpecificOutput.permissionDecision, 'deny');
});

test('an unrelated placeholder cannot suppress a planted secret', () => {
  const result = runHook({
    hook_event_name: 'PreToolUse',
    tool_name: 'Write',
    tool_input: {
      file_path: 'placeholder-bypass-test.txt',
      content: `// example only\napi token: "${'ghp_' + 'B'.repeat(40)}"`,
    },
  });

  assert.equal(result.status, 0, result.stderr);
  assert.equal(JSON.parse(result.stdout).hookSpecificOutput.permissionDecision, 'deny');
});

test('clean proposed content is allowed without a deny response', () => {
  const result = runHook({
    hook_event_name: 'PreToolUse',
    tool_name: 'Write',
    tool_input: { file_path: 'clean-test.txt', content: 'Only harmless sample text.' },
  });

  assert.equal(result.status, 0, result.stderr);
  assert.equal(result.stdout, '');
});

test('malformed event is denied closed', () => {
  const result = spawnSync(process.execPath, [hookPath], { input: '{', encoding: 'utf8' });
  assert.equal(result.status, 0, result.stderr);
  assert.equal(JSON.parse(result.stdout).hookSpecificOutput.permissionDecision, 'deny');
});

test('Claude Code settings register the blocking hook for file mutation tools', () => {
  const settings = JSON.parse(fs.readFileSync(settingsPath, 'utf8'));
  const registration = settings.hooks.PreToolUse.find(hook => hook.matcher === '^(Write|Edit|MultiEdit|NotebookEdit)$');
  assert.ok(registration, 'PreToolUse matcher must cover supported file mutation tools');
  assert.match(registration.hooks[0].command, /pretooluse-hook\.js$/);
});