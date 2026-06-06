@echo off
setlocal

cd /d "%~dp0"

if not exist "target\ACLSimulator.jar" (
    echo ACLSimulator.jar not found. Building it now...
    call mvn clean package
    if errorlevel 1 exit /b 1
)

java -jar target\ACLSimulator.jar
