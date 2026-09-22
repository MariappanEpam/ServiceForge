# The "develop this feature" pipeline

This document defines the end-to-end SDLC workflow used by all AI agents and humans contributing to this repository.

Trigger:

> Develop this feature: <intent>

Every stage must update the active handoff record before passing work to the next stage.

---

# Handoff File

Location:

pipeline/handoffs/feature-N-handoff.md

Purpose:

- Track current feature status
- Record decisions
- Record approvals
- Record blockers
- Track current owner
- Provide auditability

Each agent must:

1. Read the latest handoff
2. Complete assigned work
3. Update handoff
4. Transfer ownership

---

# SDLC Workflow

## 1. BA Agent

Role:
- Understand feature intent
- Analyze requirements
- Identify dependencies
- Define scope
- Define acceptance criteria

Inputs:
- Feature intent
- Existing features
- Existing decisions
- Existing rules

Outputs:
- Feature Specification

Location:
pipeline/features/feature-N-<slug>.md

Handoff Update:
- Status = Specification Complete
- Owner = Architecture Agent
- Dependencies identified
- Definition of Done recorded

---

## 2. Architecture Design Agent

Role:
- Design solution architecture
- Define components
- Define integrations
- Define technology choices
- Address NFRs

Inputs:
- Approved Feature Specification

Outputs:
- Architecture Document

Location:
pipeline/architecture/feature-N-architecture.md

Handoff Update:
- Status = Architecture Complete
- Owner = Design Review Agent
- Architecture decisions recorded
- Risks recorded

---

## 3. Design Review Agent

Role:
- Review architecture
- Verify scalability
- Verify security
- Verify maintainability
- Verify standards compliance

Inputs:
- Architecture Document

Outputs:
- Design Review Report

Location:
pipeline/reviews/feature-N-review.md

Handoff Update:
- Status = Design Approved
- Owner = Implementation Planner Agent
- Review findings recorded
- Approval decision recorded

---

## 4. Implementation Planner Agent

Role:
- Create implementation roadmap
- Break architecture into epics
- Create feature tasks
- Define dependencies
- Define implementation sequence

Inputs:
- Feature Specification
- Approved Architecture

Outputs:
- Delivery Plan

Location:
pipeline/plans/feature-N-plan.md

Handoff Update:
- Status = Plan Approved
- Owner = Developer Agent
- Delivery phases recorded
- Dependencies recorded

---

## 5. Developer Agent

Role:
- Implement approved design
- Follow coding standards
- Follow repository rules
- Avoid scope expansion

Inputs:
- Feature Specification
- Architecture
- Delivery Plan
- Repository Rules

Outputs:
- Working Code

Handoff Update:
- Status = Development Complete
- Owner = Tester Agent
- Changed components recorded
- Technical notes recorded

---

## 6. Tester Agent

Role:
- Validate implementation
- Create automated tests
- Verify Definition of Done
- Verify acceptance criteria

Inputs:
- Feature Specification
- Code Changes

Outputs:
- Test Results

Handoff Update:
- Status = Testing Complete
- Owner = Release Review Agent
- Test evidence recorded
- Defects recorded

---

## 7. Bug Fix Agent (Conditional)

Triggered When:
- Test failures exist
- Production defects found
- Regression issues detected

Inputs:
- Defect details
- Test results
- Code changes

Outputs:
- Fix implementation
- Root cause analysis
- Prevention recommendations

Handoff Update:
- Status = Bug Fixed
- Owner = Tester Agent
- Root cause recorded
- Fix summary recorded

Tester Agent re-runs validation.

---

## 8. Release Review Agent

Role:
- Verify all gates passed
- Verify approvals exist
- Verify documentation exists
- Verify no blockers remain

Inputs:
- All prior artifacts
- Handoff file

Outputs:
- Release Recommendation

Handoff Update:
- Status = Ready For Release
- Owner = Human Approver
- Outstanding risks recorded

---

# Safe Recovery

Stop the workflow if:

- Specification is incomplete
- Definition of Done is missing
- Architecture is not approved
- Review fails
- Plan is incomplete
- Tests cannot execute
- Acceptance criteria cannot be verified

Never continue based on assumptions.

Escalate to a human.

---

# Human Approval Gates

Mandatory approvals:

1. Specification Approval
2. Architecture Approval
3. Release Approval

Development must not begin until architecture approval exists.

Release must not occur until testing is completed.

---

# Handoff Ownership Rule

Only one owner may exist at a time.

Example:

BA Agent
    ↓
Architecture Agent
    ↓
Design Review Agent
    ↓
Implementation Planner Agent
    ↓
Developer Agent
    ↓
Tester Agent
    ↓
Release Review Agent
    ↓
Human Approval

Each agent must update the handoff file before transferring ownership.