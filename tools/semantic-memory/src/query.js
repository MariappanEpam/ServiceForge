import path from 'node:path';

import { getChromaClient, getOrCreateCollection } from './chroma.js';
import { embed } from './embedder.js';

async function main() {
  const q = process.argv.slice(2).join(' ').trim();
  if (!q) {
    console.error('Usage: npm run query -- "your question"');
    process.exit(2);
  }

  // Ensure we run from repo root or tools/semantic-memory
  const repoRoot = process.cwd().includes(path.sep + 'tools' + path.sep + 'semantic-memory')
    ? path.resolve(process.cwd(), '..', '..')
    : process.cwd();
  void repoRoot; // reserved for future path resolution

  const client = getChromaClient();
  const collection = await getOrCreateCollection(client);

  const embedding = embed(q);

  const res = await collection.query({
    queryEmbeddings: [embedding],
    nResults: 6,
    include: ['metadatas', 'documents', 'distances'],
  });

  const out = [];
  const docs = res.documents?.[0] || [];
  const metas = res.metadatas?.[0] || [];
  const dists = res.distances?.[0] || [];

  for (let i = 0; i < docs.length; i++) {
    out.push({
      rank: i + 1,
      distance: dists[i],
      path: metas[i]?.path,
      chunk: metas[i]?.chunk,
      snippet: String(docs[i]).slice(0, 500),
    });
  }

  process.stdout.write(JSON.stringify({ query: q, matches: out }, null, 2) + '\n');
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
