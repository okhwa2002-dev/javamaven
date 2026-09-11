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

## 쿼리 성능
- `EXPLAIN ANALYZE`로 실행계획 확인 후 반영.
- 대량 INSERT/UPDATE는 배치 처리.
- 트랜잭션 범위는 최소화.

## 마이그레이션 (Flyway)
- 위치: `src/main/resources/db/migration/V<번호>__<설명>.sql` (더블 언더스코어 필수)
- 애플리케이션 기동 시 순차 적용됨. 수동 실행 불필요.
- **이미 적용된 파일은 수정 금지** — 체크섬 위반으로 기동 실패. 정정은 다음 번호로 `V<n+1>__fix_xxx.sql` 추가.
- 컬럼 DROP / 타입 변경 / NOT NULL 추가 등은 배포 순서를 검토한 뒤 반영.
