@echo off
REM 프로젝트 빌드 (JAR 생성): scripts\build.bat
setlocal
cd /d "%~dp0.."
call mvnw.cmd clean package %*
