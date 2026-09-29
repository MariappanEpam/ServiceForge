import fs from 'node:fs/promises';
import path from 'node:path';

import { getChromaClient, getOrCreateCollection } from './chroma.js';
import { embed } from './embedder.js';
import { loadSourcesIndex, listSourceFiles, readFileUtf8 } from './sources.js';

function estimateTokens(text) {
  // Rough heuristic: ~4 chars per token for English-ish text.
  // This is an estimate for tokenomics comparison without calling an LLM.
  const s = String(text ?? '');
  return Math.ceil(s.length / 4);
}

function buildPrompt({ question, context }) {
  return (
    `You are a helpful assistant. Answer the question using ONLY the provided context.\n` +
    `If the answer is not in the context, say so.\n\n` +
    `Question: ${question}\n\n` +
    `Context:\n${context}\n`
  );
}

async function retrieveFromSemanticMemory(question, { nResults = 6 } = {}) {
  const client = getChromaClient();
  const collection = await getOrCreateCollection(client);

  const qEmb = embed(question);
  const res = await collection.query({
    queryEmbeddings: [qEmb],
    nResults,
    include: ['metadatas', 'documents', 'distances'],
  });

  const docs = res.documents?.[0] || [];
  const metas = res.metadatas?.[0] || [];

  const blocks = [];
  for (let i = 0; i < docs.length; i++) {
    const p = metas[i]?.path ?? 'unknown';
    const c = metas[i]?.chunk ?? 'unknown';
    blocks.push(`[${i + 1}] ${p} (chunk ${c})\n${String(docs[i]).trim()}`);
  }

  return blocks.join('\n\n');
}

async function retrieveFromRepoNaive(question, { maxFiles = 200 } = {}) {
  // "Repo using LLM" baseline: naive approach is to stuff a lot of repo text into the prompt.
  // We approximate this by concatenating the first N files from the semantic sources.
  const repoRoot = process.cwd().includes(path.sep + 'tools' + path.sep + 'semantic-memory')
    ? path.resolve(process.cwd(), '..', '..')
    : process.cwd();

  const sourcesIndex = await loadSourcesIndex(repoRoot);
  const files = await listSourceFiles(repoRoot, sourcesIndex.sources);

  const selected = files.slice(0, maxFiles);
  const parts = [];

  for (const rel of selected) {
    const txt = await readFileUtf8(repoRoot, rel);
    // cap per file to avoid runaway memory
    const capped = txt.length > 6000 ? txt.slice(0, 6000) : txt;
    parts.push(`FILE: ${rel}\n${capped}`);
  }

  return parts.join('\n\n');
}

async function main() {
  const question = process.argv.slice(2).join(' ').trim() || 'parts reservation rules';

  const semanticContext = await retrieveFromSemanticMemory(question, { nResults: 6 });
  const repoContext = await retrieveFromRepoNaive(question, { maxFiles: 200 });

  const semanticPrompt = buildPrompt({ question, context: semanticContext });
  const repoPrompt = buildPrompt({ question, context: repoContext });

  const report = {
    question,
    semantic: {
      contextChars: semanticContext.length,
      estimatedTokens: estimateTokens(semanticPrompt),
      note: 'Semantic retrieval: top-k chunks from ChromaDB',
    },
    repo_naive: {
      contextChars: repoContext.length,
      estimatedTokens: estimateTokens(repoPrompt),
      note: 'Naive repo stuffing baseline: first N files from sources (capped per file)',
    },
    savings: {
      estimatedTokensSaved: estimateTokens(repoPrompt) - estimateTokens(semanticPrompt),
      estimatedPercentSaved:
        estimateTokens(repoPrompt) > 0
          ? Math.round(
              ((estimateTokens(repoPrompt) - estimateTokens(semanticPrompt)) / estimateTokens(repoPrompt)) * 100
            )
          : null,
    },
    disclaimer:
      'Token counts are estimates (chars/4). For exact counts, integrate a tokenizer for your target model and/or call the LLM API with usage reporting.',
  };

  const outPath = path.join(process.cwd(), 'tokenomics-report.json');
  await fs.writeFile(outPath, JSON.stringify(report, null, 2), 'utf8');

  // Intentionally do not print the outcome.
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
