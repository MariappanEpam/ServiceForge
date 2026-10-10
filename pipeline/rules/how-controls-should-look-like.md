# Date and Time Selection Rules

## Start Date Control

### Rule DT-001: Year Dropdown
- Display Year as a dropdown list.
- Preselect the current calendar year.
- Allow users to select a different year if needed.

Example:
Current Date: 08-Oct-2026
Default Year: 2026

---

### Rule DT-002: Month Dropdown
- Display Month as a dropdown list.
- Preselect the current month.
- Month values shall be January through December.

Example:
Current Date: 08-Oct-2026
Default Month: October

---

### Rule DT-003: Day Selection
- Display the current day preselected.
- When Year or Month changes, update available day values accordingly.
- Validate against the selected month's valid days.

Example:
Current Date: 08-Oct-2026
Default Day: 08

---

### Rule DT-004: Default Start Date
On form load:

Start Date =
    Current Year +
    Current Month +
    Current Day

Example:
08-Oct-2026

---

## End Date / Duration Control

### Rule DT-005: Replace End Time Selection
Instead of displaying an End Date-Time field:

- Display "Hours Required" input.
- User enters the number of hours required to complete the work.

Example:
Hours Required: 4

---

### Rule DT-006: Auto Calculate End Date-Time
System calculates End Date-Time based on:

End Date-Time =
    Start Date-Time +
    Hours Required

Example:

Start:
08-Oct-2026 09:00 AM

Hours Required:
4

Calculated End:
08-Oct-2026 01:00 PM

---

### Rule DT-007: Hours Validation

- Hours Required must be greater than 0.
- Maximum allowed value shall be configurable.
- Decimal values may be allowed (e.g., 1.5 hours) if business permits.

Valid:
- 1
- 2
- 4.5

Invalid:
- 0
- Negative values

---

## User Experience Requirements

1. Year displayed as dropdown with current year preselected.
2. Month displayed as dropdown with current month preselected.
3. Day automatically preselected to current day.
4. End Date-Time field is not manually selected by the user.
5. User provides only Hours Required.
6. System automatically calculates and displays End Date-Time.
7. Calculated End Date-Time updates whenever Start Date or Hours Required changes.