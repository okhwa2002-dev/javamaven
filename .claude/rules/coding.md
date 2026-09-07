# 코딩 컨벤션

## 패키지
- 루트 패키지: `com` (추후 `com.<도메인>` 형태로 세분화 예정)
- 계층별 하위 패키지: `controller`, `service`, `mapper`, `domain`
- 공통 영역: `com.cmn.*` (예: `com.cmn.exception`, `com.cmn.util`, `com.cmn.config`)
  - 특정 도메인에 속하지 않고 여러 계층·기능에서 재사용되는 코드는 반드시 `cmn` 아래에 배치.

## 네이밍
- 클래스: `PascalCase` (`UserService`, `OrderController`)
- 메서드/변수: `camelCase`
- 상수: `UPPER_SNAKE_CASE`
- DTO/VO: 접미사 `Dto`, `Vo`, `Request`, `Response` 명시

## Spring 스타일
- 생성자 주입만 사용. **`@RequiredArgsConstructor` + `final` 필드** 조합을 기본으로. 필드 주입(`@Autowired`) 금지.
- 컨트롤러는 얇게, 비즈니스 로직은 서비스로.
- 예외는 서비스에서 던지고, 컨트롤러/전역 핸들러에서 응답으로 변환.

## Lombok 사용 규칙
- **DTO/VO**: `@Getter @Setter` (MyBatis가 setter로 값을 채워야 하므로). 불변이 명확하면 `@Getter`만.
- **서비스/컨트롤러**: `@RequiredArgsConstructor`로 생성자 주입.
- **로거**: `@Slf4j`로 `log` 변수 자동 생성.
- **금지 어노테이션**:
  - `@Data` — Entity/DTO에서 `equals/hashCode`가 순환참조/성능 문제 유발 가능. 필요한 것만 명시적으로 조합.
  - `@AllArgsConstructor` — 필드 순서 변경 시 호출부가 조용히 깨짐. 필요한 생성자는 명시적으로 작성.
  - `@Builder` on entity — 사용 시 팀 합의 후. 남용하면 의도 불명확.

## 로깅
- `@Slf4j` 사용 (내부적으로 SLF4J Logger 생성).
- `System.out.println`, `printStackTrace()` 금지.
- 운영 로그에 개인정보/자격증명 노출 금지.

## 금지
- 하드코딩된 DB 접속정보, API 키, 비밀번호
- `printStackTrace()` — 로거를 통해 예외 로깅
- 필드 주입(`@Autowired` on field) — 생성자 주입만 사용
