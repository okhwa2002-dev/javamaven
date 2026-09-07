#!/bin/bash
# 프로젝트 빌드 (JAR 생성): scripts/build.sh
set -e
cd "$(dirname "$0")/.."
./mvnw clean package "$@"
