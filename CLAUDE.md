# javamaven

Spring Boot 3.3.5 / Java 21 / PostgreSQL / MyBatis 기반 프로젝트.

## 기술 스택
- Spring Boot 3.3.5 (Web)
- Java 21 (Maven)
- MyBatis (`mybatis-spring-boot-starter` 3.0.3)
- PostgreSQL (`org.postgresql:postgresql`)

## 디렉토리 구조
```
src/main/
├── java/com/
│   ├── Main.java          # @SpringBootApplication + @MapperScan("com.mapper")
│   ├── controller/        # REST 엔드포인트
│   ├── service/           # 비즈니스 로직 + @Transactional
│   ├── mapper/            # MyBatis 매퍼 인터페이스
│   ├── domain/            # DTO/VO (mybatis type-aliases 대상)
│   └── cmn/               # 공통 영역 (exception, util, config 등)
│       ├── config/        # Spring @Configuration (WebConfig, MyBatisConfig 등)
│       └── exception/     # 전역 예외 핸들러 및 커스텀 예외
└── resources/
    ├── application.yml    # dev/prod 프로필 한 파일에 통합
    ├── mapper/            # MyBatis XML 매퍼 (classpath:mapper/**/*.xml)
    └── db/migration/      # Flyway 마이그레이션 (V1__init.sql, V2__xxx.sql ...)
```

## 실행
```bash
mvn spring-boot:run                                    # 기본 dev
mvn spring-boot:run -Dspring-boot.run.profiles=prod    # prod
```

## 상세 규칙 (세부 사항은 아래 문서 참고)
@.claude/rules/coding.md
@.claude/rules/mybatis.md
@.claude/rules/database.md
@.claude/rules/git.md
