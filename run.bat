@echo off
setlocal enabledelayedexpansion
echo Starting TOBi Backend...
cd /d "%~dp0"

set JAVA_EXE=C:\Users\User\.jdks\ms-17.0.19\bin\java.exe
set APP_CLASS=com.vodafone.tobi.TOBiApplication

set CLASSPATH=target/classes
set CP_FILE=cp.txt
for /f "usebackq delims=" %%a in ("%CP_FILE%") do set CLASSPATH=!CLASSPATH!;%%a

echo Starting...
"%JAVA_EXE%" -cp "!CLASSPATH!" %APP_CLASS%
pause
