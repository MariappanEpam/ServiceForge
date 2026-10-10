# Review Agent Checklist

## Requirement Alignment

- [ ] All requirements are implemented.
- [ ] All acceptance criteria are satisfied.
- [ ] No requirements are missing.
- [ ] No unnecessary scope has been added.
- [ ] Assumptions are explicitly documented.
- [ ] Business rules are correctly implemented.
- [ ] Edge cases identified in requirements are handled.

---

## Traceability

- [ ] Every implementation maps to a requirement.
- [ ] Every requirement has corresponding implementation.
- [ ] Every requirement has corresponding test coverage.
- [ ] Non-functional requirements are addressed.
- [ ] Design decisions align with business objectives.

---

## Architecture & Design

- [ ] Solution follows established architecture guidelines.
- [ ] Separation of concerns is maintained.
- [ ] Components have clear responsibilities.
- [ ] Dependencies are appropriate and justified.
- [ ] Design is scalable.
- [ ] Design is maintainable.
- [ ] Error handling strategy is defined.
- [ ] Reusable components are leveraged where applicable.
- [ ] No architectural anti-patterns introduced.

---

## Security

- [ ] Input validation is implemented.
- [ ] Authorization controls are present.
- [ ] Authentication is properly enforced.
- [ ] Sensitive data is protected.
- [ ] Secrets are not hardcoded.
- [ ] APIs are secured.
- [ ] OWASP risks are considered.
- [ ] Security exceptions are documented.
- [ ] Logs do not expose confidential information.

---

## Code Quality

- [ ] Code follows project standards.
- [ ] Naming conventions are consistent.
- [ ] Functions have a single responsibility.
- [ ] Logic duplication is minimized.
- [ ] Error handling is appropriate.
- [ ] Logging is meaningful and actionable.
- [ ] Dead code is removed.
- [ ] Code complexity is reasonable.
- [ ] No obvious bugs or defects detected.
- [ ] Configuration values are externalized.

---

## Maintainability

- [ ] Code is easy to understand.
- [ ] Appropriate comments are included where needed.
- [ ] Documentation is updated.
- [ ] Technical debt is identified.
- [ ] Future extensibility is considered.
- [ ] Modules are loosely coupled.
- [ ] Changes are localized and modular.

---

## API Review

- [ ] API contracts are respected.
- [ ] Request validation exists.
- [ ] Response format is consistent.
- [ ] Error handling follows standards.
- [ ] API documentation is updated.
- [ ] Backward compatibility is maintained.
- [ ] Versioning guidelines are followed.

---

## Database Review

- [ ] Schema changes are justified.
- [ ] Migrations are safe and reversible.
- [ ] Queries are optimized.
- [ ] Required indexes exist.
- [ ] Transactions are properly handled.
- [ ] Data integrity is preserved.
- [ ] No unnecessary database coupling introduced.

---

## Frontend Review

- [ ] UI aligns with requirements.
- [ ] Validation is implemented.
- [ ] Error messages are user friendly.
- [ ] Loading states are handled.
- [ ] Empty states are handled.
- [ ] Accessibility requirements are met.
- [ ] Responsive behavior is verified.
- [ ] Cross-browser considerations are addressed.

---

## Testing

### Unit Testing

- [ ] New functionality is covered.
- [ ] Business logic is tested.
- [ ] Negative scenarios are tested.
- [ ] Edge cases are tested.

### Integration Testing

- [ ] Service interactions are covered.
- [ ] Database interactions are verified.
- [ ] External dependencies are validated.

### End-to-End Testing

- [ ] Critical workflows are covered.
- [ ] Regression risks are addressed.

### Overall Testing

- [ ] Test suite passes successfully.
- [ ] No flaky tests introduced.
- [ ] Coverage meets project threshold.
- [ ] Test evidence is available.

---

## Performance

- [ ] Performance impact is assessed.
- [ ] Expensive operations are optimized.
- [ ] Caching opportunities considered.
- [ ] Network usage is optimized.
- [ ] Large data volumes are handled appropriately.
- [ ] No obvious bottlenecks identified.

---

## Observability

- [ ] Logging is sufficient.
- [ ] Metrics are captured.
- [ ] Monitoring requirements are addressed.
- [ ] Failure scenarios are observable.
- [ ] Diagnostics support troubleshooting.

---

## Documentation

- [ ] Architecture documentation updated.
- [ ] API 