# CodeGraph-assisted entrypoints (legacy root)

## Scope
- Scope slug: `legacy-root`
- Indexed folders: `backend/`, `frontend/`, `test-automation/`

## Status
Status: partial

## What this is
This note captures **candidate entrypoints** and **navigation starting points** using CodeGraph queries.

IMPORTANT:
- CodeGraph output is a navigation aid only.
- Any factual claims about behavior must be confirmed with repo file:line evidence.

## Candidate entrypoints (NEEDS-EVIDENCE)
- Backend application bootstrap (Spring Boot main class):
	- `backend/src/main/java/com/serviceforge/ServiceForgeApplication.java` (Spring Boot `main()`)
- Backend REST controllers (Spring MVC):
	- `backend/src/main/java/com/serviceforge/controller/JobController.java` (`@RestController`, `@RequestMapping("/api/jobs")`)
	- `backend/src/main/java/com/serviceforge/controller/JobPartsReservationController.java`
	- `backend/src/main/java/com/serviceforge/controller/PartsCatalogController.java`
	- `backend/src/main/java/com/serviceforge/controller/PartsManagementController.java`
	- `backend/src/main/java/com/serviceforge/controller/TechnicianController.java`
- Frontend bootstrap (Angular main):
	- `frontend/src/main.ts` (calls `bootstrapApplication(AppComponent, appConfig)`)
- Frontend routes/components:
	- `frontend/src/app/app.routes.ts` (defines `routes: Routes = [...]`)
- Test automation entrypoints (Playwright/.NET):
	- `test-automation/src/ServiceForge.Tests.Api/ServiceForge.Tests.Api.csproj` (test project)
	- `test-automation/src/ServiceForge.Tests.Api/TestConfig.cs` (base URLs + headless toggle)
	- `test-automation/src/ServiceForge.Tests.Api/UiTestBase.cs` (Playwright + NUnit base class)
	- `test-automation/src/ServiceForge.Tests.Api/Feature2PartsReservationUiTests.cs` (example UI test entrypoint)

## Evidence
Backend bootstrap:
- [backend/src/main/java/com/serviceforge/ServiceForgeApplication.java](backend/src/main/java/com/serviceforge/ServiceForgeApplication.java#L1-L10)

Backend controllers:
- [backend/src/main/java/com/serviceforge/controller/JobController.java](backend/src/main/java/com/serviceforge/controller/JobController.java#L1-L45)

Frontend bootstrap:
- [frontend/src/main.ts](frontend/src/main.ts#L1-L6)

Frontend routes:
- [frontend/src/app/app.routes.ts](frontend/src/app/app.routes.ts#L1-L12)

Test automation:
- [test-automation/src/ServiceForge.Tests.Api/ServiceForge.Tests.Api.csproj](test-automation/src/ServiceForge.Tests.Api/ServiceForge.Tests.Api.csproj#L1-L27)
- [test-automation/src/ServiceForge.Tests.Api/TestConfig.cs](test-automation/src/ServiceForge.Tests.Api/TestConfig.cs#L1-L27)
- [test-automation/src/ServiceForge.Tests.Api/UiTestBase.cs](test-automation/src/ServiceForge.Tests.Api/UiTestBase.cs#L1-L92)
- [test-automation/src/ServiceForge.Tests.Api/Feature2PartsReservationUiTests.cs](test-automation/src/ServiceForge.Tests.Api/Feature2PartsReservationUiTests.cs#L1-L86)

NEEDS-EVIDENCE:
None
