@echo off
setlocal
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
    echo Error: 'java' not found on PATH. Please install JDK 21+
    exit /b 1
)
where mvn >nul 2>nul
if errorlevel 1 (
    echo Error: 'mvn' (Maven) not found on PATH. Please install Maven 3.9+
    exit /b 1
)

echo Building...
call mvn -q clean package
if errorlevel 1 exit /b 1

java -jar target\declension-trainer.jar
