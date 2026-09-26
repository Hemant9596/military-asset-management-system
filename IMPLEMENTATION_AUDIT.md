# Repository Audit Checklist

Statuses below describe the implementation before this work and the current source state. “Partially implemented” includes functionality that exists but lacks a complete UI, lifecycle, or verification gate.

|   # | Requirement              | Before changes        | Current status        | Code evidence / note                                                                                                      |
| --: | ------------------------ | --------------------- | --------------------- | ------------------------------------------------------------------------------------------------------------------------- |
|   1 | Authentication           | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Login, current-user endpoint, BCrypt, and enabled-account checks.                                                         |
|   2 | JWT                      | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Bearer filter, expiration checks, external strong secret, invalid-token 401.                                              |
|   3 | RBAC                     | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Method-level role rules and route guards; server rules remain authoritative.                                              |
|   4 | Base-level authorization | BROKEN                | IMPLEMENTED           | Requested base is resolved against the authenticated user's assignment; transaction detail/list paths are scoped.         |
|   5 | Users                    | PARTIALLY IMPLEMENTED | PARTIALLY IMPLEMENTED | Admin create/list and base-scoped assignment recipients; no edit/disable UI.                                              |
|   6 | Bases                    | PARTIALLY IMPLEMENTED | PARTIALLY IMPLEMENTED | Admin API CRUD, commander-scoped reads, and create/list UI; no edit/delete UI.                                            |
|   7 | Equipment types          | PARTIALLY IMPLEMENTED | PARTIALLY IMPLEMENTED | API and admin UI create/read; no update/delete lifecycle.                                                                 |
|   8 | Assets                   | PARTIALLY IMPLEMENTED | PARTIALLY IMPLEMENTED | API CRUD and UI create/list; no edit UI.                                                                                  |
|   9 | Inventory                | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Scoped reads, transactional stock mutations, pessimistic row locks, and nonnegative database constraints.                 |
|  10 | Purchases                | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Persisted purchase flow updates inventory and audit log; API-backed entry/history UI.                                     |
|  11 | Transfers                | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Transactional source/destination inventory updates, stock check, scoped history, and UI.                                  |
|  12 | Assignments              | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Base-scoped creation/list, recipient validation, inventory update, audit, and UI.                                         |
|  13 | Expenditures             | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Stock validation/update, audit, and API-backed entry/history UI.                                                          |
|  14 | Dashboard                | BROKEN                | IMPLEMENTED           | Persisted movement calculations use audited one-time opening stock and reconcile opening/closing for the selected period. |
|  15 | Filters                  | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Date, base, and equipment type filters are on the dashboard; base scope is enforced server-side.                          |
|  16 | Net Movement details     | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Detail endpoint returns purchase, transfer, and expenditure journal rows; dashboard opens a detail dialog.                |
|  17 | Audit logging            | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Login and stock/user transactions are audited; an API filter logs request method/path/status/duration.                    |
|  18 | Database migrations      | MISSING               | PARTIALLY IMPLEMENTED | Flyway V1 schema and role rows are present; migration execution/schema validation were not run here.                      |
|  19 | Aiven configuration      | BROKEN                | PARTIALLY IMPLEMENTED | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` are required; actual Aiven connectivity/TLS remains deployment configuration.      |
|  20 | Frontend pages           | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Login, dashboard, inventory, opening balance, catalog/admin screens, transaction forms, and transfer history.             |
|  21 | API integration          | PARTIALLY IMPLEMENTED | IMPLEMENTED           | Axios client sends bearer tokens and consumes live backend responses; no fake API data.                                   |
|  22 | Validation               | PARTIALLY IMPLEMENTED | PARTIALLY IMPLEMENTED | DTO validation, service stock checks, date-range validation, and relational checks; runtime validation is unverified.     |
|  23 | Error handling           | PARTIALLY IMPLEMENTED | PARTIALLY IMPLEMENTED | Structured validation, malformed request, access, conflict, and not-found responses; runtime paths are unverified.        |
|  24 | Tests                    | MISSING               | PARTIALLY IMPLEMENTED | Focused unit tests were added, but the test runner was skipped and Maven is unavailable in this environment.              |
|  25 | README                   | MISSING               | IMPLEMENTED           | Root setup, Aiven variables, run/test commands, access rules, and initialization workflow documented.                     |

## Verification Record

- Frontend production build: `npm run build` completed successfully from `frontend/`.
- Frontend lint: `npm run lint` completed without warnings.
- Frontend runtime smoke check: Vite served `/login`, and the browser showed email/password fields without prefilled demo credentials.
- Java source diagnostics: no errors reported for backend main and test sources.
- Backend tests: not run. The test-runner request was skipped; Maven and a Maven wrapper are not installed/present in this workspace.
- Flyway migration: not executed; no Aiven/MySQL connection details were available.
- Authentication, RBAC, transaction runtime behavior, and audit persistence: not exercised against a running backend/database.
