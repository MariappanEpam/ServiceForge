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

## Orchestrator tokenomics (end-to-end)

To estimate token consumption for a full orchestrator run (not individual questions), use:

- `node tools/semantic-memory/orchestrator-tokenomics.mjs --transcript "<path-to-copilot-transcript.jsonl>" --out tools/semantic-memory/orchestrator-tokenomics-report.json`

To estimate a naive "no memory" baseline (repo stuffing of pipeline sources), use:

- `node tools/semantic-memory/naive-baseline-tokenomics.mjs --out tools/semantic-memory/orchestrator-tokenomics-naive-baseline.json`

Both reports use a simple heuristic: $tokens \approx chars/4$.
