@echo off
REM prod 프로파일로 JAR 실행 (.env.prod 자동 로드): scripts\run-prod.bat
setlocal
cd /d "%~dp0.."

if not exist .env.prod (
    echo ERROR: .env.prod not found. Copy .env.prod.example to .env.prod and fill in values.
    exit /b 1
)

set JAR=
for %%f in (target\*.jar) do set JAR=%%f

if "%JAR%"=="" (
    echo JAR not found. Building...
    call mvnw.cmd clean package -DskipTests
    for %%f in (target\*.jar) do set JAR=%%f
)

echo Running: %JAR% (profile=prod, dotenv=.env.prod)
java -Ddotenv.filename=.env.prod -jar "%JAR%" --spring.profiles.active=prod
