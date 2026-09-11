# JWT_SECRET 설정 가이드

이 문서는 JWT 서명 키(`JWT_SECRET`)를 로컬 개발 및 로컬 운영 테스트 환경에 안전하게 주입하는 절차를 설명한다.

## 왜 필요한가

- 애플리케이션은 로그인 성공 시 HMAC-SHA256으로 서명된 JWT를 발급한다 (`JwtTokenProvider`).
- 서명 키가 없으면 dev 프로필은 기동 시 임의 키를 자동 생성하지만, **재기동할 때마다 이전 토큰이 모두 무효**가 된다.
- prod 프로필은 서명 키가 없으면 아예 기동을 거부한다 (`JwtConfig.java:47-49`). 임의 키로 뜨면 인스턴스마다 서명이 달라져 로그인/조회가 서로 통하지 않기 때문.

## 요구사항

| 항목 | 값 |
|---|---|
| 알고리즘 | HMAC-SHA256 |
| 최소 길이 | 32바이트 (`JwtConfig.MIN_SECRET_BYTES`) |
| 저장 위치 | 환경변수 또는 `.env` 파일 |
| Git 커밋 | 금지 (`.gitignore`에 `.env`, `.env.*` 이미 등록됨) |

## 1단계: 안전한 값 생성

Git Bash에서 (한 번만 실행하고 값을 보관):

```bash
openssl rand -base64 48
```

출력 예:
```
K8vJq3nP2mR7wY5tZ9xB4cF6hL1jS0eD8gAxHwLc7tRq9mNbEp4YvUsIzXfKjOa2
```

⚠️ 생성한 값은 **Git 커밋 금지, 채팅/이메일 노출 금지**. 유출되면 즉시 새 값으로 교체해야 하며, 교체 시 기존 발급 토큰은 모두 무효가 된다.

## 2단계: 애플리케이션에 주입

이 프로젝트는 `pom.xml`에 `spring-dotenv` 라이브러리를 포함하고 있어, 프로젝트 루트의 `.env` 파일을 환경변수처럼 자동으로 읽는다.

### 방법 A: `.env` 파일 사용 (권장)

프로젝트 루트에 있는 `.env.dev.example` / `.env.prod.example`을 복사해서 실제 값을 채운다.

**dev 프로필용** — 프로젝트 루트에 `.env.dev` 파일:
```
DB_PASSWORD=dailypw
JWT_SECRET=<위에서 생성한 값>
```

**prod 프로필 로컬 테스트용** — 프로젝트 루트에 `.env.prod` 파일:
```
DB_URL=jdbc:postgresql://localhost:5438/daily
DB_USERNAME=daily
DB_PASSWORD=dailypw
JWT_SECRET=<위에서 생성한 값>
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

실행:
```bash
# dev
mvn spring-boot:run

# prod (로컬 테스트)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 방법 B: 커맨드라인에서 직접 주입

한 번만 확인할 때 편리하지만, 쉘 히스토리에 값이 남는다는 단점이 있다.

```bash
JWT_SECRET='<위에서 생성한 값>' \
DB_PASSWORD=dailypw \
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 방법 C: IntelliJ Run Configuration

1. 상단 툴바 → **Edit Configurations...**
2. Spring Boot 설정 선택 → **Environment variables** 필드에 세미콜론 구분 입력:
   ```
   JWT_SECRET=<위에서 생성한 값>;DB_PASSWORD=dailypw
   ```
3. **Active profiles**: `prod` (또는 `dev`)
4. Apply → Run

## 3단계: 동작 검증

### 정상 기동 확인

로그에서 아래 두 항목을 확인한다.
```
The following 1 profile is active: "prod"
Tomcat started on port(s): 80
```
그리고 `"JWT_SECRET 이 설정되지 않아 임의 키를 생성합니다"` 경고가 **뜨지 않아야** 정상.

### 안전장치 확인 (선택)

prod 프로필로 `JWT_SECRET`을 비운 채 기동해본다. 아래 예외로 즉시 종료되면 `JwtConfig.java`의 안전장치가 기대대로 동작 중이다.
```
IllegalStateException: security.jwt.secret (JWT_SECRET) is required when the prod profile is active
```

### 길이 검증 확인 (선택)

32바이트 미만 값으로 시도하면 다음 예외로 종료된다.
```
IllegalStateException: security.jwt.secret must be at least 32 bytes (현재 N)
```

## 프로필별 동작 요약

| 프로필 | JWT_SECRET 미설정 시 | 결과 |
|---|---|---|
| `dev` | 임의 키 자동 생성 | 기동됨. **재기동 시 기존 토큰 모두 무효** |
| `prod` | 즉시 예외 발생 | 기동 중단 |

## 유출 대응

키가 유출됐다고 판단되면 다음 순서로 조치한다.

1. 새 값 생성 (`openssl rand -base64 48`)
2. `.env` 또는 배포 환경변수 교체
3. 애플리케이션 재기동
4. 기존 발급 토큰은 자동으로 모두 무효화됨 → 사용자 재로그인 필요

## 관련 파일

| 경로 | 역할 |
|---|---|
| `src/main/java/com/cmn/config/JwtConfig.java` | 서명 키 로딩·검증 로직 |
| `src/main/java/com/cmn/jwt/JwtProperties.java` | `security.jwt.*` 설정 바인딩 |
| `src/main/java/com/cmn/jwt/JwtTokenProvider.java` | 토큰 발급·검증 |
| `src/main/resources/application.yml` | `security.jwt.secret: ${JWT_SECRET:}` 매핑 |
| `.env.dev.example`, `.env.prod.example` | 환경변수 템플릿 |
