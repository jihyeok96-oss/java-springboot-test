========================================================================
Ⅰ. 평가지
========================================================================

1. 프로젝트 및 PostgreSQL 접속 환경 구성
   Spring Boot 4.x와 Java 21을 기준으로 Gradle 프로젝트를 구성하시오. PostgreSQL(5432)의 company 데이터베이스에 사용자 postgress, 비밀번호 1004로 접속하도록 application.yml을 작성하고, JPA/Hibernate와 Thymeleaf가 정상 동작하도록 필요한 의존성을 build.gradle에 추가하시오. 패키지는 com.example.board를 기준으로 한다.

2. JPA 공통 설정 및 시간 처리 Configuration
   Board의 생성/수정 시각을 안정적으로 관리할 수 있도록 Configuration 클래스를 작성하시오. Java Time(ZoneId: Asia/Seoul)을 사용하는 Clock을 @Bean으로 등록하고, JPA Auditing을 활성화하시오. 애플리케이션에서 해당 Bean을 주입받아 사용할 수 있어야 한다.

3. Board Entity 작성
   다음 항목을 갖는 Board Entity를 작성하시오: id, title, content, author, hits, createdAt, updatedAt. id는 자동 생성되는 Long PK로 하고, title/author는 필수값, hits의 초기값은 0으로 한다. createdAt/updatedAt은 JPA Auditing을 이용하여 자동 관리하시오. 테이블명은 board로 지정한다.

4. Repository 및 조회 기능
   BoardRepository를 작성하시오. 기본 CRUD가 가능해야 하며, 제목에 특정 문자열이 포함된 게시글을 createdAt 내림차순으로 조회하는 메서드를 추가하시오. 목록 화면에서 사용할 수 있도록 적절한 Spring Data JPA 메서드 명명 규칙을 사용한다.

5. Service 계층의 CRUD 및 조회수 증가
   BoardService를 작성하시오. 전체 목록 조회, 단건 조회, 등록, 수정, 삭제를 제공한다. 단건 조회 시 조회수(hits)를 1 증가시키고 증가된 값을 반영한 Board를 반환한다. 존재하지 않는 id가 전달되면 적절한 예외를 발생시키고, 수정 시에는 기존 id와 생성일시는 보존하면서 title/content/author를 갱신한다.

6. Thymeleaf 게시글 목록/등록/수정/상세 화면
   Thymeleaf를 이용하여 게시글 목록(index.html), 등록(form.html), 수정(form.html), 상세(detail.html) 화면을 작성하시오. 목록에는 id/title/author/hits/createdAt을 표시하고, 등록·수정·상세·삭제로 이동할 수 있는 링크 또는 버튼을 제공한다. 폼은 Board의 title/content/author를 입력받는다.

7. MVC Controller 작성
   BoardController를 작성하시오. GET /boards에서 목록을 보여주고, GET /boards/new에서 등록 폼을 제공하며, POST /boards에서 등록한다. GET /boards/{id}에서 상세를 보여주고, GET /boards/{id}/edit에서 수정 폼을 제공하며, POST /boards/{id}에서 수정하고, POST /boards/{id}/delete에서 삭제한다. 등록/수정/삭제 후에는 적절한 리다이렉트를 사용한다.

8. REST Controller 및 JSON API
   BoardRestController를 작성하시오. GET /api/boards에서 전체 목록, GET /api/boards/{id}에서 단건, POST /api/boards에서 등록, PUT /api/boards/{id}에서 수정, DELETE /api/boards/{id}에서 삭제가 가능하도록 한다. DTO는 사용하지 않으며 Board Entity를 직접 JSON으로 반환한다. 등록/수정 요청은 JSON을 사용한다.

9. Talend API Tester를 이용한 API 검증
   Talend API Tester 또는 동등한 HTTP API 테스트 도구를 이용하여 REST API의 GET 목록, GET 단건, POST 등록, PUT 수정, DELETE 삭제를 순서대로 검증하시오. 각 요청의 URL, HTTP Method, Header(Content-Type), Body가 올바른지 확인하고 응답 상태 코드와 JSON 결과를 캡처하여 제출한다.

10. 통합 CRUD 점검 및 오류 대응
    애플리케이션을 실행한 후 브라우저에서 등록→목록→상세→수정→목록→삭제의 전체 흐름을 검증하고, REST API에서도 동일한 데이터가 확인되는지 점검하시오. PostgreSQL의 board 테이블에 데이터가 실제 반영되는지 확인한다. 마지막으로 존재하지 않는 id 조회와 잘못된 요청에 대한 오류를 확인하고, 발생 가능한 문제 2가지와 해결 방법을 서술하시오.

========================================================================
Ⅱ. 답안 작성 요령
========================================================================
- 문항 번호와 파일명을 코드 상단 주석 또는 캡처 파일명에 명확히 표시한다.
- application.yml의 DB 접속 정보는 문제에서 제시한 값과 정확히 일치시키고 YAML 들여쓰기를 유지한다.
- Entity의 createdAt/updatedAt은 @CreatedDate/@LastModifiedDate로 관리하고 @EnableJpaAuditing을 활성화한다.
- Controller는 화면 반환(MVC), RestController는 JSON API 반환으로 역할을 분리한다.
- Service에서 조회수 증가와 예외 처리를 수행하고, 수정에서는 id와 createdAt을 변경하지 않는다.
- Thymeleaf의 th:object/th:field/th:each/th:href/th:action을 사용하여 서버 데이터와 화면을 연결한다.
- REST 테스트는 GET→POST→PUT→GET→DELETE 순서로 수행하고 HTTP method, URL, Content-Type, JSON body, status를 캡처한다.
- 모든 코드는 실행 가능성을 우선하며 import 누락, 패키지 경로 불일치, 템플릿 경로 오류를 최종 점검한다.