export function chunkText(text, { maxChars = 1200, overlapChars = 200 } = {}) {
  const cleaned = text.replace(/\r\n/g, '\n');
  const chunks = [];

  let i = 0;
  while (i < cleaned.length) {
    const end = Math.min(i + maxChars, cleaned.length);
    const chunk = cleaned.slice(i, end).trim();
    if (chunk) chunks.push(chunk);

    if (end >= cleaned.length) break;
    i = Math.max(0, end - overlapChars);
  }

  return chunks;
}
