# Semantic memory (local)

This folder contains a small local indexing + query utility that:
- reads semantic sources from `pipeline/memory/semantic/index.sources.json`
- chunks markdown/text-ish files
- stores embeddings + documents in **ChromaDB** (running via Docker)

## Prereqs

- Docker Desktop running
- ChromaDB running:
  - `docker compose -f pipeline/memory/semantic/vector-db/docker-compose.yml up -d`

## Install

From repo root:
- `cd tools/semantic-memory`
- `npm install`

## Index

- `npm run index`

This creates/updates a Chroma collection named `serviceforge-semantic`.

## Query

- `npm run query -- "how does parts reservation work?"`

Outputs the top matches with file path + snippet.

## Notes

- Embeddings are computed locally using a lightweight hashing embedder (no external LLM calls).
- This is intended for **tokenomics**: retrieve a small set of relevant snippets before prompting.
