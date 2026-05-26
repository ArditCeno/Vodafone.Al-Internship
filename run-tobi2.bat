@echo off
set JAVA_HOME=C:\Users\User\.jdks\ms-17.0.19
set MAVEN_HOME=%USERPROFILE%\.m2\apache-maven-3.9.6

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo First run: downloading Maven 3.9.6...
    if not exist "%USERPROFILE%\.m2" mkdir "%USERPROFILE%\.m2"
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\maven.zip'"
    echo Extracting...
    powershell -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%\.m2' -Force"
    echo Done.
)

echo Deleting old data...
if exist "data" rmdir /s /q data
"%MAVEN_HOME%\bin\mvn.cmd" clean spring-boot:run -P tobi2 -Dspring-boot.run.profiles=tobi2
pause
