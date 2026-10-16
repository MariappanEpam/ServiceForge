# PR Suggestion Agent Skill — ServiceForge

## Purpose
Enable a dedicated agent to take an existing PR (draft or ready) and add review feedback as **code review suggestions** (inline, line-level) so maintainers can apply them directly.

## Required capabilities
To create true GitHub “suggested changes”, the agent must be able to submit a PR review with inline comments.

### Preferred tool
- GitHub CLI: `gh pr review <PR> --comment --body-file <file>`
  - The body must include inline suggestion blocks:
    - ```suggestion
      <replacement code>
      ```

### Alternative
- GitHub API (GraphQL/REST) to create a review with inline comments.

## Inputs
- PR number or URL
- Optional: focus areas

## Process
1) Gather context
   - Identify PR number.
   - Fetch changed files and diff.

2) Generate findings
   - Prefer small, safe, mechanical improvements.
   - Each suggestion must be tied to a specific file + line range.
   - Keep suggestions minimal and compilable.

3) Post review
   - Submit a PR review with inline suggestions.
   - Also include a short summary section.

## Guardrails
- Do not propose changes that introduce new dependencies unless required.
- Do not propose changes outside the PR scope.
- Never include secrets.
