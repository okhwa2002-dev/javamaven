@echo off
REM dev 프로파일로 앱 실행 (.env.dev 자동 로드): scripts\run-dev.bat
setlocal
cd /d "%~dp0.."
call mvnw.cmd spring-boot:run ^
    -Dspring-boot.run.profiles=dev ^
    -Dspring-boot.run.jvmArguments="-Ddotenv.filename=.env.dev"
