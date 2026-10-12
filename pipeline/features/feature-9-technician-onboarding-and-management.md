# Feature 9 — Technician onboarding and management

| Field | Value |
|---|---|
| **Status** | Draft (BA) |
| **One-line intent** | Provide a master page to add and manage technicians, including age, vehicle details, area served, skills, and activation status. |
| **Depends on** | Feature 1 (Technician list + technician concept exists) |
| **In scope** | Create/edit/deactivate technicians; manage technician details (age, vehicle, area served, skills); list/search/filter technicians; basic validation and error handling |
| **Out of scope** | Authentication/authorization; role-based access; technician availability scheduling changes; job assignment workflows; file uploads (photos/docs); external integrations; advanced reporting |
| **Definition of Done** | Admin user can create a technician, edit details, deactivate/reactivate, and view/search the technician list; API + UI validations and error states are covered; seeded/mock data remains compatible (migration-safe). |

## Assumptions (explicit)

1. **Persona**: A back-office dispatcher/admin uses this screen. No auth is implemented in this repo; therefore access control is out of scope.
2. **Technician identity**: A technician has a stable `id` (string or numeric) used by existing endpoints (e.g., Feature 1). This feature must not break existing `GET /api/technicians` consumers.
3. **Age vs DOB**: The intent asks for **Age**. Age changes over time; for simplicity we store `age` as an integer at time of entry (not derived from DOB). If DOB is preferred, it must be a separate feature.
4. **Vehicle details**: Captured as simple fields (e.g., type, make/model, plate). No VIN validation or fleet integration.
5. **Area served**: Captured as free-text plus optional structured fields (city/zip). No GIS, maps, or geofencing.
6. **Skills**: Managed as a list of strings (or references to a small catalog). This spec supports both approaches; architecture will pick one.

## User stories

### US-9.1 Technician master list
As an admin, I can view a list of technicians with key details and status so I can manage the workforce.

### US-9.2 Create technician
As an admin, I can add a new technician with required details so they can be scheduled and assigned work.

### US-9.3 Edit technician
As an admin, I can update technician details (including skills, vehicle, area served) so records stay accurate.

### US-9.4 Deactivate/reactivate technician
As an admin, I can deactivate a technician (and later reactivate) so inactive technicians are not used operationally.

## Functional requirements

### FR-9.1 Technician list
- Display technicians in a table/grid.
- Columns (minimum): Name, Age, Area served, Vehicle summary, Skills summary, Status (Active/Inactive).
- Provide search by name and filter by status.

### FR-9.2 Technician create/edit form
- Form fields (minimum):
  - Name (required)
  - Age (required, integer)
  - Area served (required)
  - Vehicle details (optional but supported)
  - Skills (optional but supported)
  - Status (Active by default on create)
- Skills entry supports add/remove.

### FR-9.3 Deactivate/reactivate
- Deactivate action available from list and detail/edit view.
- Reactivate action available for inactive technicians.
- Deactivation is **soft** (record remains, status changes).

### FR-9.4 Validation and errors
- Client-side validation mirrors server-side validation.
- Server returns consistent `ApiError` for 400/404/409.

#### Age bounds (explicit)
- Allowed range: 16..80 (inclusive).
- If provided age is outside this range, return `400 Bad Request` with `ApiError`.

## Data requirements

### Technician fields (new/confirmed)
- `id` (existing)
- `name` (existing or new)
- `active` (new or existing; boolean)
- `age` (new; integer)
- `vehicleDetails` (new; object)
  - `type` (string, optional)
  - `make` (string, optional)
  - `model` (string, optional)
  - `licensePlate` (string, optional)
- `areaServed` (new; string)
- `skills` (new; list of strings OR list of skill IDs)

### Migration-safety note (must be addressed)
This feature likely changes the shape of the existing `Technician` model and the seeded/mock data. Follow migration-safety guidance:
- Additive-first changes only.
- Update all seeds so existing UI/API flows do not break.
- Grep/update all DTOs and frontend models that consume technicians.

## API requirements (logical)

Exact endpoint shapes will be finalized in architecture, but must support:

- List technicians (existing): `GET /api/technicians`
- Create technician: `POST /api/technicians`
- Get technician by id: `GET /api/technicians/{id}`
- Update technician: `PUT /api/technicians/{id}` (or `PATCH` if repo convention prefers)
- Deactivate technician: `POST /api/technicians/{id}/deactivate`
- Reactivate technician: `POST /api/technicians/{id}/reactivate`

### Error semantics
- 400: validation errors (missing name, invalid age, etc.)
- 404: technician not found
- 409: conflict (e.g., duplicate unique key if introduced; otherwise avoid inventing)

## Acceptance Criteria (AC)

### AC-9.1 List and search
- Given technicians exist, when I open the Technician Management page, then I see a list of technicians with status.
- When I search by name, then the list filters to matching technicians.
- When I filter by status (Active/Inactive), then only technicians with that status are shown.

### AC-9.2 Create technician (happy path)
- Given I am on the Technician Management page, when I choose Add Technician and enter valid values for Name, Age, Area served, and optionally Vehicle details and Skills, then the technician is created and appears in the list as Active.

### AC-9.3 Create technician (validation)
- When I submit the create form with missing Name, then I see a validation message and the API returns 400.
- When I submit the create form with Age not an integer or Age outside allowed bounds, then I see a validation message and the API returns 400.
- When I submit the create form with missing Area served, then I see a validation message and the API returns 400.

### AC-9.4 Edit technician (happy path)
- Given an existing technician, when I edit Age, Vehicle details, Area served, and Skills and save, then the changes persist and are visible on refresh.

### AC-9.5 Edit technician (not found)
- When I attempt to edit a technician that does not exist, then the UI shows a not-found state and the API returns 404 with `ApiError`.

### AC-9.6 Deactivate technician
- Given an active technician, when I deactivate them, then their status becomes Inactive and they remain visible in the list when filtering for Inactive.

### AC-9.7 Reactivate technician
- Given an inactive technician, when I reactivate them, then their status becomes Active and they appear in the Active filter.

### AC-9.8 Skills management
- When I add one or more skills to a technician and save, then the skills are displayed in the list/detail view.
- When I remove a skill and save, then it no longer appears.

### AC-9.9 Vehicle details management
- When I enter vehicle details and save, then the vehicle summary is displayed (e.g., "Van • Toyota Hiace • ABC-123").

### AC-9.10 Area served
- When I set Area served and save, then it is displayed in the list/detail view.

## Non-functional requirements (NFRs)
- Consistent REST error shape (`ApiError`) and status codes.
- No breaking changes to existing Feature 1 flows.
- Basic accessibility: form labels, keyboard navigation for add/remove skills.

## Test notes (for later Tester stage)
- Backend: controller/service tests for create/update/deactivate/reactivate; validation tests.
- Frontend: component tests for form validation and list filtering.
- Regression: Feature 1 technician list and job booking still work with updated technician model.
