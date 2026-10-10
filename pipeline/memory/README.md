# Repo-managed memory

This repo stores **token-efficient memory** as files so agents can retrieve context without relying on chat history.

This document defines the **B1/B2 memory contract** reviewers asked for: memory design, retention, retrieval, write/read round trip, token limits, provenance, and pipeline integration.

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

### 3a) Facts memory (Stable facts)
- Location: `pipeline/memory/facts/`
- Stable, reusable facts and workarounds that should survive beyond a single episode.
- Must include provenance and a review/expiry date.

### 4) Semantic memory (Local index)
- Location: `pipeline/memory/semantic/`
- Stores a local semantic index artifact generated from specs/architecture/decisions.

## Conventions
- Prefer **small, structured** markdown/YAML/JSON.
- Avoid duplicating large code blocks.
- Link to the source-of-truth files when possible.

---

## B1 — Memory design

### Goals
- Persist only **feature-relevant, reusable facts** that help later pipeline steps (BA → Dev → Test).
- Keep memory **repo-local** (no dependency on user profile paths or external logs).
- Make memory **auditable** (provenance + timestamps + confidence).

### Storage layout (source of truth)
1) Decisions: `pipeline/decisions/`
2) Rules: `pipeline/rules/`
3) Facts: `pipeline/memory/facts/`
4) Episodes: `pipeline/memory/episodes/`
5) Semantic index (derived): `pipeline/memory/semantic/`

Semantic memory is an **acceleration layer** only; the markdown files above remain the source of truth.

### Memory entry schema (provenance-first)
Each memory entry should be a small block containing:
- `id`: stable identifier (e.g., `mem-proxy-ipv4-001`)
- `type`: `fact | decision | workaround | constraint`
- `scope`: `project | feature-N | backend | frontend | pipeline`
- `statement`: 1–3 sentences
- `provenance`: link to repo file(s) and/or committed test evidence
- `observed_at`: date
- `confidence`: `high|medium|low`
- `review_by` (preferred) or `expires_at`

### Token limits (compaction contract)
- Per entry: target **≤ 120 tokens**.
- Retrieval response: target **≤ 600 tokens** unless explicitly asked for deep detail.

---

## B1 — Retention rules

### Facts / Decisions
- Retain **indefinitely** until superseded.
- Must include provenance.
- When superseded, update the existing entry (or add a new entry that explicitly references the old one).

### Workarounds
- Retain with a `review_by` date.
- Remove or downgrade once no longer applicable.

### Episodes
- Retain **90 days** (or longer if repo size is not a concern).
- Episodes should stay short and link to the durable fact/decision if one was promoted.

---

## B2 — Retrieval skill (how agents query memory)

### Retrieval order (must prefer)
1) `pipeline/decisions/`
2) `pipeline/rules/`
3) `pipeline/memory/facts/`
4) `pipeline/memory/episodes/` (only if needed)

### Minimum viable retrieval algorithm
- Search the above locations for keywords.
- Return the top matches, prioritizing:
	- scope match (feature/project)
	- confidence
	- recency
- Always include provenance links.

---

## B2 — Write/read round trip

### Write path (when to write memory)
Write or update memory when:
- A fix changes how the repo runs (ports, proxy, DB, migrations).
- A decision affects API shape/compatibility.
- A non-obvious test failure is diagnosed and resolved.

Where:
- Durable facts/workarounds → `pipeline/memory/facts/`
- Decisions → `pipeline/decisions/`
- Operational notes → `pipeline/memory/episodes/`

### Read path (when to read memory)
Read memory at the start of each pipeline phase:
- BA: decisions + rules + relevant facts
- Dev: feature rules/decisions + relevant facts
- Test: acceptance criteria + constraints/workarounds

---

## B2 — Pipeline integration

### Pre-hook (memory retrieval)
Before BA/Dev/Test work begins, retrieve and surface relevant memory for the feature.

### Post-hook (memory write)
After Dev/Test completes, promote any durable learnings into `pipeline/memory/facts/` (with provenance) and keep episodes short.

---

## Provenance requirements
Every durable memory entry must link to at least one:
- repo file (preferred), or
- committed test evidence under `pipeline/test-execution/` or `pipeline/test-report/`.

Avoid provenance that points to user profile paths (non-repo locations).
