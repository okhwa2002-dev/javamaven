# 데이터베이스 규칙 (PostgreSQL)

## 접속
- dev: `localhost:5432/devdb` (application.yml에 명시)
- prod: `${DB_USERNAME}/${DB_PASSWORD}` 환경변수 주입. yml/코드에 자격증명 하드코딩 금지.

## 명명
- 테이블/컬럼: `snake_case` (예: `user_account`, `created_at`)
- PK: `id` (BIGSERIAL 권장)
- FK: `<대상테이블>_id` (예: `user_id`)
- 인덱스: `idx_<테이블>_<컬럼>`
- 유니크 제약: `uk_<테이블>_<컬럼>`

## 컬럼 표준
- 생성/수정 시각: `created_at TIMESTAMP`, `updated_at TIMESTAMP`
- 논리 삭제: `deleted_at TIMESTAMP NULL` (사용 시)
- Boolean: `is_<상태>` 대신 상태를 나타내는 명확한 컬럼명 사용

## 쿼리 성능3
- `EXPLAIN ANALYZE`로 실행계획 확인 후 반영.
- 대량 INSERT/UPDATE는 배치 처리.
- 트랜잭션 범위는 최소화.

## 스키마 관리 (spring.sql.init)
- 위치: `src/main/resources/schema.sql` (단일 파일)
- 실행 주체: Spring Boot 의 `spring.sql.init`
  - dev / test 프로필: `mode: always` — 기동 시 자동 실행 (Testcontainer IT 도 동일하게 적용됨)
  - prod 프로필: `mode: never` — 자동 실행 차단. DBA 가 `schema.sql` 을 참고해 수동 적용
- **재실행 안전**을 위해 `CREATE TABLE IF NOT EXISTS`, `CREATE INDEX IF NOT EXISTS` 사용. `COMMENT ON` 은 idempotent.
- 스키마 변경 절차
  1. `schema.sql` 을 직접 편집 (신규 테이블/컬럼 등 추가).
  2. dev DB 에는 개발자가 필요한 `ALTER` / `DROP` 을 수동으로 반영 (앱 재기동만으로는 기존 테이블이 갱신되지 않음).
  3. prod 반영은 배포 절차 문서(별도) 를 따른다.
- 파괴적 변경(컬럼 DROP / 타입 변경 / NOT NULL 추가)은 반드시 리뷰 후 반영. 자동 마이그레이션 도구가 없으므로 팀 규율로 실수를 막는다.
