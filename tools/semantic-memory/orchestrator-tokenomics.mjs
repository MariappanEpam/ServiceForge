/*
  Orchestrator tokenomics estimator (ESM)

  Purpose:
  - Estimate how much context (and therefore tokens) is being consumed during an end-to-end
    orchestrator run, using the Copilot chat transcript JSONL.

  Notes:
  - This is an estimate: tokens ~= chars/4.
  - We count:
      - user.message content
      - assistant.message content
      - assistant toolRequests arguments (often the largest payload)
      - tool.result payloads (if present in transcript)

  Usage (PowerShell):
    node tools/semantic-memory/orchestrator-tokenomics.mjs --transcript "<path-to-jsonl>" --out tools/semantic-memory/orchestrator-tokenomics-report.json
*/

import fs from 'node:fs';
import path from 'node:path';

function parseArgs(argv) {
  const args = { transcript: null, out: null };
  for (let i = 2; i < argv.length; i++) {
    const a = argv[i];
    if (a === '--transcript') args.transcript = argv[++i];
    else if (a === '--out') args.out = argv[++i];
  }
  return args;
}

function estTokens(chars) {
  return Math.round(chars / 4);
}

function safeStringify(x) {
  try {
    if (typeof x === 'string') return x;
    return JSON.stringify(x);
  } catch {
    return '';
  }
}

function main() {
  const { transcript, out } = parseArgs(process.argv);
  if (!transcript) {
    console.error('Missing --transcript <path-to-jsonl>');
    process.exit(2);
  }

  const p = path.resolve(transcript);
  const raw = fs.readFileSync(p, 'utf8');
  const lines = raw.split(/\r?\n/).filter(Boolean);

  const counts = { user: 0, assistant: 0, tool: 0, other: 0 };
  let userChars = 0;
  let assistantChars = 0;
  let toolChars = 0;
  let totalChars = 0;

  for (const line of lines) {
    let o;
    try {
      o = JSON.parse(line);
    } catch {
      continue;
    }

    const t = o.type;
    const d = o.data || {};

    if (t === 'user.message') {
      const s = String(d.content || '');
      counts.user++;
      userChars += s.length;
      totalChars += s.length;
      continue;
    }

    if (t === 'assistant.message') {
      const s = String(d.content || '');
      counts.assistant++;
      assistantChars += s.length;
      totalChars += s.length;

      if (Array.isArray(d.toolRequests)) {
        for (const tr of d.toolRequests) {
          const arg = tr.arguments ?? tr.argument ?? '';
          const a = safeStringify(arg);
          toolChars += a.length;
          totalChars += a.length;
        }
      }
      continue;
    }

    if (t === 'tool.result') {
      const s = safeStringify(d);
      counts.tool++;
      toolChars += s.length;
      totalChars += s.length;
      continue;
    }

    counts.other++;
  }

  const report = {
    transcript: p,
    lines: lines.length,
    counts,
    chars: { total: totalChars, user: userChars, assistant: assistantChars, tool: toolChars },
    estimatedTokens: {
      total: estTokens(totalChars),
      user: estTokens(userChars),
      assistant: estTokens(assistantChars),
      tool: estTokens(toolChars),
    },
    disclaimer:
      'Token counts are estimates (chars/4). For exact counts, integrate a tokenizer for your target model and/or call the LLM API with usage reporting.',
  };

  const json = JSON.stringify(report, null, 2);
  if (out) {
    const outPath = path.resolve(out);
    fs.writeFileSync(outPath, json, 'utf8');
    console.log(`Wrote ${outPath}`);
  } else {
    console.log(json);
  }
}

main();
