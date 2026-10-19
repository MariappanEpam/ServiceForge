#!/usr/bin/env node
/*
 * Estimate potential context reduction for a CodeGraph-assisted reverse-engineering run.
 *
 * This is a proxy, not a measurement of model billing or proof of causality. It compares:
 *   - all tracked source/config text in backend/, frontend/, and test-automation/; with
 *   - unique source/config files cited by generated reverse-engineering Markdown.
 *
 * Tokens use chars/4. Optional USD estimation requires CODEGRAPH_INPUT_USD_PER_MILLION.
 */
import fs from 'node:fs';
import path from 'node:path';

const REPO_ROOT = process.cwd();
const ROOTS = ['backend', 'frontend', 'test-automation'];
const SKIP_DIRS = new Set([
  '.git', '.codegraph', 'node_modules', 'target', 'dist', 'build', 'coverage',
  'bin', 'obj', 'TestResults', '.angular', '.next', 'vendor',
]);
const SOURCE_EXTENSIONS = new Set([
  '.java', '.kt', '.scala', '.ts', '.tsx', '.js', '.jsx', '.mjs', '.cjs',
  '.cs', '.html', '.htm', '.css', '.scss', '.sass', '.less', '.sql', '.xml',
  '.yml', '.yaml', '.json', '.properties', '.gradle', '.kts', '.sh', '.ps1',
  '.py', '.go', '.rs', '.vue', '.svelte', '.graphql', '.gql', '.md',
]);

function parseArgs(argv) {
  const args = { scope: 'legacy-root', price: process.env.CODEGRAPH_INPUT_USD_PER_MILLION ?? null };
  for (let i = 2; i < argv.length; i += 1) {
    if (argv[i] === '--scope') args.scope = argv[++i];
    else if (argv[i] === '--usd-per-million') args.price = argv[++i];
  }
  return args;
}

function walkFiles(dir, predicate = () => true) {
  if (!fs.existsSync(dir)) return [];
  const result = [];
  for (const item of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, item.name);
    if (item.isDirectory()) {
      if (!SKIP_DIRS.has(item.name)) result.push(...walkFiles(full, predicate));
    } else if (item.isFile() && predicate(full)) {
      result.push(full);
    }
  }
  return result;
}

function normalizeRepoPath(value) {
  return value.replace(/\\/g, '/').replace(/^\.\//, '').split('#')[0].replace(/:\d+(?:-\d+)?$/, '');
}

function citedSourcePaths(markdownFiles, knownSources) {
  const found = new Set();
  // Count only source paths explicitly followed by a line citation.
  const pathPattern = /((?:backend|frontend|test-automation)\/[A-Za-z0-9._@+~%/-]+\.[A-Za-z0-9]+)(?=#L\d+(?:-L\d+)?|:\d+(?:-\d+)?)/gim;
  for (const mdPath of markdownFiles) {
    const content = fs.readFileSync(mdPath, 'utf8');
    for (const match of content.matchAll(pathPattern)) {
      const relative = normalizeRepoPath(match[1]);
      if (knownSources.has(relative)) found.add(relative);
    }
  }
  return [...found].sort();
}

function measure(paths) {
  let chars = 0;
  const rows = [];
  for (const relative of paths) {
    const text = fs.readFileSync(path.resolve(REPO_ROOT, relative), 'utf8');
    chars += text.length;
    rows.push({ path: relative, chars: text.length, estimatedTokens: Math.round(text.length / 4) });
  }
  return { fileCount: rows.length, chars, estimatedTokens: Math.round(chars / 4), files: rows };
}

function readIndexStats(outDir) {
  const stats = {};
  for (const label of ROOTS) {
    const file = path.join(outDir, `codegraph-status-${label}.txt`);
    if (!fs.existsSync(file)) {
      stats[label] = { status: 'missing' };
      continue;
    }
    const text = fs.readFileSync(file, 'utf8');
    const match = text.match(/Files:\s*([\d,]+)/i);
    stats[label] = {
      status: match ? 'reported' : 'unparsed',
      indexedFiles: match ? Number(match[1].replace(/,/g, '')) : null,
      hasNoFilesMessage: /No files indexed/i.test(text),
    };
  }
  return stats;
}

function main() {
  const { scope, price } = parseArgs(process.argv);
  if (!/^[a-z0-9][a-z0-9-]*$/i.test(scope)) {
    throw new Error(`Invalid scope slug: ${scope}`);
  }

  const outputDir = path.resolve(REPO_ROOT, 'pipeline', 'repo-understanding', scope, 'codegraph');
  if (!fs.existsSync(outputDir)) throw new Error(`CodeGraph output folder not found: ${outputDir}`);

  const sourcePaths = ROOTS.flatMap((root) => walkFiles(path.resolve(REPO_ROOT, root), (file) =>
    SOURCE_EXTENSIONS.has(path.extname(file).toLowerCase()),
  )).map((file) => path.relative(REPO_ROOT, file).replace(/\\/g, '/')).sort();
  const knownSources = new Set(sourcePaths);
  const outputMarkdown = walkFiles(path.resolve(REPO_ROOT, 'pipeline', 'repo-understanding', scope), (file) =>
    file.toLowerCase().endsWith('.md') && !file.replace(/\\/g, '/').toLowerCase().includes('/codegraph/'),
  );
  const cited = citedSourcePaths(outputMarkdown, knownSources);
  const baseline = measure(sourcePaths);
  const evidenceSet = measure(cited);
  const potentialAvoidedTokens = Math.max(0, baseline.estimatedTokens - evidenceSet.estimatedTokens);
  const pricePerMillion = price === null || price === '' ? null : Number(price);
  if (pricePerMillion !== null && (!Number.isFinite(pricePerMillion) || pricePerMillion < 0)) {
    throw new Error('--usd-per-million must be a non-negative number');
  }

  const report = {
    version: 1,
    generatedAt: new Date().toISOString(),
    scope,
    metric: 'potential context avoided by targeted evidence selection (proxy)',
    method: 'Compare chars/4 across source/config text in the three indexed folders against unique source/config files cited by generated reverse-engineering Markdown.',
    baseline,
    evidenceReferenced: evidenceSet,
    estimatedPotentialTokensAvoided: potentialAvoidedTokens,
    estimatedPotentialReductionPercent: baseline.estimatedTokens === 0
      ? 0
      : Number((potentialAvoidedTokens / baseline.estimatedTokens * 100).toFixed(2)),
    pricing: {
      inputUsdPerMillionTokens: pricePerMillion,
      estimatedPotentialUsdAvoided: pricePerMillion === null
        ? null
        : Number((potentialAvoidedTokens * pricePerMillion / 1_000_000).toFixed(8)),
    },
    codegraphIndex: readIndexStats(outputDir),
    limitations: [
      'This is an estimate of broad source-context avoided, not actual prompt tokens, provider billing, or a causal A/B comparison.',
      'Cited files are a lower-bound proxy for evidence inspected; files read but not cited are not counted.',
      'Some cited content may not have been sent to the model, and this estimate does not subtract the CodeGraph snapshot or interaction overhead.',
      'The backend CodeGraph index may report zero supported files; source citations remain the evidence authority.',
      'Dollar values are omitted unless CODEGRAPH_INPUT_USD_PER_MILLION or --usd-per-million is explicitly provided.',
    ],
  };

  const jsonPath = path.join(outputDir, 'token-savings-report.json');
  const markdownPath = path.join(outputDir, 'token-savings-report.md');
  fs.writeFileSync(jsonPath, `${JSON.stringify(report, null, 2)}\n`, 'utf8');
  const usdLine = report.pricing.estimatedPotentialUsdAvoided === null
    ? 'USD estimate: not calculated (input-token price not configured).'
    : `Estimated potential input cost avoided: $${report.pricing.estimatedPotentialUsdAvoided} at $${pricePerMillion}/1M tokens.`;
  const md = [
    `# CodeGraph-assisted context savings estimate: ${scope}`,
    '',
    `Generated: ${report.generatedAt}`,
    '',
    '> Proxy estimate only; not actual token usage, billed cost, or causal proof that CodeGraph produced the reduction.',
    '',
    '## Estimate',
    `- Broad source/config baseline: ${baseline.fileCount} files; ${baseline.estimatedTokens.toLocaleString()} estimated tokens (chars/4).`,
    `- Unique source/config files cited in RE artifacts: ${evidenceSet.fileCount}; ${evidenceSet.estimatedTokens.toLocaleString()} estimated tokens (chars/4).`,
    `- Estimated potential context avoided: ${potentialAvoidedTokens.toLocaleString()} tokens (${report.estimatedPotentialReductionPercent}%).`,
    `- ${usdLine}`,
    '',
    '## CodeGraph index coverage',
    ...ROOTS.map((label) => {
      const item = report.codegraphIndex[label];
      return `- ${label}: ${item.status === 'reported' ? `${item.indexedFiles} indexed file(s)${item.hasNoFilesMessage ? ' (no-files warning)' : ''}` : item.status}.`;
    }),
    '',
    '## Limitations',
    ...report.limitations.map((item) => `- ${item}`),
    '',
    '## Files cited (measured proxy)',
    ...(evidenceSet.files.length ? evidenceSet.files.map((item) => `- ${item.path}: ${item.estimatedTokens.toLocaleString()} estimated tokens`) : ['- No eligible source-file citations detected in the generated Markdown.']),
    '',
  ].join('\n');
  fs.writeFileSync(markdownPath, md, 'utf8');
  console.log(md);
  console.log(`Wrote ${jsonPath}`);
  console.log(`Wrote ${markdownPath}`);
}

main();
