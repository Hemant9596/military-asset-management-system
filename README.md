# Military Asset Management System

React/Vite client -> Spring Boot REST API -> MySQL. The backend uses Spring Security, JWT, Spring Data JPA, and Flyway; the frontend uses React Router and Axios.

## Requirements

- Java 21
- Maven 3.9 or later
- Node.js 22.12 or later and npm
- A MySQL 8.x database

## Configure MySQL

Create a MySQL database named `military_asset_db`.

The application uses Flyway to create the required application tables automatically on startup. Do not manually create the application tables.

In PowerShell, set the environment variables in the terminal used to run the backend:

```powershell
$env:DB_URL = "jdbc:mysql://<MYSQL_HOST>:<MYSQL_PORT>/military_asset_db?sslMode=REQUIRED"
$env:DB_USERNAME = "<MYSQL_USERNAME>"
$env:DB_PASSWORD = "<MYSQL_PASSWORD>"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"

$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$secretBytes = New-Object byte[] 48
$rng.GetBytes($secretBytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)

$env:INITIAL_ADMIN_EMAIL = "<ADMIN_EMAIL>"
$env:INITIAL_ADMIN_PASSWORD = "<UNIQUE_PASSWORD_AT_LEAST_12_CHARACTERS>"
