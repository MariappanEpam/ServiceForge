# Test Plan — Feature 7: Parts Inventory Management (CRUD)

## References
- Spec: [pipeline/features/feature-7-parts-inventory-management.md](../../features/feature-7-parts-inventory-management.md)
- Architecture: [pipeline/architecture/feature-7-architecture.md](../../architecture/feature-7-architecture.md)
- Reviewed architecture: [pipeline/architecture-design/feature-7-architecture.md](../../architecture-design/feature-7-architecture.md)
- Implementation plan: [pipeline/implementation-plan/feature-7-implementation-plan.md](../../implementation-plan/feature-7-implementation-plan.md)

## Scope

### In-scope
- Backend Parts Inventory Management API (Feature 7):
  - list/search: `GET /api/parts/manage?q=&activeOnly=`
  - get by SKU: `GET /api/parts/manage/{sku}`
  - create: `POST /api/parts/manage`
  - update: `PUT /api/parts/manage/{sku}`
  - delete (soft delete): `DELETE /api/parts/manage/{sku}`
- Frontend Parts Management screen:
  - route: `/parts/manage`
  - list/search, add, edit, delete

### Out of scope
- Reservation delete-guard behavior (optional in spec; only test if implemented)

## Automated suites

### Backend integration tests (Java)
- `PartsManagementControllerTest`
  - create success → 201
  - create duplicate SKU → 409
  - update missing SKU → 404
  - delete success → 204 and not listed by default (`activeOnly=true`)
  - invalid SKU pattern → 400

### Frontend unit tests (Angular)
- `PartsManagementComponent`
  - renders list on init
  - create flow calls service with expected payload
  - delete confirmation triggers service call

### API automation (C# Playwright)
- `Feature7PartsInventoryApiTests`
  - `ListPartsManage_ShouldReturn200`
  - `CreateUpdateDeletePart_ShouldSucceed`
  - `CreateDuplicateSku_ShouldReturn409`

### UI automation (C# Playwright)
- `Feature7PartsInventoryUiTests`
  - `ManageParts_CreateEditDelete_ShouldWork`

## Execution readiness
- Backend running on `SERVICEFORGE_API_BASE_URL` (default `http://localhost:8080`)
- Frontend running on `SERVICEFORGE_UI_BASE_URL` (default `http://127.0.0.1:4200`)
- Playwright browsers installed (test-automation)

## Evidence
- TRX: `test-execution/feature-7-parts-inventory-management/*.trx`
- HTML report: `test-execution/feature-7-parts-inventory-management/reports/*.html`
