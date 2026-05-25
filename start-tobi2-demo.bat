@echo off
title TOBi2 Demo - Presentation Mode
echo ============================================
echo         TOBi2 Demo - Duke u nisur...
echo ============================================
echo.

:: Kill existing processes on ports 8081 and 5173
echo [1/4] Duke mbyllur servera te vjeter...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081') do taskkill /F /PID %%a >nul 2>&1
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173') do taskkill /F /PID %%a >nul 2>&1
timeout /t 2 /nobreak >nul

:: Get local IP
for /f "tokens=3 delims=: " %%i in ('netsh interface ip show address "Wi-Fi" ^| findstr "IP Address"') do set LOCAL_IP=%%i
if "%LOCAL_IP%"=="" for /f "tokens=3 delims=: " %%i in ('netsh interface ip show address "WiFi" ^| findstr "IP Address"') do set LOCAL_IP=%%i

:: Start Backend (Spring Boot)
echo [2/4] Duke nisur Backend-in (Spring Boot)...
start "TOBi2-Backend" /min cmd /c "mvn spring-boot:run -P tobi2 -Dspring-boot.run.profiles=tobi2 > backend.log 2>&1"

echo Duke pritur per Backend-in...
:wait_loop
timeout /t 2 /nobreak >nul
curl -s http://localhost:8081/api/tobi2/session/start?userId=test >nul 2>&1
if errorlevel 1 goto wait_loop
echo [OK] Backend eshte gati!

:: Start Frontend (Vite)
echo [3/4] Duke nisur Frontend-in (Vite React)...
start "TOBi2-Frontend" /min cmd /c "cd /d src\Tobi2\frontend && npm run dev > frontend.log 2>&1"
timeout /t 3 /nobreak >nul

:: Show URLs
echo.
echo ============================================
echo   TOBi2 eshte gati per prezentim!
echo ============================================
echo.
echo  Lokalisht:        http://localhost:5173
echo  Ne rrjet (WiFi):  http://%LOCAL_IP%:5173
echo  Backend API:      http://localhost:8081/api
echo.
echo  Hape link-un e WiFi nga telefoni per demo!
echo.
echo  Login:
echo    User:      Ardit Ceno / password123
echo    Employee:  ardit.ceno@vodafone / vodafone123
echo.
echo ============================================
pause
