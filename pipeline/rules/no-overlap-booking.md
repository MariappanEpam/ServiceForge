# Technician Scheduling Rules

## Purpose
Ensure technicians are scheduled efficiently without conflicts and prevent overlapping bookings.

## Rule 1: No Overlapping Bookings
A technician must never be assigned to two jobs that overlap in time.

### Validation
For each booking request:

1. Retrieve all existing confirmed and tentative bookings for the technician.
2. Calculate the occupied time window for each booking:
   - Start Time = Scheduled Job Start Time
   - End Time = Scheduled Job End Time

3. Reject the booking if:

   Requested Start Time < Existing End Time
   AND
   Requested End Time > Existing Start Time

4. Overlap is not allowed even for a partial duration.

### Examples

✅ Allowed

Existing Job:
- 09:00 - 10:00

New Job:
- 10:00 - 11:00

Reason:
The new job starts exactly when the previous job ends.

---

❌ Not Allowed

Existing Job:
- 09:00 - 10:00

New Job:
- 09:30 - 10:30

Reason:
Time windows overlap from 09:30 to 10:00.

---

❌ Not Allowed

Existing Job:
- 09:00 - 11:00

New Job:
- 10:00 - 10:30

Reason:
The new job falls within an existing booking.

## Rule 2: Travel Time Protection
Travel time must be reserved between jobs.

### Validation

Occupied Window =
Job Start Time
to
(Job End Time + Travel Buffer)

Default Travel Buffer = 45 minutes

The next booking can only start after the occupied window ends.

### Example

Existing Job:
- Work: 09:00 - 10:00
- Travel Buffer: 45 minutes

Technician Occupied Until:
- 10:45

✅ Allowed:
- New Job Start = 10:45 or later

❌ Not Allowed:
- New Job Start = 10:30

## Rule 3: Resource Exclusivity
A technician can have only one active assignment at any point in time.

The scheduling engine must verify:
- Confirmed bookings
- Tentative bookings
- In-progress jobs
- Reserved time slots

before allocating work.

## Rule 4: Booking Decision
If any overlap or travel-time conflict exists:

Decision = REJECT

Response:
"Technician is unavailable during the requested time slot. Select another technician or choose a different time."

## Rule 5: Scheduling Priority
When multiple technicians are available:

1. Prefer technicians with required skills.
2. Prefer technicians closest to the job location.
3. Prefer technicians with the lowest utilization.
4. Apply conflict validation before final assignment.

## Non-Negotiable Constraint

Under no circumstances shall the system create, update, or approve a booking that overlaps with:
- Another scheduled job
- Travel buffer time
- In-progress work
- Reserved technician availability

This rule takes precedence over all optimization or scheduling recommendations.