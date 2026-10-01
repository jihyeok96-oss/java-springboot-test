# 문항 1~10 · GitHub 수업 자료 확인 및 구현 근거

대상 평가지는 `board/TEST.md`이다. 사용자가 지정한 [jihyeok96-oss의 저장소 목록](https://github.com/jihyeok96-oss?tab=repositories)에 공개된 저장소 14개의 기본 브랜치 파일 트리를 확인하고, 각 저장소에서 관련 있는 대표 소스 파일을 읽어 아래 내용을 정리했다. 모든 저장소의 모든 개별 파일을 읽었다는 뜻은 아니다. 별도 로그인, 회원 권한, 파일 업로드, 페이지네이션 같은 기능은 평가문제에서 요구하지 않아 가져오지 않았다.

코드 주석의 `[수업 예제 참고]`는 실제 확인한 수업 코드의 언어·클래스·메서드 작성 방식을 따른 부분이다. `[수업 예제 외 추가]`는 확인한 대표 예제에 그대로 존재하지 않거나, 평가조건과 실행 가능성을 충족하기 위해 보완한 부분이다. 클래스 전체가 새 기술이라는 뜻이 아니라 해당 주석에서 설명한 차이만 가리킨다.

## 공개 저장소 14개 확인 결과

| 저장소 · 확인 브랜치 | 실제 확인한 대표 파일 | 언어 / 구조 | 평가문제와의 관계 |
| --- | --- | --- | --- |
| `spring-practice-ans` · `master` | [BoardRepository.java](https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/java/com/company/myapp/repository/BoardRepository.java), [BoardService.java](https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/java/com/company/myapp/service/BoardService.java), [BoardController.java](https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/java/com/company/myapp/controller/BoardController.java), [board/list.html](https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/resources/templates/board/list.html), [BoardDummyDataTest.java](https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/test/java/com/company/myapp/BoardDummyDataTest.java) | Java, Spring Boot, JPA, Lombok, Thymeleaf, HTML/CSS, Gradle | 문항 3~7의 가장 직접적인 게시판 예제. `JpaRepository`, `ContainingIgnoreCase` 검색, `@RequiredArgsConstructor`, CRUD 메서드, `Model`, `@ModelAttribute`, `redirect:`를 참고했다. 더미 테스트의 `deleteAll()`은 기존 데이터 삭제를 일으키므로 평가용 실행 흐름에 복사하지 않았다. |
| `mobility_project_backend` · `main` | [build.gradle](https://github.com/jihyeok96-oss/mobility_project_backend/blob/main/build.gradle), [BoardRestController.java](https://github.com/jihyeok96-oss/mobility_project_backend/blob/main/src/main/java/com/kim/mobility/controller/BoardRestController.java), [GlobalExceptionHandler.java](https://github.com/jihyeok96-oss/mobility_project_backend/blob/main/src/main/java/com/kim/mobility/controller/GlobalExceptionHandler.java) | Java, Spring Boot, JPA, REST, Validation, Security, QueryDSL | 문항 8·10의 `/api/boards`, `@RequestBody`, `@Valid`, 각 HTTP 매핑과 예외 응답 방식 참고. 원본은 DTO와 인증을 사용하지만 문항 8은 Entity 직접 반환을 요구하므로 DTO·인증 구조는 도입하지 않았다. |
| `ref-back` · `master` | [QnaService.java](https://github.com/jihyeok96-oss/ref-back/blob/master/src/main/java/com/logic/project/service/QnaService.java), [QnaRestController.java](https://github.com/jihyeok96-oss/ref-back/blob/master/src/main/java/com/logic/project/api/QnaRestController.java), [ApiExceptionHandler.java](https://github.com/jihyeok96-oss/ref-back/blob/master/src/main/java/com/logic/project/controller/ApiExceptionHandler.java), [qna/form.html](https://github.com/jihyeok96-oss/ref-back/blob/master/src/main/resources/templates/qna/form.html) | Java, Spring Boot, JPA, Thymeleaf, REST | 문항 4~8·10 참고. `findAllByOrderByCreatedAtDesc()`, 클래스의 `@Transactional(readOnly = true)`와 쓰기 메서드의 `@Transactional`, 기존 Entity 수정, Entity 직접 JSON 반환, `Map` 오류 응답, 등록/수정 공용 폼이 있다. |
| `jspServlet` · `master` | [BoardDAO.java](https://github.com/jihyeok96-oss/jspServlet/blob/master/project2/src/main/java/com/mobility/git/project2/dao/BoardDAO.java), [BoardController.java](https://github.com/jihyeok96-oss/jspServlet/blob/master/project2/src/main/java/com/mobility/git/project2/controller/BoardController.java) | Java, Servlet, JSP, JDBC, SQL | `findAll`, `findById`, `insert`, `update`, `delete`로 분리하는 게시판 CRUD 사고방식을 참고했다. 문항은 Spring Data JPA·Thymeleaf이므로 `HttpServlet`, `PreparedStatement`, JSP를 섞지 않았다. 원본 GET 삭제 대신 평가문제의 POST 삭제를 따른다. |
| `mobility0917` · `main` | [Board.java](https://github.com/jihyeok96-oss/mobility0917/blob/main/src/main/java/com/oji/mobility/domain/Board.java), [BoardService.java](https://github.com/jihyeok96-oss/mobility0917/blob/main/src/main/java/com/oji/mobility/service/BoardService.java), [BoardRepository.java](https://github.com/jihyeok96-oss/mobility0917/blob/main/src/main/java/com/oji/mobility/repository/BoardRepository.java) | Java, Spring Boot, JPA, QueryDSL, Lombok | 문항 3~5 참고. `Long id`, `IDENTITY`, `hits = 0L`, 생성시각 수정 금지, `orElseThrow`가 있다. 원본 날짜 관리인 `@PrePersist/@PreUpdate`는 문항 3의 Auditing으로 바꾸었다. `increaseViewCount`의 JPQL은 Entity에 없는 `viewCount`를 사용하므로 그대로 복사하지 않았다. |
| `spring-practice` · `main` | [build.gradle](https://github.com/jihyeok96-oss/spring-practice/blob/main/build.gradle), [application.yaml](https://github.com/jihyeok96-oss/spring-practice/blob/main/src/main/resources/application.yaml) | Java 21, Spring Boot 4.1.1, Gradle | 문항 1 참고. `java.toolchain`, JPA·Thymeleaf·Web MVC starter, PostgreSQL 드라이버, Lombok 및 JUnit Platform 설정을 확인했다. 이 저장소는 게시판 로직 없이 초기 프로젝트 구성이 중심이다. |
| `java21_basic` · `master` | [Board.java](https://github.com/jihyeok96-oss/java21_basic/blob/master/ex00/src/ch07/sec4/Board.java), [ListEx1.java](https://github.com/jihyeok96-oss/java21_basic/blob/master/ex00/src/ch12/sec3/ListEx1.java), [MapEx1.java](https://github.com/jihyeok96-oss/java21_basic/blob/master/ex00/src/ch12/sec5/MapEx1.java), [ExceptionEx1.java](https://github.com/jihyeok96-oss/java21_basic/blob/master/ex00/src/ch14/sec2/ExceptionEx1.java) | Java, Lombok, 클래스, 제네릭, List/Map, 예외 처리 | 문항 3~5·10의 Java 작성 방식 참고. 일반 클래스의 필드 선언, Lombok Getter/Setter, `List<T>`, `Map<K,V>`, 예외 종류별 처리가 있다. 웹·JPA 기능을 제공하는 저장소는 아니다. |
| `gittest` · `master` | [README.md](https://github.com/jihyeok96-oss/gittest/blob/master/README.md) | Git 명령 설명 Markdown | Git 저장소 초기화·등록·commit·push·pull 학습 자료다. 게시판 Java/HTML 구현 자료가 없어 코드 근거로 사용하지 않았다. GitHub 배포는 평가문제에 포함되지 않는다. |
| `ref-front` · `master` | [src/api/index.js](https://github.com/jihyeok96-oss/ref-front/blob/master/src/api/index.js), [QnaForm.vue](https://github.com/jihyeok96-oss/ref-front/blob/master/src/views/QnaForm.vue) | JavaScript, Vue, Axios, HTML/CSS | JSON `Content-Type`, 등록/수정 상태에 따라 공용 폼을 사용하는 UI 흐름과 오류 메시지를 확인했다. 문항 6의 Thymeleaf 지시를 따라 Vue·Axios·별도 npm 프로젝트는 도입하지 않았다. |
| `project1` · `main` | [NoticeRestController.java](https://github.com/jihyeok96-oss/project1/blob/main/backend/notice/src/main/java/com/oji/notice/controller/NoticeRestController.java), [NoticeService.java](https://github.com/jihyeok96-oss/project1/blob/main/backend/notice/src/main/java/com/oji/notice/service/NoticeService.java), [ApiExceptionHandler.java](https://github.com/jihyeok96-oss/project1/blob/main/backend/notice/src/main/java/com/oji/notice/controller/ApiExceptionHandler.java) | Java, Spring Boot, JPA, REST / JavaScript, Vue | 문항 5·8~10 참고. 단건 조회 시 `hits + 1`, 등록 201·수정/조회 200·삭제 204, `@Valid @RequestBody`, 상태코드별 오류 응답이 있다. 원본 DTO는 문항 8에 맞춰 제외했다. 원본 일부 파일의 패키지 불일치는 재현하지 않고 `com.example.board`로 통일했다. |
| `jihyeok96-oss.github.io` · `main` | [list.html](https://github.com/jihyeok96-oss/jihyeok96-oss.github.io/blob/main/list.html), [write.html](https://github.com/jihyeok96-oss/jihyeok96-oss.github.io/blob/main/write.html), [view.html](https://github.com/jihyeok96-oss/jihyeok96-oss.github.io/blob/main/view.html) | HTML5, CSS | 문항 6의 검색 폼, 게시판 표, 입력 폼, 상세 표시 같은 기본 화면 구성을 참고했다. 원본은 정적 예시 데이터이므로 서버 값은 Thymeleaf 속성으로 연결했다. |
| `jihyeok96-ojh.github.io` · `main` | [index.html](https://github.com/jihyeok96-oss/jihyeok96-ojh.github.io/blob/main/index.html) | HTML5, CSS, 개인 소개 페이지 | `html lang="ko"`, UTF-8, viewport와 기본 HTML/CSS 작성 방식을 확인했다. 본문에 React/TypeScript가 소개되지만 실제 확인한 소스는 HTML/CSS이므로 이를 평가 프로젝트의 사용 기술로 해석하지 않았다. 게시판 CRUD 코드는 없다. |
| `java-springboot-test` · `main` | [build.gradle](https://github.com/jihyeok96-oss/java-springboot-test/blob/main/build.gradle), [TimeConfig.java](https://github.com/jihyeok96-oss/java-springboot-test/blob/main/src/main/java/com/example/board/config/TimeConfig.java), [Board.java](https://github.com/jihyeok96-oss/java-springboot-test/blob/main/src/main/java/com/example/board/domain/Board.java), [application.yaml](https://github.com/jihyeok96-oss/java-springboot-test/blob/main/src/main/resources/application.yaml), [BoardApplicationTests.java](https://github.com/jihyeok96-oss/java-springboot-test/blob/main/src/test/java/com/example/board/BoardApplicationTests.java) | Java 21, Spring Boot 4.1.1, JPA, Thymeleaf, Validation, ZonedDateTime | 문항 1~3의 직접적인 출발 자료. 현재 평가 프로젝트와 같은 `com.example.board`, `Clock`, `ZoneId.of("Asia/Seoul")`, `DateTimeProvider`, `ZonedDateTime`가 있다. Auditing 연결과 Entity 어노테이션, DB 사용자 및 YAML 계층을 보완했다. |
| `mvclab` · `main` | [MemberService.java](https://github.com/jihyeok96-oss/mvclab/blob/main/src/main/java/com/example/mvclab/member/MemberService.java), [MemberController.java](https://github.com/jihyeok96-oss/mvclab/blob/main/src/main/java/com/example/mvclab/web/MemberController.java), [members/form.html](https://github.com/jihyeok96-oss/mvclab/blob/main/src/main/resources/templates/members/form.html), [GlobalExceptionHandler.java](https://github.com/jihyeok96-oss/mvclab/blob/main/src/main/java/com/example/mvclab/exception/GlobalExceptionHandler.java) | Java, Spring MVC, Validation, Thymeleaf, HTML/CSS | 문항 6·7·10의 `@Valid @ModelAttribute`, `BindingResult`와 입력 오류 시 폼 재표시 방식 참고. 원본 `GlobalExceptionHandler`는 빈 클래스라 실제 예외 응답 구현의 근거로 사용하지 않았다. |

## 문항별 수업 근거와 평가조건 적용

| 문항 | 주된 수업 자료 / 그대로 따른 방식 | 평가조건을 위해 적용한 차이 |
| --- | --- | --- |
| 1. 프로젝트·DB 설정 | `java-springboot-test`와 `spring-practice`의 Gradle Groovy DSL, Spring Boot 4.x, Java 21, JPA/Thymeleaf/Web MVC/Validation 및 PostgreSQL 의존성 | 평가지는 `application.yml`, `company`, `5432`, 사용자 **`postgress`**, 비밀번호 **`1004`**를 요구한다. 원본의 `postgres`와 달라도 평가문제의 철자를 따른다. Hibernate 설정은 `spring.jpa.hibernate.ddl-auto`, SQL 표시와 포맷은 `spring.jpa` 아래로 배치한다. |
| 2. Configuration·시간 처리 | `java-springboot-test/TimeConfig.java`에 이미 `Clock`, 서울 `ZoneId`, `seoulDateTimeProvider(Clock clock)`, `ZonedDateTime.now(clock)`, `@EnableJpaAuditing`이 있다. | `[수업 예제 외 추가]` `dateTimeProviderRef`로 Auditing이 해당 시간 공급 Bean을 실제 사용하도록 연결한다. `DateTimeProvider` 자체는 수업에서 확인한 내용이다. |
| 3. Board Entity | `java-springboot-test/Board.java`의 `@Entity`, `@Table(name = "board")`, `Long id`, `IDENTITY`, Lombok, `hits = 0L`, `ZonedDateTime` 및 필수 컬럼. `spring-practice-ans/entity/Board.java`에는 이미 `@CreatedDate/@LastModifiedDate`와 생성시각 `updatable = false`가 있다. | 수업의 날짜 어노테이션을 적용한다. `[수업 예제 외 추가]` `@EntityListeners(AuditingEntityListener.class)`로 실제 저장/변경 이벤트에 연결해 자동 시간 관리를 완성한다. 서버 관리 필드는 입력값으로 덮어쓰지 않는다. |
| 4. Repository·검색 | `spring-practice-ans`의 `JpaRepository<Board, Long>`와 `Containing` 검색 계열, `ref-back`의 `findAllByOrderByCreatedAtDesc()` | 평가조건에 맞춰 `findByTitleContainingOrderByCreatedAtDesc(String keyword)`를 선언한다. 새로운 검색 라이브러리를 추가하지 않고 이미 수업에서 사용한 Spring Data JPA 메서드명 구성요소를 조합한다. |
| 5. Service·조회수 | `spring-practice-ans`의 `findAll/findById/insert/update/delete`, 생성자 주입, `orElseThrow`; `ref-back`의 읽기/쓰기 트랜잭션 분리; `project1`의 조회수 증가 | 기존 글을 조회해 `title/content/author`만 갱신하므로 `id/createdAt`이 보존된다. 상세 조회와 편집 폼용 조회를 구분해 편집 화면에서 조회수가 증가하지 않도록 한다. `[수업 예제 외 추가]` 없는 게시글은 전용 예외와 404로 구분한다. |
| 6. Thymeleaf 화면 | `spring-practice-ans` 게시판 화면의 `th:each/th:href`, `ref-back/qna/form.html` 공용 폼, `mvclab`의 바인딩·오류 표시, GitHub Pages 게시판의 HTML 구조 | 평가문제의 `index.html`, 공용 `form.html`, `detail.html`을 작성한다. `th:object/th:field/th:action`으로 `title/content/author`를 바인딩하고 목록에 요구된 다섯 항목을 표시한다. `[수업 예제 외 추가]` 오류 후에도 수정 대상 id를 유지하며 폼을 재표시한다. |
| 7. MVC Controller | `spring-practice-ans/BoardController.java`의 `Model`, `@ModelAttribute`, 화면 이름 반환과 `redirect:`, `mvclab`의 `BindingResult` 처리 | 평가문제의 `/boards`, `/boards/new`, `/{id}`, `/{id}/edit`, `POST /boards`, `POST /{id}`, `POST /{id}/delete`를 정확히 적용한다. `[수업 예제 외 추가]` 폼에서 수정 가능한 필드를 제한한다. |
| 8. REST Controller | `mobility_project_backend`의 `@RestController`와 CRUD HTTP 매핑, `ref-back`의 Entity 직접 JSON 반환, `project1`의 등록 201·삭제 204·조회/수정 200 | **DTO를 만들지 않고 `Board`를 요청/응답에 사용한다.** DTO 예제의 요청 구조는 가져오지 않는다. `[수업 예제 외 추가]` Entity 요청에서 서버 관리 필드는 무시하고 편집 가능한 필드만 저장한다. API는 서비스와 동일한 데이터를 사용한다. |
| 9. HTTP 검증 | `project1/NoticeRestController.java`의 200/201/204 및 `ref-front/src/api/index.js`의 `Content-Type: application/json` | `[수업 예제 외 추가]` Talend 또는 동등한 도구로 그대로 실행할 수 있도록 HTTP 요청, 실행 순서, 응답 상태 및 JSON 확인 항목을 작성한다. 요청/응답 예시와 실제 실행 결과는 구분해야 한다. 화면 캡처 제출은 실행한 결과만 사용한다. |
| 10. 통합 점검·오류 | `@SpringBootTest/@Test` 기본 골격은 `java-springboot-test` 등에 있다. 입력 검증은 `mvclab`, API 오류의 `@ExceptionHandler`와 `Map`은 `ref-back`·`mobility_project_backend`·`project1`을 참고한다. | `[수업 예제 외 추가]` 전체 MVC/REST CRUD, 조회수, Auditing, 생성시각 보존, 없는 id 및 잘못된 요청을 확인하는 구체적인 통합 테스트와 점검 기록을 마련한다. MVC 오류는 화면, API 오류는 JSON으로 구분한다. PostgreSQL 실반영은 실제 DB를 사용하는 실행 결과로 확인해야 한다. |

## 원본을 그대로 복사하지 않은 이유

### 문항 1·2·3: 설정과 자동 시각 관리

`java-springboot-test`는 평가 프로젝트의 패키지·기본 Entity·시간 Configuration과 가장 가깝다. 다만 원본 `@EnableJpaAuditing`에는 `dateTimeProviderRef`가 없어서 선언한 `seoulDateTimeProvider`와 연결되지 않는다. 원본 `Board`에도 Auditing listener와 날짜 어노테이션이 없어, 시간 Bean만 만든 상태로는 문항 3을 완성할 수 없다. 날짜 어노테이션은 이미 [spring-practice-ans의 Board Entity](https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/java/com/company/myapp/entity/Board.java)에 존재하므로 해당 수업 방식을 가져왔다. listener와 provider 이름의 연결은 필요한 보완으로 구분했다.

원본 `application.yaml`에서 `hibernate`, `show-sql`, `properties`가 `spring.jpa` 아래에 있지 않은 부분은 정상 JPA 설정 계층으로 수정했다. DB 사용자 이름은 일반적으로 보이는 `postgres`와 평가문제의 `postgress`가 서로 다르므로 **문제의 `postgress`를 기준**으로 작성했다.

### 문항 5·7·8: 기존 Entity를 수정하고 서버 필드를 보호

수업 예제처럼 저장된 Entity를 먼저 조회해 수정한다. 요청으로 넘어온 새로운 Entity를 그대로 저장하면 id, 조회수, 생성시각을 덮어쓸 수 있으므로 `title`, `content`, `author`만 옮긴다. REST API도 Entity를 직접 반환하는 `ref-back`의 방식을 따르면서, 문항 8의 DTO 금지를 지킨다. Lombok, 생성자 주입, Service/Repository/Controller 역할 분리는 수업 예제의 구조를 유지한다.

상세 조회에서 조회수 증가가 필요한 반면 수정 폼 진입·수정·삭제는 상세 읽기가 아니다. 따라서 조회수 증가 없는 조회 경로를 함께 두어 전체 흐름에서 불필요한 증가를 막는다. 트랜잭션 안에서 변경된 값을 저장하고 증가한 Board를 반환한다.

### 문항 6·7: 평가 화면은 Thymeleaf

`ref-front`와 `project1/frontend-vue`에는 Vue 예제가 있지만 평가문제는 Thymeleaf를 명시한다. 프론트엔드는 Spring Boot의 `templates` HTML과 `static/css`로 작성하며, 수업의 `th:object`, `th:field`, `th:each`, `th:href`, `th:action`을 사용한다. JSP/Servlet 저장소는 CRUD 개념 참고용이며 Thymeleaf 화면에 JSP 코드를 섞지 않는다.

### 문항 9·10: 오류 응답과 실행 검증

`mvclab`의 오류 핸들러는 빈 클래스여서 예외 처리 구현의 근거로 사용할 수 없다. 구현된 `ref-back`, `mobility_project_backend`, `project1`의 어노테이션과 상태코드/Map 방식에 따라 보완했다. 없는 id와 입력값 오류를 구분하는 전용 예외, malformed JSON/id 처리, 폼 재표시의 HTTP 상태, 구체적인 테스트와 결과 기록은 필요한 추가 내용으로 표시한다.

`spring-practice-ans/BoardDummyDataTest.java`는 기존 데이터를 삭제하고 더미 데이터를 넣는 예제다. 실제 평가 흐름 확인은 기존 데이터 전체를 지우는 방식 대신 점검을 위해 생성한 게시글로 수행한다. 테스트 성공은 실행한 환경을 기준으로 해석한다. 특히 별도 테스트 DB 성공만으로 평가문제의 PostgreSQL `company`에 저장됐다고 기록하면 안 된다.

## 문항 9·10의 검증 파일과 범위

| 파일 | 용도 / 수업 예제와의 관계 |
| --- | --- |
| `src/test/java/com/example/board/BoardApplicationTests.java` | 원본의 `@SpringBootTest/@Test` 골격을 확장한 실제 CRUD·오류·조회수·날짜 검증. MockMvc와 H2 메모리 DB를 이용하는 구체적인 검증은 `[수업 예제 외 추가]`이다. |
| `docs/board-api.http` | 문항 9의 GET/POST/PUT/GET/DELETE 요청을 재현하는 HTTP 테스트 파일. URL, Method, Header, JSON Body를 함께 기록하는 실습 보조 자료로 추가했다. |
| `docs/TEST_GUIDE.md` | 문항 9·10의 Talend/API 요청 재현, 브라우저 흐름, PostgreSQL 확인, 오류 두 가지와 해결 방법을 정리한 제출 준비 문서. |
| `docs/verification/` | 생성되는 검증 기록이 있다면 실행 도구와 DB 종류를 확인한 뒤 해석한다. 자동화된 응답 기록과 브라우저/Talend 화면 캡처는 서로 다른 자료다. |

평가문제의 PostgreSQL 접속 설정은 `postgress / 1004`를 유지한다. 사용자가 확인한 실제 로컬 DB 사용자는 `postgres`이므로 로컬 실행에서는 `--spring.datasource.username=postgres`로 덮어쓴다. 이전 수업 테이블이 있는 `company.public.board` 대신 `company.board_exam_ch09.board`에서 점검하며, JDBC URL의 `currentSchema=board_exam_ch09`와 서버 포트 18080을 사용한다. 계정 확인 및 점검 환경 준비만으로 성공했다고 기록하지 않고, 실제 PostgreSQL 실행 결과는 `docs/verification/`에 별도로 기록한다. H2 테스트 결과와 실제 PostgreSQL 결과도 구분한다. 실행·확인 절차는 `docs/TEST_GUIDE.md`에 있다.

## 주석을 읽는 순서

1. 각 파일 상단의 `문항 번호 · 파일명`에서 담당 문제를 확인한다.
2. `[수업 예제 참고]`의 저장소·파일·메서드를 이 문서의 링크와 비교한다.
3. 메서드 및 템플릿 주석으로 요청 → Controller → Service → Repository → DB → 화면/JSON의 흐름을 확인한다.
4. `[수업 예제 외 추가]`는 왜 원본과 달라졌는지 읽는다. 평가조건을 위해 필요한 보완과 선택적인 실습 도구를 구분한다.
