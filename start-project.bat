@echo off
title Military Asset Management System

echo Starting Military Asset Management System...

for /f "usebackq tokens=1,* delims==" %%A in ("backend\.env.local") do (
    set "%%A=%%B"
)

echo Starting Backend...
start "Military Asset Backend" powershell -NoExit -Command "cd '%~dp0backend'; $env:DB_URL='%DB_URL%'; $env:DB_USERNAME='%DB_USERNAME%'; $env:DB_PASSWORD='%DB_PASSWORD%'; $env:JWT_SECRET='%JWT_SECRET%'; $env:INITIAL_ADMIN_EMAIL='%INITIAL_ADMIN_EMAIL%'; $env:INITIAL_ADMIN_PASSWORD='%INITIAL_ADMIN_PASSWORD%'; $env:CORS_ALLOWED_ORIGINS='%CORS_ALLOWED_ORIGINS%'; mvn spring-boot:run"

timeout /t 8 /nobreak >nul

echo Starting Frontend...
start "Military Asset Frontend" powershell -NoExit -Command "cd '%~dp0frontend'; $env:VITE_API_URL='http://localhost:8080'; npm run dev"

timeout /t 5 /nobreak >nul

start http://localhost:5173

echo.
echo Project started.