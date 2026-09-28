After Developer Complete hook

This hook runs after the Developer Agent completes and performs local developer orchestration:

- Kills any processes listening on ports 8080 and 4200 (used by backend/frontend dev servers).
- Starts backend with `mvn spring-boot:run` in `main/backend` (uses mvn or mvnw.cmd if present).
- Starts frontend with `npm start` in `main/frontend`.

Files:
- after-developer-complete.json — hook metadata
- after-developer-complete.ps1 — script executed by the hook

Testing:
1. From the workspace root run the script directly:
   powershell -ExecutionPolicy Bypass -File .github\hooks\after-developer-complete.ps1
2. Verify backend logs appear and frontend serves on http://localhost:4200

Notes:
- This is for local development only. The hook sets "allowOnCi": false in metadata to avoid running in CI.
- The script force-stops any PID bound to the ports — ensure this is acceptable in your environment.
