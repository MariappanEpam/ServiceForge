// Lightweight local embedder to avoid external API calls.
// Produces a fixed-size vector using hashed token buckets.

const DIM = 384;

function tokenize(text) {
  return text
    .toLowerCase()
    .replace(/[^a-z0-9_\-\s]/g, ' ')
    .split(/\s+/)
    .filter(Boolean);
}

function fnv1a32(str) {
  let h = 0x811c9dc5;
  for (let i = 0; i < str.length; i++) {
    h ^= str.charCodeAt(i);
    h = Math.imul(h, 0x01000193);
  }
  return h >>> 0;
}

export function embed(text) {
  const v = new Array(DIM).fill(0);
  const tokens = tokenize(text);
  if (tokens.length === 0) return v;

  for (const t of tokens) {
    const h = fnv1a32(t);
    const idx = h % DIM;
    // signed contribution
    const sign = (h & 1) === 0 ? 1 : -1;
    v[idx] += sign;
  }

  // L2 normalize
  let norm = 0;
  for (let i = 0; i < DIM; i++) norm += v[i] * v[i];
  norm = Math.sqrt(norm) || 1;
  for (let i = 0; i < DIM; i++) v[i] = v[i] / norm;

  return v;
}

export function embedMany(texts) {
  return texts.map((t) => embed(t));
}

export const EMBEDDING_DIM = DIM;
