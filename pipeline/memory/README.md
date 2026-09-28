# Repo-managed memory

This repo stores **token-efficient memory** as files so agents can retrieve context without relying on chat history.

## Memory types

### 1) Summary memory (Decisions)
- Location: `pipeline/decisions/`
- One file per feature (and optionally a project-wide decision file).
- Keep entries short and stable.

### 2) Entity memory (Configurable aspects)
- Location: `pipeline/memory/entities/`
- Stores key configurable constants and business rules (e.g., travel buffer minutes).

### 3) Episodic memory (Bugs)
- Location: `pipeline/memory/episodes/`
- One file per bug: symptoms → root cause → fix → regression.

### 4) Semantic memory (Local index)
- Location: `pipeline/memory/semantic/`
- Stores a local semantic index artifact generated from specs/architecture/decisions.

## Conventions
- Prefer **small, structured** markdown/YAML/JSON.
- Avoid duplicating large code blocks.
- Link to the source-of-truth files when possible.
