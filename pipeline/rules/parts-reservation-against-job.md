# Parts Reservation Rules

## Domain
Parts Reservation

## Purpose
Manage the reservation of inventory parts exclusively against a Job.

---

## Relationship Rules

### PR-001: Job is Mandatory
A Parts Reservation must always be associated with a valid Job.

**Rule**
- Reservation cannot be created without a JobId.
- JobId is the primary relational key linking reservations to jobs.

---

### PR-002: No Standalone Reservations
Parts Reservations shall not exist independently.

**Rule**
- Orphan reservations are prohibited.
- Every reservation record must reference an existing Job.

---

### PR-003: JobId Immutability
Once a reservation is created, its JobId cannot be modified.

**Reason**
- Prevents reservation movement between jobs.
- Preserves inventory audit trail.

---

### PR-004: Multiple Reservations per Job
A Job may contain zero, one, or many Parts Reservations.

**Cardinality**
- Job (1) → Parts Reservations (0..*)

---

### PR-005: Reservation Scope
Reserved quantities are owned by the associated Job only.

**Rule**
- Reserved inventory cannot be consumed by another Job.
- Cross-job reservation sharing is not permitted.

---

### PR-006: Job Existence Validation
Before creating a reservation:

**Validation**
- JobId must exist in the Job master.
- Job status must allow parts reservation.

**Failure**
- Reservation request shall be rejected.

---

### PR-007: Reservation Quantity Validation
Reserved quantity must satisfy:

```text
Requested Quantity > 0
Requested Quantity <= Available Inventory

Parts Reservations can only be created against Active Jobs.

Not Allowed:
- Non-existent Jobs
- Cancelled Jobs
- Closed Jobs
- Completed Jobs
- Archived Jobs
- Past Jobs

Allowed:
- Active Jobs that permit parts reservation