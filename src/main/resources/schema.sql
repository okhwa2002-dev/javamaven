-- 신규 설치용 DDL
CREATE TABLE IF NOT EXISTS users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    login_id   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 기존 테이블 마이그레이션용.
-- 기존 행이 있을 수 있어 NULL 허용으로 추가한다.
ALTER TABLE users ADD COLUMN IF NOT EXISTS login_id VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_login_id ON users (login_id);

-- 기존 행의 login_id/password를 모두 채운 뒤 적용한다.
-- 값이 비어 있는 행이 남아 있으면 실패하므로 먼저 채울 것.
ALTER TABLE users ALTER COLUMN login_id SET NOT NULL;
ALTER TABLE users ALTER COLUMN password SET NOT NULL;
