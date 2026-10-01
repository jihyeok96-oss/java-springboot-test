# 9번·10번 실제 검증 결과

검증 날짜는 **2026-10-01, Asia/Seoul**이다. 아래 결과는 실제 실행 기록이다.

## 검증 환경

- Spring Boot 4.1.1, Java 21.0.12, PostgreSQL 18.6.
- 실제 HTTP 서버: `http://localhost:18080`.
- 실제 DB: `company`, 사용자 `postgres`, 테이블 `board_exam_ch09.board`.
- 제출용 `application.yml`은 문제의 `postgress / 1004`를 유지했다. 실제 사용자는 사용자가 확인한
  `postgres`이므로 실행 인자로 덮어썼다. `postgress` 계정은 현재 환경에서 인증되지 않는다.
- 기존 `public.board`에는 다른 수업 테이블이 있으므로 변경하지 않았다. 별도 스키마에서 검증했다.
- H2 자동 테스트와 실제 PostgreSQL 검증은 아래에서 구분한다.

## 1~8번·10번 자동 통합 테스트

실행 명령: `gradlew.bat test bootJar --offline --no-daemon`.

**BUILD SUCCESSFUL**, 테스트 **7개 / 실패 0개 / 오류 0개**. 서울 시간 표시 보완 후 다시 실행해 통과했다.
테스트는 `application-test.yml`의 H2를 사용하며 실제 Spring Controller, Service, JPA를 연결한다.
각 테스트 트랜잭션은 끝에서 롤백한다.

| 테스트 메서드 | 실제 확인 항목 |
| --- | --- |
| `contextLoads` | 전체 설정과 서울 Clock Bean |
| `auditingAndTitleSearchUseClockAndLatestOrder` | 자동 시간, hits 초기값, 제목 포함 검색, 최신순 |
| `restCrudPreservesIdentityAndIncrementsHits` | REST CRUD, 생성 Location, PK/생성일 보존, 조회수 증가, 임의 서버 필드 입력 방지 |
| `mvcCrudAndRestShareSameData` | 화면 경로/템플릿, 등록·수정·삭제 리다이렉트, MVC/API 데이터 공유 |
| `invalidRequestsAndUnknownIdsAreHandled` | 필수값·잘못된 JSON·숫자가 아닌 id의 400 및 없는 id의 404 |
| `invalidEditKeepsActionAndOriginalEntity` | 수정 검증 실패 시 수정 URL과 기존 데이터 보존 |
| `userContentIsEscapedInHtml` | 사용자 HTML을 이스케이프해 표시 |

Gradle 원본 결과: `build/reports/tests/test/index.html`,
`build/test-results/test/TEST-com.example.board.BoardApplicationTests.xml`.
`build/`는 생성 파일이므로 Git에는 포함하지 않는다.

## 9번 실제 HTTP API 검증

실행 명령:

```powershell
.\scripts\verify-api.ps1 -BaseUrl 'http://localhost:18080' -CaptureDatabase -DatabaseSchema board_exam_ch09
```

**9개 요청 모두 통과**. [실제 요청/응답 보고서](api-report.html),
[원본 결과 JSON](api/summary.json)에는 URL·Method·Header·Body·상태 코드·응답 JSON을 기록했다.
Talend API Tester 대신 같은 역할의 PowerShell HTTP 도구를 사용했다.
**[수업 예제 외 추가]** 이 도구와 보고서 생성은 평가 검증용 보조 구현이다.

| 순서 | 실제 요청 | 실제 응답 | 캡처 기록 |
| --- | --- | --- | --- |
| 1 | GET `/api/boards` | 200, 목록 JSON | [목록](api/01-get-list.txt) |
| 2 | POST `/api/boards` | 201, id=2, hits=0, Location | [등록](api/02-post-create.txt) |
| 3 | PUT `/api/boards/2` | 200, 입력값 수정, id/createdAt/hits 보존 | [수정](api/03-put-update.txt) |
| 4 | GET `/api/boards/2` | 200, hits=1, 서울 오프셋 +09:00 | [상세](api/04-get-detail.txt) |
| 5 | DELETE `/api/boards/2` | 204, 빈 본문 | [삭제](api/05-delete-board.txt) |
| 6 | GET `/api/boards/2` | 404, status/message | [없는 id](api/06-get-deleted-404.txt) |
| 7 | POST 잘못된 JSON | 400, JSON 형식 안내 | [JSON 오류](api/07-malformed-json-400.txt) |
| 8 | POST 빈 제목 | 400, 필수값 오류 | [필수값 오류](api/08-blank-title-400.txt) |
| 9 | GET 숫자가 아닌 id | 400, id 형식 안내 | [id 오류](api/09-invalid-id-400.txt) |

등록한 id=2가 실제 DB에 저장된 상태를 DELETE 전에 SELECT로 확인했다.
[PostgreSQL 원본 조회 결과](api/10-postgresql-insert-evidence.txt)에는 입력 항목, hits=1, 자동 시간 값이 있다.
PostgreSQL의 `timestamp(6)`은 마이크로초 단위이므로 최초 Java 시간의 나노초가 DB 저장 시 반올림될 수 있다.
API 비교는 1마이크로초 이내 표현 차이를 허용하며, 수정 SQL은 `created_at`을 갱신하지 않는다.

## 6·7·10번 실제 화면/DB 확인

브라우저에서 id=1을 등록하고 목록 → 상세 → 수정 폼 → 수정 완료 → 목록을 직접 확인했다.
아래 이미지는 실제 내장 브라우저 캡처이며, 보고서를 위해 화면을 새로 그린 이미지가 아니다.

| 단계 | 화면 캡처 |
| --- | --- |
| 등록 폼 입력 | [10-01-form.jpg](10-01-form.jpg) |
| 등록 후 목록, hits=0 | [10-02-list.jpg](10-02-list.jpg) |
| 상세, hits=1 | [10-03-detail.jpg](10-03-detail.jpg) |
| 기존 글 수정 폼 | [10-04-edit-form.jpg](10-04-edit-form.jpg) |
| 수정 후 상세, hits=2 | [10-05-updated-detail.jpg](10-05-updated-detail.jpg) |
| 수정 후 목록 | [10-06-updated-list.jpg](10-06-updated-list.jpg) |

수정 전후 id=1의 `created_at`은 **2026-10-01 09:51:18.370792+09**로 동일했다.
상세 화면은 hits=1, 수정 완료 후 리다이렉트된 상세 GET은 hits=2였다. 수정 폼을 여는 것만으로는 증가하지 않았다.

- [수정 전 DB 조회](10-db-before-update.txt) / [수정 후 DB 조회](10-db-after-update.txt)
- [수정 전 같은 데이터의 API 목록](10-api-mvc-shared-before-update.json)
- [수정 후 같은 데이터의 API 목록](10-api-mvc-shared-after-update.json)

내장 브라우저의 JavaScript 삭제 확인창 단계에서 자동화 도구가 응답하지 않았다.
따라서 확인창 수락과 삭제 후 화면을 **브라우저에서 끝까지 검증했다고 주장하지 않는다**.
이후 실제 HTTP 클라이언트에서 별도 id=3을 등록해 MVC 삭제 POST와 리다이렉트를 확인했다.
[실제 MVC HTTP 결과](10-delete-http.txt)는 등록 302 → 삭제 302 → 삭제 후 상세 404를 기록한다.
[삭제 후 MVC 404 HTML](10-mvc-deleted-404.html)도 저장했다.

최종 [DB 확인](10-db-after-delete.txt)에서 테스트 id=1,2,3의 남은 행 수는 **0**이었다.
기존 다른 수업 데이터와 `public.board`는 삭제하지 않았다.

## 10번 문제 상황과 해결

1. **DB 사용자명 불일치 / 비밀번호 인증 실패**: 평가문항은 `postgress`, 실제 사용자는 `postgres`였다.
   제출용 설정은 유지하고 실제 검증은 실행 인자로 사용자명을 덮어썼다.
   기존 게시판 테이블 구조도 달라 별도 스키마를 사용했다. 실행 안내는 [TEST_GUIDE.md](../TEST_GUIDE.md)에 있다.
2. **시간 자동 관리 또는 서울 시간 표시 오류**: Entity의 Auditing 리스너와 날짜 어노테이션,
   `@EnableJpaAuditing(dateTimeProviderRef = "seoulDateTimeProvider")`를 함께 연결했다.
   실제 PostgreSQL에서 시간대를 UTC로 반환하는 현상을 확인해, getter의 `withZoneSameInstant`로
   동일한 시점을 서울 시간으로 표현하게 했다. JSON은 +09:00, 화면은 한국 시간으로 확인됐다.

없는 id와 잘못된 요청은 위 표의 실제 404/400 응답으로 별도 점검했다.

로컬 HTML 보고서를 `file:` 주소로 브라우저에 여는 것은 브라우저 URL 정책의 자동 승인 검토에서 차단됐다.
보고서 및 모든 캡처/원본 응답은 프로젝트 파일로 제공한다.
