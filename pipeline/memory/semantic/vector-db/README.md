# Local Vector DB (semantic memory)

This repo currently stores **semantic memory sources** in `pipeline/memory/semantic/index.sources.json`.

To enable a **local vector database** for semantic retrieval, we run a local ChromaDB instance via Docker.

## Start ChromaDB (local)

From repo root:

- `docker compose -f pipeline/memory/semantic/vector-db/docker-compose.yml up -d`

ChromaDB will be available at:
- HTTP: `http://localhost:8000`

Data is persisted to:
- `pipeline/memory/semantic/vector-db/.chroma_data/`

## Stop

- `docker compose -f pipeline/memory/semantic/vector-db/docker-compose.yml down`

## Notes

- This is **local-only** and intended for development.
- No embeddings/indexing pipeline is wired yet; this only provides the vector DB runtime.
