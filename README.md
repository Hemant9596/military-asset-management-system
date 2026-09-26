# Military Asset Management System

React/Vite client -> Spring Boot REST API -> Aiven MySQL. The backend uses Spring Security, JWT, Spring Data JPA, and Flyway; the frontend uses React Router and Axios.

## Requirements

- Java 21
- Maven 3.9 or later
- Node.js 22.12 or later and npm
- An Aiven MySQL service and database

## Configure Aiven

Use the existing Aiven MySQL database `military_asset_db`. Do not create another database or manually create application tables. The first backend startup runs the Flyway migration and validates the JPA schema. The database user needs permission to create/alter schema objects and insert/read application data.

In PowerShell, set the environment variables in the terminal used to run the backend:

```powershell
$env:DB_URL = "jdbc:mysql://<AIVEN_HOST>:<AIVEN_PORT>/military_asset_db?sslMode=REQUIRED"
$env:DB_USERNAME = "<AIVEN_USERNAME>"
$env:DB_PASSWORD = "<AIVEN_PASSWORD>"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173,https://<YOUR_FRONTEND_HOST>"
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$secretBytes = New-Object byte[] 48
$rng.GetBytes($secretBytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
$env:INITIAL_ADMIN_EMAIL = "<ADMIN_EMAIL>"
$env:INITIAL_ADMIN_PASSWORD = "<UNIQUE_PASSWORD_AT_LEAST_12_CHARACTERS>"
```

`CORS_ALLOWED_ORIGINS` is a comma-separated exact-origin allowlist; replace the example frontend host with the deployed React origin. `INITIAL_ADMIN_EMAIL` and `INITIAL_ADMIN_PASSWORD` create the first administrator only when that email does not exist. Remove those two variables after the account is created. No database or signing credentials are committed in the project. If the Aiven service requires a CA certificate rather than TLS encryption alone, configure the JDBC truststore according to the certificate supplied by Aiven.

Do not point Flyway at an existing Hibernate-created schema without first backing it up and planning a baseline/migration. This project expects a new database where V1 can run cleanly.

## Run the Backend

In a terminal with the database and JWT variables set:

```powershell
cd backend
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. Flyway applies `backend/src/main/resources/db/migration/V1__initial_schema.sql` on startup. `spring.jpa.hibernate.ddl-auto=validate` checks mapped entities against the migrated schema; Hibernate does not create or update the schema.

## Run the Frontend

In a separate terminal:

```powershell
cd frontend
npm install
$env:VITE_API_URL = "http://localhost:8080"
npm run dev
```

Vite prints the local URL, normally `http://localhost:5173`.

## Tests and Build

```powershell
cd backend
mvn test
```

```powershell
cd frontend
npm run build
```

## Initial Setup

Sign in as the configured administrator, then create a base, equipment type, asset, and base-scoped users. Record one-time opening balances for existing stock; use purchases for later acquisitions. Transfers, assignments, and expenditures require sufficient available stock. Dashboard balances and detail rows are calculated from persisted inventory and transaction records; the client has no seeded or hardcoded dashboard values.

## Access Rules

- `ADMIN`: cross-base access and user/catalog/base administration.
- `LOGISTICS_OFFICER`: stock purchases and transfers from the officer's assigned base; transfer destinations may be another base.
- `BASE_COMMANDER`: inventory, dashboard, assignments, and expenditures for the assigned base only.
- Every API route except `POST /api/auth/login` requires a valid bearer token. The server enforces roles and base scopes independently of frontend navigation.

Transfer, assignment, purchase, and expenditure inventory updates run transactionally, lock inventory rows, reject insufficient stock, and write audit records. API requests log method, path, status, and duration without logging authorization headers or request bodies.
