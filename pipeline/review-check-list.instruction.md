# Review Agent Checklist

## Purpose

The Review Agent performs a structured validation across five critical review areas:

1. Jira AC Verification
2. Quality Audit
3. Security Review
4. Compliance Review
5. Design Alignment

---

# 1. Jira AC Verification

## Objective

Verify every Acceptance Criterion (AC) is implemented correctly and fulfills the intended business outcome.

### Checklist

- [ ] Every Jira Acceptance Criterion is mapped to implemented functionality.
- [ ] Implementation satisfies the business intent, not just the literal wording.
- [ ] Positive scenarios are covered.
- [ ] Negative scenarios are covered.
- [ ] Boundary conditions are covered.
- [ ] Error and exception paths are covered.
- [ ] No ACs are partially implemented.
- [ ] No ACs are skipped without documented approval.
- [ ] User workflow aligns with expected business process.
- [ ] Test evidence exists for every AC.
- [ ] Requirement traceability is maintained.

### Findings Template

```text
AC Status:
✓ AC-1 Implemented
✓ AC-2 Implemented
⚠ AC-3 Partially Implemented
✗ AC-4 Missing Validation Scenario

Overall AC Coverage: XX%
```

---

# 2. Quality Audit

## Objective

Ensure implementation is maintainable, readable, testable, and free from unnecessary complexity.

## Code Quality

- [ ] Naming conventions followed consistently.
- [ ] Classes, methods, and variables are descriptive.
- [ ] No dead or unreachable code.
- [ ] No duplicate logic or copy-paste implementations.
- [ ] SOLID principles appropriately applied.
- [ ] Complexity remains manageable.
- [ ] Logging is sufficient and meaningful.
- [ ] Exception handling is clear and actionable.
- [ ] Code readability meets team standards.
- [ ] Comments explain intent rather than implementation details.

## Scope Control

- [ ] No unauthorized features introduced.
- [ ] No hidden requirements implemented.
- [ ] No unnecessary framework or library additions.
- [ ] Solution remains within approved Jira scope.
- [ ] Technical debt is documented.

## Testing Quality

- [ ] Unit tests added where required.
- [ ] Test names clearly describe behavior.
- [ ] Critical paths validated.
- [ ] Edge cases tested.
- [ ] Regression risks evaluated.

---

# 3. Security Review

## Objective

Identify application, infrastructure, and AI-specific security risks.

## OWASP Security Review

- [ ] Input validation implemented.
- [ ] Output encoding applied where needed.
- [ ] Authentication controls enforced.
- [ ] Authorization controls verified.
- [ ] Sensitive data protected.
- [ ] SQL injection protection verified.
- [ ] XSS protection verified.
- [ ] CSRF protection verified.
- [ ] Rate limiting considered.
- [ ] File upload security validated.
- [ ] Error messages do not expose internal details.

## Secrets Management

- [ ] Secrets-scan hook reported clean for this diff.
- [ ] No hardcoded credentials identified.
- [ ] No API keys committed.
- [ ] No connection strings exposed.
- [ ] No secrets present in logs.
- [ ] Environment variables used appropriately.

## LLM / Agentic Security

- [ ] Prompt injection risks reviewed.
- [ ] Tool invocation permissions constrained.
- [ ] User input sanitized before LLM consumption.
- [ ] Memory access scope validated.
- [ ] Sensitive information excluded from prompts.
- [ ] Least-privilege principles enforced.
- [ ] Retrieval sources validated.
- [ ] Model outputs validated before execution.
- [ ] Hallucination-related risks assessed.
- [ ] Agent routing cannot bypass security controls.

### Findings Template

```text
Security Status: PASS

Findings:
✓ No critical vulnerabilities
✓ Secrets scan clean
⚠ Missing rate-limiting controls
```

---

# 4. Compliance Review

## Objective

Ensure implementation complies with project standards, architecture rules, and previously approved decisions.

## Project Standards

- [ ] All approved rule files followed.
- [ ] Architecture constraints respected.
- [ ] Coding standards followed.
- [ ] Repository conventions followed.
- [ ] API standards followed.
- [ ] Naming standards followed.
- [ ] Logging standards followed.
- [ ] Error handling standards followed.
- [ ] Documentation standards followed.

## Historical Compliance

- [ ] Previous design decisions maintained.
- [ ] Previous architectural commitments preserved.
- [ ] Earlier review findings resolved.
- [ ] Previously fixed issues not reintroduced.
- [ ] Existing business rules remain intact.

## Governance

- [ ] Changes traceable to Jira work item.
- [ ] Audit trail maintained.
- [ ] Required approvals documented.
- [ ] Compliance exceptions documented and approved.

### Findings Template

```text
Compliance Status: PASS

Violations:
None

Observations:
Previous architecture commitments remain intact.
```

---

# 5. Design Alignment Review

## Objective

Validate that implementation remains aligned with approved requirements, architecture, and intended system design.

## Requirements Alignment

- [ ] Solution aligns with approved requirements.
- [ ] Business workflow remains unchanged unless approved.
- [ ] Functional behavior matches design intent.
- [ ] Non-functional requirements addressed.

## Architecture Alignment

- [ ] Approved architecture followed.
- [ ] Layer boundaries respected.
- [ ] Service responsibilities preserved.
- [ ] Integration patterns unchanged.
- [ ] Data ownership boundaries respected.
- [ ] Existing contracts preserved.

## Scope Drift Detection

- [ ] No unauthorized feature additions.
- [ ] No hidden architectural changes.
- [ ] No unexpected dependencies introduced.
- [ ] No technology stack deviations.
- [ ] Design remains within agreed boundaries.

## Future Impact Assessment

- [ ] Scalability impact reviewed.
- [ ] Maintainability impact reviewed.
- [ ] Operational impact reviewed.
- [ ] Performance impact reviewed.

### Findings Template

```text
Design Alignment Status: PASS

Drift Analysis:
✓ Architecture unchanged
✓ Requirements aligned
⚠ Additional caching layer introduced

Action:
Requires architecture approval.
```

---

# Final Review Summary

```text
Review Summary

1. Jira AC Verification      : PASS | FAIL
2. Quality Audit            : PASS | FAIL
3. Security Review          : PASS | FAIL
4. Compliance Review        : PASS | FAIL
5. Design Alignment Review  : PASS | FAIL

Critical Findings : 0
Major Findings    : 0
Minor Findings    : 0

Recommendation:
APPROVE
APPROVE WITH COMMENTS
REJECT

Required Actions:
1.
2.
3.
```

---

# Review Agent Approval Criteria

## APPROVE

- No critical findings
- No major findings
- All five review areas pass

## APPROVE WITH COMMENTS

- No critical findings
- Minor issues exist
- Does not block deployment or merge

## REJECT

- Any critical finding present
- Significant AC gap identified
- Security violation exists
- Compliance violation detected
- Architecture or design drift found without approval