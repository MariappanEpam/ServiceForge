import { ChromaClient } from 'chromadb';
import { DEFAULT_CHROMA_URL, DEFAULT_COLLECTION } from './config.js';

export function getChromaClient() {
  return new ChromaClient({ path: DEFAULT_CHROMA_URL });
}

export async function getOrCreateCollection(client, name = DEFAULT_COLLECTION) {
  // chromadb client supports getOrCreateCollection
  return client.getOrCreateCollection({ name });
}
