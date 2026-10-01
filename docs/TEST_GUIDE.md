# 9번·10번 · API 검증 및 통합 CRUD 점검 안내

이 문서는 `TEST.md`의 9번·10번을 실행하고 캡처할 수 있도록 작성한 안내다. 아래 상태 코드·JSON·조회수 설명은 **예상 확인 항목**이며, 이 문서만으로 실제 검증에 성공했다는 뜻은 아니다. 실행 결과와 사용한 DB는 `docs/verification/`의 기록에서 확인한다.

실제 완료 결과는 [RESULTS.md](verification/RESULTS.md)에 있다. 자동 통합 테스트 7개와 실제 API 요청
9개가 통과했으며, 브라우저로 확인한 범위와 실제 HTTP/SQL로 확인한 범위를 구분해서 기록했다.

문항 9의 반복 검증 도구는 `[수업 예제 외 추가]` `scripts/verify-api.ps1`이다.

```powershell
.\scripts\verify-api.ps1 -BaseUrl 'http://localhost:18080' -CaptureDatabase -DatabaseSchema board_exam_ch09
```

기본 실행은 HTTP만 사용하며, `-CaptureDatabase`를 붙이면 만든 글만 SELECT하여 저장 증거도 기록한다.
테스트에서 생성한 글만 삭제한다. 결과 HTML과 요청/응답 원본은 `docs/verification/`에 저장된다.

수업에서는 [project1의 NoticeRestController](https://github.com/jihyeok96-oss/project1/blob/main/backend/notice/src/main/java/com/oji/notice/controller/NoticeRestController.java)가 등록 201, 조회/수정 200, 삭제 204를 사용한다. JSON Content-Type 작성 방식은 [ref-front의 Axios 설정](https://github.com/jihyeok96-oss/ref-front/blob/master/src/api/index.js), API 오류의 Map 응답은 [ref-back의 ApiExceptionHandler](https://github.com/jihyeok96-oss/ref-back/blob/master/src/main/java/com/logic/project/controller/ApiExceptionHandler.java)을 참고했다. **[수업 예제 외 추가]** `.http` 요청 파일, 구체적인 통합 테스트, 검증 기록 및 캡처 절차는 평가문제를 확인하기 위한 보조 자료다.

## 9번·10번 실행 환경 준비

PowerShell에서 프로젝트 폴더로 이동한다. Java 21을 사용한다.

```powershell
Set-Location -LiteralPath 'C:\jh\spring\ch09\board'
java -version
```

`application.yml`의 기본 설정은 평가문제와 정확히 같은 PostgreSQL `localhost:5432/company`, 사용자 **`postgress`**, 비밀번호 **`1004`**다. 평가 환경에 해당 계정과 DB가 준비되어 있으면 기본 명령으로 실행한다.

```powershell
.\gradlew.bat bootRun
```

기본 화면은 `http://localhost:8080/boards`, API는 `http://localhost:8080/api/boards`다.

현재 로컬 DB 사용자는 사용자가 확인한 **`postgres`**다. 문제의 `postgress`와 철자가 다르므로 평가용 설정 파일을 바꾸지 않고 실행 인자로 덮어쓴다. Entity와 같은 구조의 테이블을 사용하는 환경에서 계정만 바꾸려면 다음 형태다.

```powershell
.\gradlew.bat bootRun --args="--spring.datasource.username=postgres"
```

현재 `company.public.board`에는 이전 수업 테이블이 존재한다. 그 테이블에는 `hits`가 없고, `content`는 NOT NULL이며, 시간 컬럼은 시간대 없는 타입이다. 이번 실검증은 이 테이블과 구분한 **`company.board_exam_ch09.board`**를 사용한다. 이 구분은 **[수업 예제 외 추가]** 검증 환경 설정이며 Entity의 테이블명 `board`는 그대로다.

DB 관리 도구에서 아래 SQL로 점검용 스키마를 준비한다. PostgreSQL CLI를 사용하는 경우 `psql -h localhost -p 5432 -U postgres -d company`로 접속해 실행할 수 있다.

```sql
-- [10번] 기존 public.board와 분리한 평가 점검용 스키마.
CREATE SCHEMA IF NOT EXISTS board_exam_ch09;
```

이번 로컬 실검증의 실행 명령은 다음과 같다. 비밀번호는 설정 파일의 `1004`를 사용한다. `currentSchema`로 점검 스키마를 지정하고 별도 포트 18080에서 실행한다.

```powershell
.\gradlew.bat bootRun --args="--server.port=18080 --spring.datasource.username=postgres --spring.datasource.url=jdbc:postgresql://localhost:5432/company?currentSchema=board_exam_ch09"
```

이 경우 화면은 `http://localhost:18080/boards`, API는 `http://localhost:18080/api/boards`다. `docs/board-api.http`의 `baseUrl`도 `http://localhost:18080`으로 변경한다. 이 설정으로 통과한 결과는 **별도 스키마에서 PostgreSQL을 검증한 결과**이며, `public.board`를 점검했다고 제출하면 안 된다.

## 9번 · Talend API Tester 요청과 캡처

Talend API Tester 또는 같은 역할의 HTTP 도구에서 요청을 순서대로 실행한다. `.http` 파일을 지원하는 도구에서는 `docs/board-api.http`를 사용할 수 있다. Talend에서는 `{{baseUrl}}` 대신 위 실행 환경의 주소를 입력하고, `{{boardId}}` 대신 POST 응답의 실제 id를 입력한다.

Body가 있는 POST/PUT은 JSON 또는 Raw JSON을 선택하고 Header에 `Content-Type: application/json`을 지정한다. 응답 확인을 위해 `Accept: application/json`도 사용할 수 있다. GET/DELETE는 Body가 없으며 Content-Type 헤더를 적어 두어도 된다. DELETE 성공은 204이므로 JSON 본문이 없어야 정상이다.

평가지 본문의 API 종류를 모두 확인하면서 답안 작성 요령의 **GET → POST → PUT → GET → DELETE** 순서로 캡처한다. POST 전에는 생성 id를 알 수 없으므로 먼저 목록을 확인하고, POST 후 얻은 id로 PUT과 단건 GET을 수행한다.

| 실행 순서 / 캡처 이름 예시 | Method · URL | Body | 예상 응답 / 확인 내용 |
| --- | --- | --- | --- |
| `09_01_GET_list.png` | GET `/api/boards` | 없음 | 200, JSON 배열. 등록 전 목록 확인. |
| `09_02_POST_create.png` | POST `/api/boards` | 아래 등록 JSON | 201, 생성된 Board JSON 및 Location. id 자동 생성, hits 0, 날짜 자동 생성. |
| `09_03_PUT_update.png` | PUT `/api/boards/{id}` | 아래 수정 JSON | 200, 변경된 입력값. id·createdAt 보존, PUT 자체로 hits 증가 없음. |
| `09_04_GET_detail.png` | GET `/api/boards/{id}` | 없음 | 200, 수정된 값과 이전 값보다 1 증가한 hits. |
| `09_05_DELETE.png` | DELETE `/api/boards/{id}` | 없음 | 204, 빈 본문. |
| `10_01_GET_deleted_404.png` | GET `/api/boards/{삭제한 id}` | 없음 | 404, status/message 오류 JSON. |
| `10_02_bad_JSON_400.png` | POST `/api/boards` | 일부러 문법을 잘못 쓴 JSON | 400, JSON 요청 형식 안내. |
| `10_03_blank_title_400.png` | POST `/api/boards` | title이 공백인 JSON | 400, 필수값 오류. DB에 새 글이 생성되지 않음. |

각 캡처에는 요청 URL·Method·Headers·Body와 응답 상태 코드·응답 Body가 보이게 한다. 요청 입력 화면만 캡처하면 응답 검증 자료가 부족하다. POST 응답의 id·createdAt을 기록하고 다음 요청의 URL 및 수정 전후 비교에 사용한다. 수정/삭제는 이번 점검에서 만든 글을 대상으로 한다.

### 9번-02 · POST 등록 JSON

```json
{
  "title": "9번 API 평가 게시글",
  "content": "POST 등록 내용을 확인합니다.",
  "author": "평가 작성자"
}
```

응답은 DTO를 거치지 않은 Board Entity다. 필드는 `id`, `title`, `content`, `author`, `hits`, `createdAt`, `updatedAt`이며, id와 날짜는 서버에서 결정한다. 이 필드들을 요청 Body에 작성해 수정하려고 하지 않는다.

### 9번-03 · PUT 수정 JSON

```json
{
  "title": "9번 API 수정 게시글",
  "content": "PUT으로 변경한 내용을 확인합니다.",
  "author": "수정 작성자"
}
```

PUT URL의 id는 POST 응답에서 복사한다. 기존 id·createdAt은 유지되고, title/content/author가 변경되어야 한다. `updatedAt`은 수정 시각으로 자동 관리되므로 요청 Body에 보내지 않는다. 바로 이어지는 단건 GET은 조회수를 1 증가시킨다.

### 10번 오류 요청

삭제한 id를 GET으로 재조회하면 아래 형태의 오류 JSON과 404를 확인한다. 숫자와 문구는 실제 요청의 id에 따라 달라진다.

```json
{
  "status": 404,
  "message": "해당 번호의 게시글을 찾을 수 없습니다."
}
```

위 JSON은 응답 구조 설명용 예시다. 실제 코드의 message에는 조회한 숫자 id가 들어간다. 잘못된 JSON 문법 요청은 `docs/board-api.http`의 `[10번-02]`처럼 마지막 `}`를 누락해 실행한다. 문법은 올바르지만 필수값을 빠뜨리는 요청도 별도로 확인한다.

```json
{
  "title": "   ",
  "content": "필수값 검증을 확인합니다.",
  "author": "평가 작성자"
}
```

`GET /api/boards/abc`는 숫자 Long으로 바꿀 수 없는 id이므로 400과 숫자 id 안내를 확인한다. 없는 숫자 id의 404와 형식이 틀린 id의 400을 구분한다.

## 10번 · 브라우저 전체 흐름

브라우저에서 `/boards`를 열고 아래 순서대로 진행한다. 화면의 링크와 버튼은 Thymeleaf `th:href/th:action`, 폼은 `th:object/th:field`, 목록 반복은 `th:each`로 연결된다. 기본 UI 흐름은 [spring-practice-ans의 BoardController](https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/java/com/company/myapp/controller/BoardController.java), 입력 오류 시 폼 재표시는 [mvclab의 MemberController](https://github.com/jihyeok96-oss/mvclab/blob/main/src/main/java/com/example/mvclab/web/MemberController.java)를 참고했다.

1. **등록**: 새 글 작성 버튼을 누르고 제목·내용·작성자를 입력한 후 등록한다. `POST /boards` 이후 목록으로 이동하는지 확인한다. 등록 전후 글 수와 자동 생성 id·날짜·초기 hits 0을 기록한다.
2. **목록**: 목록에 id/title/author/hits/createdAt이 표시되는지 확인한다. 제목 키워드 검색을 실행하고 제목 포함 결과가 생성시각 내림차순으로 정렬되는지 확인한다.
3. **상세**: 생성한 제목을 눌러 상세 화면으로 이동한다. `GET /boards/{id}`마다 hits가 1 증가하며 내용과 작성자가 보이는지 확인한다.
4. **수정**: 수정 버튼으로 `GET /boards/{id}/edit`에 들어간다. 이 폼 진입 자체는 조회수를 증가시키지 않는다. title/content/author를 변경하고 저장한다. 같은 id·createdAt이 유지되는지 확인한다.
5. **목록 재확인**: 목록으로 이동해 수정한 제목·작성자를 확인한다. REST 목록에서도 같은 id에 같은 수정 내용이 나타나는지 확인한다.
6. **삭제**: 이번 점검에서 만든 글을 삭제한다. `POST /boards/{id}/delete` 이후 목록으로 이동하며 글이 사라지는지 확인한다. REST 목록에도 없어야 하고 단건 조회는 404여야 한다.

**조회수 비교 시 주의할 실제 동작:** 수정 성공은 상세 화면으로 리다이렉트한다. 브라우저가 뒤따르는 `GET /boards/{id}`를 요청하므로 수정 후 표시되는 hits는 1 증가한다. 이는 수정 메서드가 올린 값이 아니라 상세 조회의 결과다. REST 단건 GET도 조회수를 1 올리므로 화면과 API를 번갈아 조회하면 그 횟수만큼 증가한다. 목록·검색·수정 폼 진입·PUT 자체는 조회수를 올리지 않는다.

**시간 비교 시 실제 동작:** hits도 JPA가 저장하는 Entity의 변경값이다. 따라서 상세 GET으로 hits가 바뀌면 `@LastModifiedDate`의 updatedAt도 갱신된다. updatedAt이 글 내용 수정 때만 변한다고 가정하지 않는다. createdAt은 수정·조회수 증가 후에도 유지된다.

브라우저 오류도 확인한다. 삭제한 id의 `/boards/{id}`는 404 오류 화면을, `/boards/abc`는 400 오류 화면을 보여야 한다. 등록 폼에서 필수값을 비운 요청은 400 상태와 오류 메시지·입력 폼을 확인한다. 수정 폼에 오류가 있을 때도 form action이 원래 `/boards/{id}`를 유지해야 한다.

## 10번 · PostgreSQL에 실제 반영됐는지 확인

이번 로컬 실검증의 데이터 위치는 **`company.board_exam_ch09.board`**다. 브라우저/API로 만든 id를 아래 SQL 결과와 비교한다. 등록 후 값이 생기고, 상세 조회 후 hits가 증가하며, 수정 후 같은 id·createdAt에 새 값이 들어가고, 삭제 후 행이 사라져야 한다.

```sql
-- [10번] 이번 점검의 실제 PostgreSQL 스키마와 테이블을 명시한다.
SELECT id, title, author, hits, created_at, updated_at
FROM board_exam_ch09.board
ORDER BY created_at DESC;
```

시간 컬럼을 확인할 때 DB 세션의 표시 시간대가 다를 수 있다. 같은 세션에서 아래 설정으로 서울 기준 표시를 맞춘다. `timestamptz`는 시각을 저장하고 조회 세션의 시간대로 표시하며 `Asia/Seoul`이라는 지역 이름 자체를 컬럼 값에 보관하는 것은 아니다.

```sql
-- [2·3·10번] 이번 SQL 조회 세션에만 적용되는 시간 표시 설정.
SET TIME ZONE 'Asia/Seoul';
```

평가 환경에서 기본 public 스키마를 사용하는 경우에는 실제 접속 설정에 맞는 `board` 테이블을 조회한다. 이번 별도 스키마 기록을 기본 `public.board`의 결과라고 적지 않는다. 실패한 요청 뒤에 위 SQL로 행 수와 값을 확인하면 불완전한 등록/수정이 저장되지 않았는지도 확인할 수 있다.

## 10번 · 발생 가능한 문제 두 가지와 해결 방법

| 문제 | 원인 / 확인 방법 | 해결 방법 |
| --- | --- | --- |
| **1. DB 인증 실패 또는 접속 실패** | 문제의 계정은 `postgress`, 로컬 실제 계정은 `postgres`다. 사용자 이름 오타, 비밀번호 불일치, PostgreSQL 미실행, 포트/DB 불일치가 있으면 `password authentication failed` 또는 연결 오류가 난다. `psql` 등으로 localhost:5432/company 접속과 실제 사용자명을 확인한다. | 제출용 `application.yml`은 문제의 `postgress / 1004`를 유지한다. 로컬은 `--spring.datasource.username=postgres`로 실제 계정을 실행 시 지정한다. PostgreSQL 실행·5432·company 존재도 확인한다. 현재 이전 수업 테이블과 충돌을 피하는 실검증은 `currentSchema=board_exam_ch09`를 함께 지정한다. |
| **2. 생성/수정 시간이 null이거나 시간 기준이 연결되지 않음** | `@CreatedDate/@LastModifiedDate`만 선언하고 `@EnableJpaAuditing` 또는 `@EntityListeners`가 빠지면 날짜가 채워지지 않을 수 있다. Clock/DateTimeProvider를 선언했어도 `dateTimeProviderRef`가 빠지면 지정한 서울 시간 공급자를 사용하지 않는다. | `TimeConfig`의 `@EnableJpaAuditing(dateTimeProviderRef = "seoulDateTimeProvider")`, 서울 Clock Bean, DateTimeProvider와 `Board`의 `@EntityListeners(AuditingEntityListener.class)`, `@CreatedDate/@LastModifiedDate`를 함께 확인한다. 생성시각은 `updatable = false`로 유지한다. |

시간 Configuration은 [java-springboot-test의 TimeConfig](https://github.com/jihyeok96-oss/java-springboot-test/blob/main/src/main/java/com/example/board/config/TimeConfig.java), 날짜 어노테이션은 [spring-practice-ans의 Board Entity](https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/java/com/company/myapp/entity/Board.java)를 참고했다. listener와 provider 이름의 실제 연결은 원본에서 빠진 부분을 보완했다.

추가 입력 오류는 JSON Body를 올바르게 작성하고 `Content-Type: application/json`을 지정해 해결한다. JSON 문법이 틀리면 400, 올바른 JSON에서도 title/author가 빈 문자열 또는 공백이면 400이다. 화면 폼은 `BindingResult`의 오류를 표시하므로 필수값을 채워 다시 제출한다.

## 9번·10번 자동 테스트와 제출 자료 구분

```powershell
.\gradlew.bat test
```

`src/test/java/com/example/board/BoardApplicationTests.java`는 MockMvc로 MVC/API 흐름과 오류를 점검한다. **[수업 예제 외 추가]** 테스트 전용 H2 메모리 DB를 사용하므로 기존 `company` 데이터에 영향을 주지 않는다. 자동 테스트 통과는 컨트롤러·서비스·템플릿·저장 로직을 검증하지만 실제 PostgreSQL 접속 성공이나 Talend/브라우저 화면 캡처를 대신하지 않는다.

Gradle 테스트 결과는 `build/reports/tests/test/index.html`, 이번 실행의 추가 기록은 `docs/verification/`을 확인한다. 보고서에 어떤 서버 포트, 계정, DB 종류 및 스키마를 사용했는지 함께 적는다. 캡처 파일명에는 `09_` 또는 `10_`과 동작·상태를 넣고 요청/응답 정보를 보이게 한다. 실행하지 않은 예상 JSON이나 안내 표를 실제 성공 결과로 제출하지 않는다.
