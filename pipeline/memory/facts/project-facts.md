# Project facts (repo-managed memory)

> Purpose: stable, reusable facts that help BA/Dev/Test stages without relying on chat history.
> 
> Rules: keep entries short; include provenance; update/supersede rather than duplicating.

---

## mem-proxy-ipv4-001 (fact) [scope: project]
**Statement:** Angular dev proxy for `/api` must target `http://127.0.0.1:8080` (not `localhost`) to avoid Windows IPv6/dual-stack `ECONNREFUSED`/proxy failures.
**Provenance:** frontend/proxy.conf.json; backend bind config in backend/src/main/resources/application.yml.
**Observed_at:** 2026-10-09
**Confidence:** high
**Review_by:** 2026-12-31

---

## mem-flyway-testdb-reset-001 (workaround) [scope: backend]
**Statement:** If Flyway migration checksum mismatches occur in tests, delete stale SQLite test DB under `backend/target/test-db/` and re-run tests to recreate schema history.
**Provenance:** backend test runs using `jdbc:sqlite:target/test-db/serviceforge-test.db` (Flyway validates migrations on startup).
**Observed_at:** 2026-10-09
**Confidence:** medium
**Review_by:** 2026-12-31
