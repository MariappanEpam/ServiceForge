# Navigation and Form Separation Rules

## Purpose
Ensure each business feature is implemented in a separate form/page and users navigate between features using a centralized Site Map.

---

# Form Separation Rules

## NAV-001: One Feature Per Form

Each business feature shall be implemented in its own dedicated form.

### Rule
- A form shall contain only one primary feature.
- Features shall not be combined into a single form.
- Each form must have its own screen, route, or page.

### Examples

Valid:
- Job Creation Form
- Parts Reservation Form
- Work Scheduling Form
- Technician Assignment Form

Invalid:
- Job + Parts Reservation in same form
- Scheduling + Assignment in same form

---

## NAV-002: Independent Data Entry

Each form shall independently manage its own data capture and validation.

### Rule
- Data fields belonging to another feature shall not appear on the current form.
- Feature-specific validations shall remain isolated within the feature form.

---

## NAV-003: Dedicated Form Ownership

Each form shall have a single business responsibility.

Examples:

### Job Form
Responsible for:
- Job creation
- Job updates
- Job status management

### Parts Reservation Form
Responsible for:
- Reservation creation
- Reservation modification
- Reservation release

### Scheduling Form
Responsible for:
- Start date selection
- Duration selection
- End date calculation

---

# Site Map Rules

## NAV-004: Site Map Required

The application shall provide a Site Map menu for navigation between forms.

### Rule
- Users shall navigate through the Site Map.
- Direct navigation between unrelated forms should be avoided.

---

## NAV-005: Site Map Visibility

The Site Map shall be available from all application screens.

### Rule
- Site Map must remain accessible at all times.
- Site Map may be implemented as:
  - Left navigation panel
  - Top menu
  - Hamburger menu
  - Navigation tree

---

## NAV-006: Form Registration

Every business feature form shall be registered in the Site Map.

### Example Structure

Site Map
├── Jobs
│   ├── Create Job
│   ├── Update Job
│   └── Search Jobs
│
├── Parts
│   ├── Parts Reservation
│   ├── Parts Issue
│   └── Inventory Lookup
│
├── Scheduling
│   ├── Work Scheduling
│   └── Calendar View
│
└── Administration
    ├── Users
    └── Configuration

---

## NAV-007: Context Preservation

When navigating from one form to another:

### Rule
- Current JobId shall be preserved and passed to related forms.
- JobId is the primary relational/context key.

Example:

Job Form
    ↓
Parts Reservation Form
    ↓
Scheduling Form

JobId remains available across forms.

---

## NAV-008: Deep Linking

The Site Map shall support direct access to any registered form.

### Rule
- Users may open a form directly from the Site Map.
- Appropriate permissions must be validated before access.

---

## NAV-009: Feature Independence

Changes to one feature form shall not require modification of unrelated forms.

### Benefits
- Easier maintenance
- Better scalability
- Independent deployments
- Reduced regression risks

---

# User Experience Requirements

1. One business feature per form.
2. No multi-feature data entry screens.
3. Site Map available on every page.
4. All forms registered within the Site Map.
5. JobId used as the context key across related forms.
6. Navigation between forms performed through Site Map.
7. Forms remain loosely coupled and independently maintainable.

---

# Example Navigation Flow

Home
 └── Site Map
      ├── Job Management
      │    └── Job Details (JobId = J1001)
      │
      ├── Parts Reservation
      │    └── Uses JobId = J1001
      │
      ├── Work Scheduling
      │    └── Uses JobId = J1001
      │
      └── Technician Assignment
           └── Uses JobId = J1001

Rule: JobId is the shared contextual identifier used across all related feature forms.