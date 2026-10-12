import { z } from 'zod';

export const DEFAULT_CHROMA_URL = process.env.CHROMA_URL || 'http://localhost:8000';
export const DEFAULT_COLLECTION = process.env.CHROMA_COLLECTION || 'serviceforge-semantic';

export const SourcesSchema = z.object({
  version: z.number().int().positive(),
  updated: z.string().optional(),
  sources: z.array(z.string().min(1)),
});
