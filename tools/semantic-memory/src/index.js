import path from 'node:path';
import crypto from 'node:crypto';

import { getChromaClient, getOrCreateCollection } from './chroma.js';
import { loadSourcesIndex, listSourceFiles, readFileUtf8 } from './sources.js';
import { chunkText } from './chunker.js';
import { embedMany } from './embedder.js';

function sha1(s) {
  return crypto.createHash('sha1').update(s).digest('hex');
}

function makeId(relPath, chunkIndex) {
  return `${relPath}::${chunkIndex}::${sha1(relPath + '::' + chunkIndex)}`;
}

async function main() {
  const repoRoot = process.cwd().includes(path.sep + 'tools' + path.sep + 'semantic-memory')
    ? path.resolve(process.cwd(), '..', '..')
    : process.cwd();

  const sourcesIndex = await loadSourcesIndex(repoRoot);
  const files = await listSourceFiles(repoRoot, sourcesIndex.sources);

  const client = getChromaClient();
  const collection = await getOrCreateCollection(client);

  let totalChunks = 0;
  let totalFiles = 0;

  for (const relPath of files) {
    const text = await readFileUtf8(repoRoot, relPath);
    const chunks = chunkText(text, { maxChars: 1400, overlapChars: 250 });
    if (chunks.length === 0) continue;

    const ids = chunks.map((_, i) => makeId(relPath, i));
    const embeddings = embedMany(chunks);
    const metadatas = chunks.map((c, i) => ({
      path: relPath,
      chunk: i,
      chars: c.length,
    }));

    // Upsert by deleting existing ids then adding.
    // (Chroma supports upsert in newer APIs; delete+add is compatible.)
    try {
      await collection.delete({ ids });
    } catch {
      // ignore if not present
    }

    await collection.add({
      ids,
      embeddings,
      documents: chunks,
      metadatas,
    });

    totalFiles += 1;
    totalChunks += chunks.length;
  }

  // Basic stats
  let count = null;
  try {
    count = await collection.count();
  } catch {
    // ignore
  }

  process.stdout.write(
    JSON.stringify(
      {
        ok: true,
        indexedFiles: totalFiles,
        indexedChunks: totalChunks,
        collectionCount: count,
      },
      null,
      2
    ) + '\n'
  );
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
