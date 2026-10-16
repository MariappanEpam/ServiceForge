import fs from 'node:fs/promises';
import path from 'node:path';
import fg from 'fast-glob';
import ignore from 'ignore';
import { SourcesSchema } from './config.js';

const DEFAULT_IGNORES = [
  '**/node_modules/**',
  '**/target/**',
  '**/dist/**',
  '**/.git/**',
  '**/.qdrant_data/**',
  '**/.chroma_data/**',
  '**/package-lock.json',
  '**/yarn.lock',
  '**/pnpm-lock.yaml',
];

function isTextLike(filePath) {
  const ext = path.extname(filePath).toLowerCase();
  return [
    '.md',
    '.txt',
    '.json',
    '.yml',
    '.yaml',
    '.properties',
    '.java',
    '.ts',
    '.js',
    '.html',
    '.css',
  ].includes(ext);
}

export async function loadSourcesIndex(repoRoot) {
  const p = path.join(repoRoot, 'pipeline', 'memory', 'semantic', 'index.sources.json');
  const raw = await fs.readFile(p, 'utf8');
  return SourcesSchema.parse(JSON.parse(raw));
}

export async function listSourceFiles(repoRoot, sources) {
  const ig = ignore().add(DEFAULT_IGNORES);

  // Also respect .gitignore if present
  try {
    const gitignore = await fs.readFile(path.join(repoRoot, '.gitignore'), 'utf8');
    ig.add(gitignore.split(/\r?\n/));
  } catch {
    // ignore
  }

  const patterns = sources.map((s) => path.posix.join(s.replace(/\\/g, '/'), '**/*'));
  const matches = await fg(patterns, {
    cwd: repoRoot,
    dot: false,
    onlyFiles: true,
    unique: true,
    followSymbolicLinks: false,
  });

  return matches
    .filter((p) => !ig.ignores(p))
    .filter((p) => isTextLike(p));
}

export async function readFileUtf8(repoRoot, relPath) {
  const abs = path.join(repoRoot, relPath);
  return fs.readFile(abs, 'utf8');
}
