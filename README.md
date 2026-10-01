# TEST.md 게시판 평가 프로젝트

Spring Boot 4.1.1 / Java 21 / Gradle / PostgreSQL / Spring Data JPA / Thymeleaf로 1~10번을 구현했다.
각 소스 상단 및 주요 동작에 문항 번호와 설명 주석을 작성했으며, 수업 자료에 없는 보완은
`[수업 예제 외 추가]`로 표시했다. DTO 없이 Board Entity를 폼과 JSON에 직접 사용한다.

## 파일과 평가문항

| 문항 | 주요 파일 | 구현 내용 |
| --- | --- | --- |
| 1 | `build.gradle`, `src/main/resources/application.yml`, `BoardApplication.java` | Boot 4.x, Java 21, DB와 JPA/Thymeleaf 의존성 |
| 2 | `config/TimeConfig.java` | Asia/Seoul Clock Bean과 Auditing 시간 공급자 연결 |
| 3 | `domain/Board.java` | 자동 PK, 필수 제목/작성자, hits=0, 자동 생성/수정 시간 |
| 4 | `repository/BoardRepository.java` | 기본 CRUD, 제목 포함 검색 및 최신순 정렬 |
| 5 | `service/BoardService.java` | CRUD, 상세 조회수 증가, 생성일/PK 보존, 없는 id 예외 |
| 6 | `templates/board/index.html`, `form.html`, `detail.html`, `static/css/style.css` | 목록·공통 등록/수정 폼·상세 화면 |
| 7 | `controller/BoardController.java` | 지정된 MVC 경로와 등록/수정/삭제 후 리다이렉트 |
| 8 | `controller/BoardRestController.java` | Entity JSON CRUD, 201/200/204 응답 |
| 9 | `docs/board-api.http`, `scripts/verify-api.ps1`, `docs/verification/` | 실제 HTTP 요청/응답 기록 및 재검증 도구 |
| 10 | `controller/GlobalExceptionHandler.java`, `exception/BoardNotFoundException.java`, `BoardApplicationTests.java` | 400/404 대응, 통합 테스트, 브라우저/DB 점검 기록 |

Java 경로 기준은 `src/main/java/com/example/board`, 템플릿은 `src/main/resources`다.

## 실행

평가문항의 기본 DB는 `localhost:5432/company`, 사용자 `postgress`, 비밀번호 `1004`다.
이 계정/DB가 준비된 평가 환경에서는 아래 명령으로 실행한다.

```powershell
.\gradlew.bat bootRun
```

현재 사용자가 확인한 로컬 계정은 `postgres`다. 기존 `company.public.board`는 다른 수업의
테이블 구조여서 검증에는 별도 `board_exam_ch09` 스키마를 사용했다. 기본 설정 파일은 평가문항
그대로 두고 실행 인자만 바꾼다. 스키마 준비와 상세 실행 방법은 [TEST_GUIDE.md](docs/TEST_GUIDE.md)를 참고한다.

```powershell
.\gradlew.bat bootRun --args="--server.port=18080 --spring.datasource.username=postgres --spring.datasource.url=jdbc:postgresql://localhost:5432/company?currentSchema=board_exam_ch09"
```

화면: `http://localhost:18080/boards` · API: `http://localhost:18080/api/boards`

## 검증과 수업 참고 자료

```powershell
.\gradlew.bat test bootJar
```

자동 테스트는 H2에서 실제 Controller/Service/Repository를 연결하며, 테스트마다 롤백한다.
실제 PostgreSQL 확인과 구분한 결과는 [검증 보고서](docs/verification/RESULTS.md)를 참고한다.

- [14개 GitHub 저장소와 문항별 참고 근거](docs/GITHUB_REFERENCE.md)
- [9·10번 실행·캡처 안내 및 오류 해결](docs/TEST_GUIDE.md)
- [HTTP 요청 예제](docs/board-api.http)
- [실제 API 요청/응답 보고서](docs/verification/api-report.html)

조회수는 MVC/REST의 상세 조회에서 증가한다. 수정 후 상세 페이지로 이동하면 그 상세 GET도
조회에 포함된다. 목록이나 수정 폼을 여는 것만으로는 증가하지 않는다. 조회수 변경도 Entity 수정이므로
Auditing의 `updatedAt`은 갱신된다.
