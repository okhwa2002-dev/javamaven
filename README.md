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
    └── schema.sql           # 테이블 DDL
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

컨테이너 최초 기동 시 `schema.sql`이 자동 적용됩니다. 이후 변경분은 수동 반영.

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

`JWT_SECRET` 생성 예:
```bash
openssl rand -base64 48
```

## API 개요

### 인증

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | `/auth/login` | login_id/password로 로그인 → JWT 발급 | X |

### 사용자

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| GET | `/users` | 전체 사용자 조회 | O |
| GET | `/users/{id}` | 사용자 단건 조회 | O |
| POST | `/users` | 사용자 등록 (회원가입) | X |
| PUT | `/users/{id}` | 사용자 수정 | O |
| DELETE | `/users/{id}` | 사용자 삭제 | O |

인증이 필요한 요청은 `Authorization: Bearer <JWT>` 헤더 필수.

### Swagger UI

- dev 프로필: `http://localhost:8080/swagger-ui.html`
- prod 프로필: 비활성화 (외부 노출 방지)

## 보안 구성

- **비밀번호**: BCrypt 해시 저장
- **인증**: JWT (HS256) + `JwtAuthInterceptor`로 요청별 검증
- **파라미터 필터**: `ParameterSecurityFilter`가 SQL 예약어/XSS 패턴 사전 차단 (`security.param-filter.enabled`로 제어)
- **전역 예외 처리**: `GlobalExceptionHandler`가 표준 `ErrorResponse` 로 변환

## 개발 규칙

세부 컨벤션은 `.claude/rules/` 참고:
- `coding.md` — 패키지·네이밍·Lombok·로깅 규칙
- `mybatis.md` — 매퍼 명명·매핑·SQL 작성 규칙
- `database.md` — 명명·컬럼 표준·성능 규칙
- `git.md` — 커밋 메시지·브랜치 규칙

## 참고

- 로그 롤링 정책: `src/main/resources/logback-spring.xml`
- 로컬 pgAdmin (선택): `docker compose --profile tools up -d pgadmin` → `http://localhost:5050`
