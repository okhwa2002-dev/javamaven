-- 애플리케이션 스키마.
-- Spring Boot 의 spring.sql.init 이 기동 시 실행한다.
-- dev/test 프로필에서만 자동 실행(prod 는 never), 재실행 안전을 위해 IF NOT EXISTS 사용.
-- 스키마 변경 시 이 파일을 직접 수정하고, dev DB 는 개발자가 수동으로 ALTER/DROP 후 반영한다.
-- prod 반영은 배포 절차 문서(별도) 를 따른다.

-- ============================================================
-- users : 애플리케이션 사용자 계정
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    login_id   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  users            IS '애플리케이션 사용자 계정';
COMMENT ON COLUMN users.id         IS 'PK (BIGSERIAL)';
COMMENT ON COLUMN users.username   IS '표시용 사용자명. UNIQUE';
COMMENT ON COLUMN users.email      IS '이메일. UNIQUE';
COMMENT ON COLUMN users.login_id   IS '로그인 아이디. UNIQUE. 규칙: ValidationUtil.LOGIN_ID_REGEX';
COMMENT ON COLUMN users.password   IS 'BCrypt 해시. 평문 저장 금지';
COMMENT ON COLUMN users.created_at IS '생성 시각';
COMMENT ON COLUMN users.updated_at IS '최근 수정 시각';

-- ============================================================
-- refresh_tokens : JWT 리프레시 토큰 저장소
--   raw 값이 아니라 SHA-256 해시(64자 hex)만 저장한다.
--   유출 시 DB 만으로는 원본 토큰을 복원할 수 없다.
-- ============================================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64)  NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL,
    revoked_at TIMESTAMP    NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens (user_id);

COMMENT ON TABLE  refresh_tokens             IS 'JWT 리프레시 토큰. raw 값이 아니라 SHA-256 해시만 저장';
COMMENT ON COLUMN refresh_tokens.id          IS 'PK';
COMMENT ON COLUMN refresh_tokens.user_id     IS 'users.id 참조. 사용자 삭제 시 CASCADE';
COMMENT ON COLUMN refresh_tokens.token_hash  IS '리프레시 토큰의 SHA-256 hex (64자). UNIQUE';
COMMENT ON COLUMN refresh_tokens.expires_at  IS '토큰 만료 시각. 이후 재발급 불가';
COMMENT ON COLUMN refresh_tokens.revoked_at  IS '폐기 시각. NULL 이면 사용 가능. 로그아웃/회전 시 세팅';
COMMENT ON COLUMN refresh_tokens.created_at  IS '발급 시각';
