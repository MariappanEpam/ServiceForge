# PR Review Agent Skill — ServiceForge

## Purpose
Provide a consistent, repeatable process for reviewing changes and creating a GitHub Pull Request with a structured review request.

This skill is designed to be used by a dedicated PR review agent that:
- Summarizes changes
- Checks pipeline artifacts and evidence
- Creates a PR (or updates an existing PR)
- Requests reviewers
- Posts a review checklist comment

## Inputs
- A feature number (e.g., Feature 8)
- The intended PR title and description
- Target base branch (default: `main`)
- Optional: head branch name (if not current)
- Optional: reviewer GitHub usernames

## Required tools
The agent must have access to:
- `Read`, `Grep`, `Glob` (repo inspection)
- GitHub MCP tools:
  - `github_create_pr`
  - `github_comment_pr`
  - `github_request_reviewers`
  - `github_list_prs`

## Review checklist (minimum)
1) Pipeline compliance
   - Spec exists under `pipeline/features/`
   - Implementation plan exists under `pipeline/implementation-plan/`
   - Test plan exists under `pipeline/test-plan/`
   - Test execution evidence exists under `test-execution/`
   - Test report exists under `pipeline/test-report/`
   - Review artifact exists under `pipeline/reviews/` (if applicable)

2) Quality
   - No obvious dead code
   - No hardcoded secrets
   - Logging does not leak sensitive data

3) Evidence
   - Links to HTML report(s) under `test-execution/**/reports/*.html`
   - If TRX is required by the repo checklist, ensure it exists and is referenced

## PR creation procedure
1) Determine the feature slug and locate the handoff file under `pipeline/handoffs/`.
2) Build a PR body with:
   - Summary
   - Scope (what changed)
   - Evidence links (repo-relative paths)
   - Known limitations / follow-ups
3) Call `github_create_pr` with title/body/base/head.
4) If reviewers were provided, call `github_request_reviewers`.
5) Post a final PR comment with the review checklist results using `github_comment_pr`.

## Output format
Return:
- PR URL
- A short checklist summary (Pass/Fail items)
- Any follow-up actions required
