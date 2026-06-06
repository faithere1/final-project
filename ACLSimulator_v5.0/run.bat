@echo off
setlocal

cd /d "%~dp0"

if not exist "ACLSimulator.jar" (
    echo ACLSimulator.jar not found in this folder.
    exit /b 1
)

java -jar ACLSimulator.jar
