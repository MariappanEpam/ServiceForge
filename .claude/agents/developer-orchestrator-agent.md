---
name: developer-orchestrator-agent
description: Implements a committed feature spec into working backend/frontend code, following this repo's stack conventions and any active rules. Use this agent only after a spec file already exists under pipeline/implementation-plan/. Do not use it to invent scope that isn't in the spec.
tools: Read, Grep, Glob, Write, Edit, Bash
model: inherit
agents:
  - alias: backend-developer-agent
    base_agent: developer-agent
    role: Turns a user story or feature into working backend code, following this repo's stack conventions and any active rules. Use this agent only after a user story or feature exist in the Open project local instance. Cover all the acceptance criteria in the spec. Do not use it to invent scope that isn't in the spec.
    model: inherit
  - alias: frontend-developer-agent
    base_agent: developer-agent
    role: Turns a user story or feature into working frontend code, following this repo's stack conventions and any active rules. Use this agent only after a user story or feature exist in the Open project local instance. Cover all the acceptance criteria in the spec. Do not use it to invent scope that isn't in the spec.
    model: inherit
  - alias: unit-tester-agent
    base_agent: developer-agent
    role: Validates that the implemented feature meets the acceptance criteria in the spec. Use this agent only after a user story or feature exist in the Open project local instance that are development complete by the backend-developer-agent and frontend-developer-agent. Implement unit tests to ensure the feature functions as expected. Do not use it to invent scope that isn't in the spec.
    model: inherit

routing:
  - from: backend-developer-agent
    to: unit-tester-agent
  - from: frontend-developer-agent
    to: unit-tester-agent
  - from: unit-tester-agent
    to: backend-developer-agent
    condition: test_status == "failed" && component == "backend"
  - from: unit-tester-agent
    to: frontend-developer-agent
    condition: test_status == "failed" && component == "frontend"
  - from: unit-tester-agent
    to: frontend-developer-agent
    condition: test_status == "passed" && previous_agent == "backend-developer-agent"

post_hooks:
  - name: "backend-unit-test-failure"
    when: "unit_test_status == 'failed' && component == 'backend'"
    description: |
      Record backend test failures in episodic memory and reroute execution
      to backend-developer-agent.
  - name: "frontend-unit-test-failure"
    when: "unit_test_status == 'failed' && component == 'frontend'"
    description: |
      Record frontend test failures in episodic memory and reroute execution
      to frontend-developer-agent.
  - name: "unit-test-success"
    when: "unit_test_status == 'passed'"
    description: |
      Record successful test execution and continue workflow to the next
      planned agent.
  - name: failed-unit-test-hook
    description: If the unit tests fail, this hook will trigger a reroute to the appropriate developer agent for code adjustments.
    model: inherit

memory: "project"
memory_metadata: 
  - "memory/entities/*.yml"
  - "memory/episodes/*.md"
  - "memory/semantic/vector-db/"

guardrails:
  -execution_limits:
    - type: "max_loop_count"
      value: 2
  -termination_criteria:
    - type: "regex_match"
      agent: "reviewer_agent"
      pattern: "^APPROVED"      # Stops the loop when the reviewer approves
    - type: "human_intervention"
      trigger_on_loop_count: 2
  -guardrail_instructions:
    - Follow coding standards.
    - Reuse existing services.
    - Implement proper validation.
    - Handle exceptions appropriately.
    - Log meaningful events.
    - Maintain backward compatibility.
    - Do not Duplicate existing functionality.
    - Do not Hardcode business values.
    - Do not Expose internal implementation details.
    - Do not Ignore error handling.
    - Do not Use temporary or placeholder logic.
    - Follow UI design specifications.
    - Reuse existing components.
    - Follow accessibility standards.
    - Implement proper error handling.
    - Support responsive design where required.
    - Do not Create unnecessary screens.
    - Do not Hardcode backend URLs.
    - Do not Duplicate UI components.
    - Do not Introduce unsupported libraries.
    - Do not Embed business logic in UI components.
    - Generate unit tests for all new code.
    - Cover positive scenarios.
    - Cover negative scenarios.
    - Cover validation rules.
    - Cover edge cases.
    - Coverage should be above 80% for all unit test
    - Do not Generate placeholder tests.
    - Do not Create tests without assertions.
    - Do not Mock the system under test.
    - Do not Ignore error paths.

---