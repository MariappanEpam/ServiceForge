import path from 'node:path';

import { getChromaClient, getOrCreateCollection } from './chroma.js';
import { embed } from './embedder.js';

function escapeFence(s) {
  return String(s).replace(/```/g, "``\u200b`");
}

async function main() {
  const q = process.argv.slice(2).join(' ').trim();
  if (!q) {
    console.error('Usage: npm run prompt -- "your question"');
    process.exit(2);
  }

  const repoRoot = process.cwd().includes(path.sep + 'tools' + path.sep + 'semantic-memory')
    ? path.resolve(process.cwd(), '..', '..')
    : process.cwd();
  void repoRoot;

  const client = getChromaClient();
  const collection = await getOrCreateCollection(client);

  const embedding = embed(q);

  const res = await collection.query({
    queryEmbeddings: [embedding],
    nResults: 6,
    include: ['metadatas', 'documents', 'distances'],
  });

  const docs = res.documents?.[0] || [];
  const metas = res.metadatas?.[0] || [];
  const dists = res.distances?.[0] || [];

  const blocks = [];
  for (let i = 0; i < docs.length; i++) {
    const p = metas[i]?.path ?? 'unknown';
    const c = metas[i]?.chunk ?? 'unknown';
    const dist = dists[i];
    const snippet = escapeFence(String(docs[i]).trim());

    blocks.push(
      `### Source ${i + 1}: ${p} (chunk ${c}, distance ${typeof dist === 'number' ? dist.toFixed(4) : dist})\n\n` +
        '```\n' +
        snippet +
        '\n```'
    );
  }

  const out =
    `# Retrieved context (semantic memory)\n\n` +
    `Query: ${q}\n\n` +
    `Use the following context to answer. If the answer is not in the context, say so.\n\n` +
    blocks.join('\n\n');

  process.stdout.write(out + '\n');
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
