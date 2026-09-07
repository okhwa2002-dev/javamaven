# MyBatis 규칙

## 파일 위치
- 매퍼 인터페이스: `src/main/java/com/mapper/*.java`
- 매퍼 XML: `src/main/resources/mapper/**/*.xml`
- 도메인/DTO: `src/main/java/com/domain/*.java` (type-aliases-package 대상)

## 명명
- 인터페이스: `<도메인>Mapper` (예: `UserMapper`)
- XML 파일: 인터페이스와 동일한 이름 (예: `UserMapper.xml`)
- XML `namespace`는 매퍼 인터페이스의 FQCN과 일치 (`com.mapper.UserMapper`)
- `id`는 인터페이스 메서드명과 동일

## 매핑
- `map-underscore-to-camel-case: true` 설정됨 → DB `user_name` ↔ Java `userName` 자동 매핑
- 복잡한 매핑만 `<resultMap>` 명시, 단순 매핑은 자동 매핑에 의존
- `parameterType`, `resultType`은 짧은 별칭(type-aliases)으로 참조 가능

## SQL 작성
- 파라미터 바인딩은 반드시 `#{param}` 사용. `${param}`은 SQL 인젝션 위험 → 컬럼/테이블 명 등 불가피한 경우에만 사용하고 화이트리스트 검증.
- `SELECT *` 지양, 필요한 컬럼만 명시.
- 동적 SQL: `<if>`, `<choose>`, `<foreach>` 사용. WHERE 절 조립엔 `<where>` 태그로 접두어 처리.
- 페이징은 PostgreSQL의 `LIMIT ? OFFSET ?` 사용.

## 트랜잭션
- 서비스 계층에 `@Transactional` 부여. 매퍼/컨트롤러에는 부여하지 않음.
- 조회 전용은 `@Transactional(readOnly = true)`.
