# javamaven

Spring Boot 기반의 REST API 백엔드 프로젝트.

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Build | Maven |
| DB | PostgreSQL 16 |
| Persistence | MyBatis (`mybatis-spring-boot-starter` 3.0.3) |
| 인증 | JWT (`jjwt` 0.12.6) + BCrypt (`spring-security-crypto`) |
| API 문서 | springdoc-openapi 2.6.0 (Swagger UI) |
| 기타 | Lombok, spring-dotenv, Spring Actuator |

## 디렉토리 구조

```
src/main/
├── java/com/
│   ├── Main.java            # 진입점 (@SpringBootApplication + @MapperScan)
│   ├── controller/          # REST 엔드포인트 (UserController, AuthController)
│   ├── service/             # 비즈니스 로직 (@Transactional)
│   ├── mapper/              # MyBatis 매퍼 인터페이스
│   ├── domain/              # DTO/VO
│   └── cmn/                 # 공통 영역
│       ├── config/          # Spring @Configuration
│       ├── exception/       # 전역 예외 핸들러
│       ├── filter/          # 파라미터 보안 필터 (SQL keyword / XSS)
│       └── jwt/             # JWT 발급·검증, 인증 인터셉터
└── resources/
    ├── application.yml      # dev/prod 프로필 통합
    ├── mapper/              # MyBatis XML 매퍼
    └── db/migration/        # Flyway 마이그레이션 (V1__init.sql, V2__xxx.sql ...)
```

## 실행 환경

### 1. PostgreSQL 기동 (Docker)

```bash
docker compose up -d postgres
```

기본 접속 정보 (변경은 `.env` 또는 `docker-compose.yml` 참고):
- host/port: `localhost:5438`
- db / user / password: `daily` / `daily` / `dailypw`

### 2. 스키마 생성

스키마는 애플리케이션 기동 시 **Flyway** 가 `src/main/resources/db/migration/V*.sql` 을 순차 적용합니다. 새 변경분은 새 파일(`V2__xxx.sql`, `V3__xxx.sql` …)로 추가하며, 이미 배포된 파일은 수정하지 않습니다.

### 3. 애플리케이션 실행

**dev 프로필** (기본, 포트 8080)
```bash
DB_PASSWORD=dailypw mvn spring-boot:run
```

**prod 프로필** (포트 80, 모든 값 환경변수 필수)
```bash
JWT_SECRET='<32바이트 이상 랜덤값>' \
DB_URL='jdbc:postgresql://localhost:5438/daily' \
DB_USERNAME=daily \
DB_PASSWORD=dailypw \
CORS_ALLOWED_ORIGINS=http://localhost:3000 \
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 환경변수

| 이름 | dev 기본값 | prod | 설명 |
|---|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5438/daily` | 필수 | DataSource URL |
| `DB_USERNAME` | `daily` | 필수 | DB 사용자 |
| `DB_PASSWORD` | — (미주입 시 실패) | 필수 | DB 비밀번호 |
| `JWT_SECRET` | 미설정 시 임의 키 자동 생성 | **필수** (없으면 기동 중단) | HMAC-SHA256 서명 키. 32바이트 이상 |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | 필수 | CORS 허용 origin |

`JWT_SECRET` 생성·주입 상세 절차는 [docs/jwt-secret-setup.md](docs/jwt-secret-setup.md) 참고.

## 테스트

```bash
mvn test      # 단위 테스트만 (Docker 불필요)
mvn verify    # 단위 + 통합 테스트 (Testcontainers 사용, Docker 필요)
```

- 파일명 규약: `*Test.java` 는 단위(surefire), `*IT.java` 는 통합(failsafe).
- 통합 테스트는 Testcontainers 로 실제 Postgres 컨테이너를 띄운다.
- **Windows + Docker Desktop 로컬에서 실행 시**: Testcontainers 가 표준 파이프를 못 찾는 케이스가 있다. 그 경우 `~/.testcontainers.properties` 에 아래 한 줄을 추가한다.
  ```
  docker.host=npipe:////./pipe/dockerDesktopLinuxEngine
  ```
  (구체 파이프 이름은 `docker context ls` 로 확인)
- CI(GitHub Actions ubuntu runner)에는 별도 설정 없이 그대로 동작한다.

## API 개요

### 인증

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/auth/login` | login_id/password로 로그인 → 액세스+리프레시 토큰 발급 | X |
| POST | `/auth/refresh` | 리프레시 토큰 회전 + 새 액세스 토큰 발급 | X |
| POST | `/auth/logout` | 제출된 리프레시 토큰 폐기 (멱등) | X |

- 액세스 토큰 유효시간 15분, 리프레시 14일 (회전 활성)
- 리프레시는 opaque 랜덤 문자열, DB 에는 SHA-256 해시만 저장

### 사용자

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/users?page=0&size=20` | 사용자 페이지 조회 (`size` 1~100) | O |
| GET | `/users/{id}` | 사용자 단건 조회 | O |
| POST | `/users` | 사용자 등록 (회원가입, 검증 규칙 적용) | X |
| PUT | `/users/{id}` | 사용자 수정 (username, email 만) | O |
| DELETE | `/users/{id}` | 사용자 삭제 | O |

인증이 필요한 요청은 `Authorization: Bearer <accessToken>` 헤더 필수.
검증 규칙(비밀번호 복잡도, loginId 형식 등)은 [`ValidationUtil`](src/main/java/com/cmn/validation/ValidationUtil.java) 참고.

### Swagger UI

- dev 프로필: `http://localhost:8080/swagger-ui.html`
- prod 프로필: 비활성화 (외부 노출 방지)
- 우측 상단 **Authorize** 버튼에 로그인으로 받은 accessToken 을 입력하면
  이후 요청에 Authorization 헤더가 자동 첨부됨

### Actuator

| 프로필 | 노출 엔드포인트 |
|---|---|
| dev | `/actuator/health`, `/info`, `/metrics`, `/loggers` |
| prod | `/actuator/health` (상세 미노출) |

`env`, `beans`, `mappings`, `configprops` 는 어떤 프로필에서도 비노출 (자격증명·구조 노출 위험).

## 보안 구성

- **비밀번호**: BCrypt 해시 저장, 길이/복잡도 검증 (영문+숫자+특수문자 각 1개 이상)
- **인증**: JWT (HS256) + `JwtAuthInterceptor` 요청별 검증. 리프레시 토큰 회전으로 세션 무효화 지원
- **파라미터 필터**: `ParameterSecurityFilter` 가 SQL 예약어/XSS 패턴 사전 차단
- **보안 헤더**: `SecurityHeadersFilter` 가 X-Content-Type-Options, X-Frame-Options, Referrer-Policy, Cache-Control 부여
- **로그 마스킹**: 로그인 실패 로그에서 loginId 원문 대신 마스킹된 값 사용 (`MaskingUtil`)
- **전역 예외 처리**: `GlobalExceptionHandler` 가 표준 `ErrorResponse` 로 변환 (400/401/404/405/409/500)

## CI

- `.github/workflows/ci.yml` — PR·main 푸시 시 `mvn verify` 자동 실행 (단위+통합)
- main 브랜치는 GitHub 브랜치 보호 규칙으로 PR 필수 (직접 푸시 차단)
- 로컬에도 `.githooks/pre-push` 로 실수로 main 에 직접 push 하는 것을 차단

## 개발 규칙

세부 컨벤션은 `.claude/rules/` 참고:
- `coding.md` — 패키지·네이밍·Lombok·로깅 규칙
- `mybatis.md` — 매퍼 명명·매핑·SQL 작성 규칙
- `database.md` — 명명·컬럼 표준·성능 규칙
- `git.md` — 커밋 메시지·브랜치 규칙

## 참고

- 로그 롤링 정책: `src/main/resources/logback-spring.xml`
- 로컬 pgAdmin (선택): `docker compose --profile tools up -d pgadmin` → `http://localhost:5050`
