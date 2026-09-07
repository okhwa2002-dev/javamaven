#!/bin/bash
# prod 프로파일로 JAR 실행 (.env.prod 자동 로드): scripts/run-prod.sh
set -e
cd "$(dirname "$0")/.."

if [ ! -f .env.prod ]; then
    echo "ERROR: .env.prod not found. Copy .env.prod.example to .env.prod and fill in values."
    exit 1
fi

JAR=$(ls target/*.jar 2>/dev/null | grep -v '\.original$' | head -1)
if [ -z "$JAR" ]; then
    echo "JAR not found. Building..."
    ./mvnw clean package -DskipTests
    JAR=$(ls target/*.jar | grep -v '\.original$' | head -1)
fi

echo "Running: $JAR (profile=prod, dotenv=.env.prod)"
java -Ddotenv.filename=.env.prod \
     -jar "$JAR" \
     --spring.profiles.active=prod
