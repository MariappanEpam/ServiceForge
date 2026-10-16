# Reverse engineering skill — Step 1: Scan (inventory)

## Outputs
- pipeline/repo-understanding/<scope-slug>/source-inventory.md
- pipeline/repo-understanding/<scope-slug>/technology-context.md
- pipeline/repo-understanding/<scope-slug>/dependency-map.md
- pipeline/repo-understanding/<scope-slug>/cve-sast-secrets-report.md
- pipeline/repo-understanding/<scope-slug>/nfr-baseline.md (Status: locked)
- pipeline/repo-understanding/<scope-slug>/compliance-map.md (Status: locked)

## Tasks
- Inventory relevant files grouped by type/language.
- Identify stacks and repo shape.
- Parse dependency manifests and lockfiles.
- Record CVE/SAST/secrets scan status and findings (or Not Scanned list).
- Create locked NFR baseline and compliance map; if targets are unknown, mark NEEDS-EVIDENCE.
