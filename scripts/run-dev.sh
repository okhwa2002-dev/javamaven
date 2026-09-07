#!/bin/bash
# dev 프로파일로 앱 실행 (.env.dev 자동 로드): scripts/run-dev.sh
set -e
cd "$(dirname "$0")/.."
./mvnw spring-boot:run \
    -Dspring-boot.run.profiles=dev \
    -Dspring-boot.run.jvmArguments="-Ddotenv.filename=.env.dev"
