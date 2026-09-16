# javamaven

Spring Boot 3.3.5 / Java 21 / PostgreSQL / MyBatis 기반 프로젝트.

## 기술 스택
- Spring Boot 3.3.5 (Web, Validation, Actuator)
- Java 21 (Maven)
- MyBatis (`mybatis-spring-boot-starter` 3.0.3)
- PostgreSQL (`org.postgresql:postgresql`) + Spring Boot `spring.sql.init` (dev/test 만 `schema.sql` 자동 실행, prod 는 never)
- JWT (`jjwt` 0.12.6) + BCrypt (`spring-security-crypto`)
- 통합 테스트: Testcontainers (Postgres)

## 디렉토리 구조
```
src/main/
├── java/com/
│   ├── Main.java          # @SpringBootApplication + @EnableScheduling + @MapperScan("com.mapper")
│   ├── controller/        # REST 엔드포인트 (AuthController, UserController)
│   ├── service/           # 비즈니스 로직 + @Transactional (AuthService, UserService, RefreshTokenService, RefreshTokenCleanupJob)
│   ├── mapper/            # MyBatis 매퍼 인터페이스
│   ├── domain/            # DTO/VO (mybatis type-aliases 대상)
│   └── cmn/               # 공통 영역
│       ├── config/        # Spring @Configuration (Web/OpenApi/Jwt/Password/Filter)
│       ├── exception/     # 전역 예외 핸들러 및 커스텀 예외
│       ├── filter/        # ParameterSecurityFilter, SecurityHeadersFilter
│       ├── jwt/           # JwtTokenProvider, JwtAuthInterceptor, JwtProperties
│       └── utils/         # 공통 유틸
│           ├── MaskingUtil     # loginId/email 마스킹
│           └── ValidationUtil  # 필드 규칙 상수 + 검증 함수 + 랜덤 생성기
└── resources/
    ├── application.yml    # dev/prod 프로필 한 파일에 통합
    ├── logback-spring.xml # 로그 롤링 정책
    ├── mapper/            # MyBatis XML 매퍼 (classpath:mapper/**/*.xml)
    └── schema.sql         # 애플리케이션 스키마. 기동 시 spring.sql.init 이 적용 (CREATE ... IF NOT EXISTS)
```

## 실행
```bash
mvn spring-boot:run                                    # 기본 dev
mvn spring-boot:run -Dspring-boot.run.profiles=prod    # prod
```

## 테스트
```bash
mvn test      # 단위 테스트만 (Docker 불필요)
mvn verify    # 단위 + 통합 테스트 (Testcontainers, Docker 필요)
```
- `*Test.java` = 단위(surefire), `*IT.java` = 통합(failsafe)

## 상세 규칙 (세부 사항은 아래 문서 참고)
@.claude/rules/coding.md
@.claude/rules/mybatis.md
@.claude/rules/database.md
@.claude/rules/git.md
