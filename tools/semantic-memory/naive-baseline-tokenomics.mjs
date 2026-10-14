/*
  Naive baseline tokenomics estimator (ESM)

  Purpose:
  - Estimate a "no memory" baseline by simulating naive repo stuffing:
    read a broad set of pipeline sources and sum their character sizes.

  This is intentionally simple and deterministic:
  - tokens ~= chars/4
  - reads files from:
      pipeline/features
      pipeline/implementation-plan
      pipeline/architecture
      pipeline/decisions
      pipeline/reviews
      pipeline/rules
      pipeline/memory (entities/facts/episodes)

  Usage:
    node tools/semantic-memory/naive-baseline-tokenomics.mjs --out tools/semantic-memory/orchestrator-tokenomics-naive-baseline.json

  Options:
    --maxFiles <n>        (default 200)
    --maxCharsPerFile <n> (default 12000)

  Notes:
  - This is not "what the model actually saw"; it's a comparable budget estimate.
*/

import fs from 'node:fs';
import path from 'node:path';

function parseArgs(argv) {
  const args = { out: null, maxFiles: 200, maxCharsPerFile: 12000 };
  for (let i = 2; i < argv.length; i++) {
    const a = argv[i];
    if (a === '--out') args.out = argv[++i];
    else if (a === '--maxFiles') args.maxFiles = Number(argv[++i]);
    else if (a === '--maxCharsPerFile') args.maxCharsPerFile = Number(argv[++i]);
  }
  return args;
}

function estTokens(chars) {
  return Math.round(chars / 4);
}

function listFilesRecursive(dir) {
  const out = [];
  if (!fs.existsSync(dir)) return out;
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const e of entries) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) out.push(...listFilesRecursive(p));
    else if (e.isFile()) out.push(p);
  }
  return out;
}

function readCapped(filePath, maxChars) {
  try {
    const s = fs.readFileSync(filePath, 'utf8');
    return s.length > maxChars ? s.slice(0, maxChars) : s;
  } catch {
    return '';
  }
}

function main() {
  const { out, maxFiles, maxCharsPerFile } = parseArgs(process.argv);

  const roots = [
    'pipeline/features',
    'pipeline/implementation-plan',
    'pipeline/architecture',
    'pipeline/decisions',
    'pipeline/reviews',
    'pipeline/rules',
    'pipeline/memory/entities',
    'pipeline/memory/facts',
    'pipeline/memory/episodes',
  ];

  const allFiles = roots.flatMap((r) => listFilesRecursive(path.resolve(r)));

  // Prefer text-ish files; keep deterministic ordering.
  const preferred = allFiles
    .filter((p) => /\.(md|yml|yaml|json|txt)$/i.test(p))
    .sort((a, b) => a.localeCompare(b));

  const selected = preferred.slice(0, maxFiles);

  let totalChars = 0;
  const files = [];

  for (const p of selected) {
    const content = readCapped(p, maxCharsPerFile);
    totalChars += content.length;
    files.push({
      path: path.relative(process.cwd(), p).replace(/\\/g, '/'),
      charsCounted: content.length,
    });
  }

  const report = {
    version: 1,
    note:
      'Naive baseline estimate: broad pipeline repo stuffing (capped per file). Token counts are estimates (chars/4).',
    config: { maxFiles, maxCharsPerFile, roots },
    selectedFiles: files.length,
    totalChars,
    estimatedTokens: estTokens(totalChars),
    topFiles: files.slice(0, 25),
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
