<#
9번. verify-api.ps1 - Talend API Tester와 동등한 HTTP 요청 도구로 실제 REST 응답을 저장한다.
10번. 등록한 데이터의 수정/조회수/삭제와 없는 id, 잘못된 JSON, 빈 제목, 숫자가 아닌 id를 확인한다.

수업 참고: 아래 REST 예제의 경로/HTTP 메서드/201·204 상태 코드/입력 검증 방식을 검증 대상으로 삼는다.
https://github.com/jihyeok96-oss/project1/blob/main/backend/notice/src/main/java/com/oji/notice/controller/NoticeRestController.java
https://github.com/jihyeok96-oss/ref-back/blob/master/src/main/java/com/logic/project/api/QnaRestController.java
https://github.com/jihyeok96-oss/mobility_project_backend/blob/main/src/main/java/com/kim/mobility/controller/GlobalExceptionHandler.java

[수업 예제 외 추가] 수업 저장소에 동일한 PowerShell API 검증 스크립트가 없으므로 .NET HttpClient로
요청/응답을 기록한다. PowerShell 5.1/7에서 실행되며 request/response를 txt, JSON, HTML로 보관한다.
성공 여부는 실제 상태 코드와 반환된 Entity 값으로 판단한다. 실행하지 않은 항목은 성공으로 쓰지 않는다.

실행 예: .\scripts\verify-api.ps1
다른 포트 예: .\scripts\verify-api.ps1 -BaseUrl http://localhost:18080
DB 증거 선택: .\scripts\verify-api.ps1 -BaseUrl http://localhost:18080 -CaptureDatabase -DatabaseSchema board_exam_ch09
DB 캡처는 자신의 id를 SELECT하는 읽기 작업이며 기본 실행은 HTTP 검증만 한다.
#>
[CmdletBinding()]
param(
    [string] $BaseUrl = 'http://localhost:8080',
    [string] $OutputDirectory = (Join-Path (Split-Path -Parent $PSScriptRoot) 'docs\verification\api'),
    [switch] $CaptureDatabase,
    [string] $DatabaseSchema = 'public',
    [string] $DatabaseUser = 'postgres',
    [string] $PsqlPath = 'C:\Program Files\PostgreSQL\18\bin\psql.exe'
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http
$BaseUrl = $BaseUrl.TrimEnd('/')
$OutputDirectory = [System.IO.Path]::GetFullPath($OutputDirectory)
$reportPath = Join-Path (Split-Path -Parent $OutputDirectory) 'api-report.html'
[void] (New-Item -ItemType Directory -Path $OutputDirectory -Force)
$utf8 = New-Object System.Text.UTF8Encoding($false)
$client = New-Object System.Net.Http.HttpClient
$client.Timeout = [TimeSpan]::FromSeconds(20)
$records = New-Object 'System.Collections.Generic.List[object]'
$runStartedAt = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(9)).ToString('o')
$createdId = $null
$passed = $false
$failure = $null
$databaseEvidence = $null

# 9번: 요청 헤더와 응답 헤더는 HttpClient가 실제 사용한 값을 수집한다.
function Get-RecordedHeaders {
    param($Headers, $ContentHeaders)
    $values = [ordered]@{}
    foreach ($header in $Headers) {
        $values[$header.Key] = [string]::Join(', ', [string[]] $header.Value)
    }
    if ($null -ne $ContentHeaders) {
        foreach ($header in $ContentHeaders) {
            $values[$header.Key] = [string]::Join(', ', [string[]] $header.Value)
        }
    }
    return $values
}

function Invoke-RecordedRequest {
    param([int] $Step, [string] $Name, [string] $Method, [string] $Path,
          [int] $ExpectedStatus, [string] $Body = $null)
    $request = New-Object System.Net.Http.HttpRequestMessage
    $request.Method = New-Object System.Net.Http.HttpMethod($Method)
    $request.RequestUri = [Uri] ($BaseUrl + $Path)
    $request.Headers.Add('Accept', 'application/json')
    if ($null -ne $Body -and $Body.Length -gt 0) {
        # 한글 제목/내용/작성자는 UTF-8로 보내고 Content-Type도 application/json으로 명시한다.
        $request.Content = New-Object System.Net.Http.StringContent($Body, [Text.Encoding]::UTF8, 'application/json')
    }
    $response = $null
    $timer = [Diagnostics.Stopwatch]::StartNew()
    try {
        $response = $client.SendAsync($request).GetAwaiter().GetResult()
        $responseBody = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        $record = [pscustomobject]@{
            question = $(if ($Step -le 5) { '9번 REST CRUD' } else { '10번 오류 대응' })
            step = $Step
            name = $Name
            time = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(9)).ToString('o')
            method = $Method
            url = $request.RequestUri.AbsoluteUri
            requestHeaders = (Get-RecordedHeaders $request.Headers $request.Content.Headers)
            requestBody = $Body
            expectedStatus = $ExpectedStatus
            responseStatus = [int] $response.StatusCode
            responseReason = $response.ReasonPhrase
            responseHeaders = (Get-RecordedHeaders $response.Headers $response.Content.Headers)
            responseBody = $responseBody
            elapsedMs = $timer.ElapsedMilliseconds
            checks = (New-Object 'System.Collections.Generic.List[string]')
            passed = $false
            file = (Join-Path $OutputDirectory ('{0:00}-{1}.txt' -f $Step, $Name))
        }
        $records.Add($record)
        return $record
    } finally {
        $timer.Stop()
        if ($null -ne $response) { $response.Dispose() }
        $request.Dispose()
    }
}

# 9·10번: 기대값과 실제값을 비교하며 실패 시 이후 정상 흐름은 중단한다.
function Assert-Record {
    param($Record, [bool] $Condition, [string] $Description)
    if (-not $Condition) {
        $Record.checks.Add('FAIL: ' + $Description)
        throw ('{0:00}-{1}: {2}' -f $Record.step, $Record.name, $Description)
    }
    $Record.checks.Add('PASS: ' + $Description)
}

function Assert-Status {
    param($Record)
    Assert-Record $Record ($Record.responseStatus -eq $Record.expectedStatus) (
        'HTTP {0} 기대 / 실제 {1}' -f $Record.expectedStatus, $Record.responseStatus)
}

# [수업 예제 외 추가] PowerShell이 ISO 시각을 로컬 DateTime으로 자동 변환하면 +09:00 원문이
# 사라질 수 있다. 가능한 버전에서는 DateKind String을 사용하고 5.1에서는 JSON 원문으로 복원한다.
function Convert-RecordedJson {
    param([string] $Json)
    if ((Get-Command ConvertFrom-Json).Parameters.ContainsKey('DateKind')) {
        return ConvertFrom-Json -InputObject $Json -DateKind String
    }
    $value = ConvertFrom-Json -InputObject $Json
    foreach ($field in @('createdAt', 'updatedAt')) {
        $match = [Regex]::Match($Json, '"' + $field + '"\s*:\s*"([^"]+)"')
        if ($match.Success) { $value.$field = $match.Groups[1].Value }
    }
    return $value
}

# [수업 예제 외 추가] PostgreSQL의 마이크로초 반올림과 Java 나노초 표현 차이를 고려한다.
# 같은 시각 비교에서 1마이크로초(10 ticks) 이하만 허용한다. POST는 Java 나노초 표현,
# 이후 DB 응답은 PostgreSQL 마이크로초 표현이며 DateTimeOffset은 100ns 단위로 비교한다.
function Convert-RecordedTime {
    param([string] $Value)
    $normalized = $Value -replace '(\.\d{7})\d+', '$1' -replace '\[.*\]$', ''
    return [DateTimeOffset]::Parse($normalized, [Globalization.CultureInfo]::InvariantCulture)
}

function Save-DatabaseEvidence {
    param([long] $Id)
    if ($DatabaseSchema -notmatch '^[A-Za-z_][A-Za-z0-9_]*$') {
        throw 'DatabaseSchema는 영문자/숫자/밑줄만 사용할 수 있습니다.'
    }
    if (-not (Test-Path -LiteralPath $PsqlPath)) { throw ('psql이 없습니다: ' + $PsqlPath) }
    # id는 이 실행의 POST 응답에서만 얻는다. 기존 게시글의 삭제/수정과 DB 직접 쓰기는 하지 않는다.
    $sql = 'SELECT id, title, content, author, hits, created_at, updated_at FROM ' +
        $DatabaseSchema + '.board WHERE id=' + $Id + ';'
    $previousPassword = $env:PGPASSWORD
    try {
        # 1번에서 제공된 비밀번호를 현재 프로세스에만 설정하고 끝나면 이전 값으로 복원한다.
        $env:PGPASSWORD = '1004'
        $output = & $PsqlPath -h localhost -p 5432 -U $DatabaseUser -d company -v ON_ERROR_STOP=1 -c $sql 2>&1
        $exitCode = $LASTEXITCODE
        $text = '10번 PostgreSQL 실제 저장 확인' + [Environment]::NewLine +
            'Database: company / Schema: ' + $DatabaseSchema + [Environment]::NewLine +
            'SQL: ' + $sql + [Environment]::NewLine + [Environment]::NewLine +
            ($output -join [Environment]::NewLine)
        $file = Join-Path $OutputDirectory '10-postgresql-insert-evidence.txt'
        [IO.File]::WriteAllText($file, $text, $utf8)
        if ($exitCode -ne 0) { throw ('PostgreSQL SELECT 실패. 로그: ' + $file) }
        return [pscustomobject]@{ executed = $true; passed = $true; id = $Id; file = $file; sql = $sql }
    } finally {
        $env:PGPASSWORD = $previousPassword
    }
}

function Convert-HeadersToText {
    param($Headers)
    return (($Headers.GetEnumerator() | ForEach-Object { $_.Key + ': ' + $_.Value }) -join [Environment]::NewLine)
}

# 9번 제출 자료: 실제 요청과 응답을 개별 txt/summary.json/HTML에 동일하게 기록한다.
function Save-Reports {
    $cards = New-Object 'System.Collections.Generic.List[string]'
    foreach ($record in $records) {
        $requestBody = $(if ([string]::IsNullOrEmpty($record.requestBody)) { '(본문 없음)' } else { $record.requestBody })
        $responseBody = $(if ([string]::IsNullOrEmpty($record.responseBody)) { '(본문 없음)' } else { $record.responseBody })
        $log = $record.question + ' / ' + $record.name + [Environment]::NewLine +
            '실제 실행 시각(Asia/Seoul): ' + $record.time + [Environment]::NewLine +
            'REQUEST' + [Environment]::NewLine + $record.method + ' ' + $record.url + [Environment]::NewLine +
            (Convert-HeadersToText $record.requestHeaders) + [Environment]::NewLine + [Environment]::NewLine +
            $requestBody + [Environment]::NewLine + [Environment]::NewLine +
            'RESPONSE' + [Environment]::NewLine +
            'HTTP ' + $record.responseStatus + ' ' + $record.responseReason + [Environment]::NewLine +
            (Convert-HeadersToText $record.responseHeaders) + [Environment]::NewLine + [Environment]::NewLine +
            $responseBody + [Environment]::NewLine + [Environment]::NewLine +
            'VERIFY' + [Environment]::NewLine + ($record.checks -join [Environment]::NewLine)
        [IO.File]::WriteAllText($record.file, $log, $utf8)
        # [수업 예제 외 추가] 응답 JSON과 제목을 HTML로 해석하지 않도록 반드시 이스케이프한다.
        $escaped = [Net.WebUtility]::HtmlEncode($log)
        $label = [Net.WebUtility]::HtmlEncode(('{0:00}. {1} — 실제 HTTP {2}' -f $record.step, $record.name, $record.responseStatus))
        $cards.Add('<section><h2>' + $label + '</h2><pre>' + $escaped + '</pre></section>')
    }
    $summary = [ordered]@{
        question = '9번 HTTP API 검증 / 10번 오류 및 실제 저장 검증'
        startedAt = $runStartedAt
        finishedAt = [DateTimeOffset]::UtcNow.ToOffset([TimeSpan]::FromHours(9)).ToString('o')
        baseUrl = $BaseUrl
        passed = $passed
        failure = $failure
        requestCount = $records.Count
        databaseEvidence = $databaseEvidence
        requests = @($records.ToArray())
        report = $reportPath
    }
    [IO.File]::WriteAllText((Join-Path $OutputDirectory 'summary.json'), ($summary | ConvertTo-Json -Depth 12), $utf8)
    $outcome = $(if ($passed) { '실제 실행 결과: PASS' } else { '실제 실행 결과: FAIL' })
    $html = '<!DOCTYPE html><html lang="ko"><head><meta charset="utf-8"><title>9·10번 실제 API 검증</title>' +
        '<style>body{font-family:Arial,sans-serif;max-width:1100px;margin:32px auto;padding:0 16px;color:#172033;background:#f6f8fb}' +
        'section{background:white;border:1px solid #d9e1ec;border-radius:8px;padding:20px;margin:20px 0}' +
        'h1{font-size:26px}h2{font-size:18px}pre{white-space:pre-wrap;overflow-wrap:anywhere;font-family:Consolas,monospace;font-size:13px;line-height:1.5}</style>' +
        '</head><body><h1>9·10번 실제 REST API 검증</h1><p>' + $outcome + '</p><p>' +
        [Net.WebUtility]::HtmlEncode('실행 주소: ' + $BaseUrl + ' / 시각: ' + $runStartedAt) +
        '</p><p>PowerShell HttpClient로 전송한 실제 Method, URL, Header, Body, 상태 코드와 JSON 응답입니다.</p>' +
        '<p>이 실행에서 생성한 게시글만 수정·삭제했습니다. 성공 표시가 없는 항목은 검증 완료를 의미하지 않습니다.</p>' +
        '<p>생성일시 보존은 POST의 Java 나노초와 DB의 마이크로초 정밀도 차이 때문에 실제 시점 차이를 1마이크로초(10 ticks) 이하로 비교합니다.</p>' +
        $(if ($failure) { '<p>' + [Net.WebUtility]::HtmlEncode('실패: ' + $failure) + '</p>' } else { '' }) +
        ($cards -join [Environment]::NewLine) + '</body></html>'
    [IO.File]::WriteAllText($reportPath, $html, $utf8)
}

try {
    # 9번 ① GET 목록 → ② POST 등록 → ③ PUT 수정 → ④ GET 단건 → ⑤ DELETE 삭제.
    $record = Invoke-RecordedRequest 1 'get-list' 'GET' '/api/boards' 200
    Assert-Status $record
    Assert-Record $record ($record.responseBody.TrimStart().StartsWith('[')) '목록 응답은 JSON 배열'
    $record.passed = $true

    $title = '9번 API 검증 - ' + [Guid]::NewGuid().ToString('N')
    $payload = @{ title = $title; content = '실제 POST 등록 검증'; author = 'API 검증' } | ConvertTo-Json -Compress
    $record = Invoke-RecordedRequest 2 'post-create' 'POST' '/api/boards' 201 $payload
    $created = Convert-RecordedJson $record.responseBody
    # 응답에 id가 있으면 즉시 보관하여 이후 검증 실패 시에도 자신의 글만 정리한다.
    if ($null -ne $created.id -and [long] $created.id -gt 0) { $createdId = [long] $created.id }
    Assert-Status $record
    Assert-Record $record ($null -ne $createdId) '서버가 Long id를 자동 생성'
    Assert-Record $record ($created.title -eq $title -and $created.author -eq 'API 검증') '한글 요청값이 UTF-8 JSON으로 저장'
    Assert-Record $record ([long] $created.hits -eq 0) '등록 조회수는 0'
    Assert-Record $record ($created.createdAt -match '\+09:00(?:\[Asia/Seoul\])?$') '생성일시는 서울 시간 +09:00'
    Assert-Record $record ($record.responseHeaders['Location'] -eq '/api/boards/' + $createdId) '201 Location에 생성된 API 주소 포함'
    $record.passed = $true
    Write-Host ('Created disposable test row id=' + $createdId)

    $updatedTitle = $title + ' 수정'
    $payload = @{ title = $updatedTitle; content = '실제 PUT 수정 검증'; author = 'API 수정 검증' } | ConvertTo-Json -Compress
    $record = Invoke-RecordedRequest 3 'put-update' 'PUT' ('/api/boards/' + $createdId) 200 $payload
    Assert-Status $record
    $updated = Convert-RecordedJson $record.responseBody
    Assert-Record $record ([long] $updated.id -eq $createdId) '수정 시 기존 id 보존'
    Assert-Record $record ($updated.title -eq $updatedTitle -and $updated.content -eq '실제 PUT 수정 검증' -and $updated.author -eq 'API 수정 검증') 'title/content/author 수정 반영'
    Assert-Record $record ([long] $updated.hits -eq [long] $created.hits) '수정 시 조회수 보존'
    $createdTime = Convert-RecordedTime $created.createdAt
    $updatedCreatedTime = Convert-RecordedTime $updated.createdAt
    Assert-Record $record (($createdTime - $updatedCreatedTime).Duration().Ticks -le 10) '수정 시 기존 생성일시 보존(정밀도 오차 1마이크로초 이하)'
    $record.passed = $true

    $record = Invoke-RecordedRequest 4 'get-detail' 'GET' ('/api/boards/' + $createdId) 200
    Assert-Status $record
    $detail = Convert-RecordedJson $record.responseBody
    Assert-Record $record ([long] $detail.id -eq $createdId -and $detail.title -eq $updatedTitle) '상세에서 수정 결과 확인'
    Assert-Record $record ([long] $detail.hits -eq 1) '단건 조회로 조회수가 정확히 1 증가'
    Assert-Record $record ($detail.createdAt -match '\+09:00(?:\[Asia/Seoul\])?$' -and $detail.updatedAt -match '\+09:00(?:\[Asia/Seoul\])?$') 'DB 재조회 후 생성/수정일시도 +09:00'
    $record.passed = $true
    if ($CaptureDatabase) { $databaseEvidence = Save-DatabaseEvidence $createdId }

    $deletedId = $createdId
    $record = Invoke-RecordedRequest 5 'delete-board' 'DELETE' ('/api/boards/' + $createdId) 204
    Assert-Status $record
    Assert-Record $record ([string]::IsNullOrEmpty($record.responseBody)) '204 삭제 응답에는 본문 없음'
    $record.passed = $true
    $createdId = $null

    # 10번: 없는 id와 잘못된 요청의 상태 코드 및 JSON 오류 메시지를 실제 확인한다.
    $record = Invoke-RecordedRequest 6 'get-deleted-404' 'GET' ('/api/boards/' + $deletedId) 404
    Assert-Status $record
    $errorBody = Convert-RecordedJson $record.responseBody
    Assert-Record $record ($errorBody.status -eq 404 -and -not [string]::IsNullOrEmpty($errorBody.message)) '없는 글 오류 JSON은 status404 + message'
    $record.passed = $true

    $record = Invoke-RecordedRequest 7 'malformed-json-400' 'POST' '/api/boards' 400 '{"title":'
    Assert-Status $record
    $errorBody = Convert-RecordedJson $record.responseBody
    Assert-Record $record ($errorBody.status -eq 400 -and -not [string]::IsNullOrEmpty($errorBody.message)) 'JSON 문법 오류는 status400 + message'
    $record.passed = $true

    $payload = @{ title = ' '; content = '저장되지 않아야 하는 요청'; author = 'API 오류 검증' } | ConvertTo-Json -Compress
    $record = Invoke-RecordedRequest 8 'blank-title-400' 'POST' '/api/boards' 400 $payload
    Assert-Status $record
    $errorBody = Convert-RecordedJson $record.responseBody
    Assert-Record $record ($errorBody.status -eq 400 -and $errorBody.message -match 'title') '공백 제목은 title 필수값 검증 오류'
    $record.passed = $true

    $record = Invoke-RecordedRequest 9 'invalid-id-400' 'GET' '/api/boards/not-a-number' 400
    Assert-Status $record
    $errorBody = Convert-RecordedJson $record.responseBody
    Assert-Record $record ($errorBody.status -eq 400 -and -not [string]::IsNullOrEmpty($errorBody.message)) '숫자가 아닌 id는 status400 + message'
    $record.passed = $true
    $passed = $true
} catch {
    $failure = $_.Exception.Message
    # 정리 대상은 이 실행의 POST 응답 id 하나뿐이다. 다른 게시글 목록을 돌며 삭제하지 않는다.
    if ($null -ne $createdId) {
        try {
            $cleanup = Invoke-RecordedRequest 99 'cleanup-own-row' 'DELETE' ('/api/boards/' + $createdId) 204
            Assert-Status $cleanup
            $cleanup.passed = $true
            $createdId = $null
        } catch {
            $failure += ' / 자신의 테스트 글 정리 실패: ' + $_.Exception.Message
        }
    }
} finally {
    Save-Reports
    $client.Dispose()
}

if (-not $passed) { throw ('실제 API 검증 실패: ' + $failure + ' / 로그: ' + $OutputDirectory) }
Write-Host ('PASS: ' + $records.Count + ' actual HTTP requests; report=' + $reportPath)
