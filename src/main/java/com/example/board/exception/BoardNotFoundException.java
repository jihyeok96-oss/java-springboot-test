/*
 * [5·10번] BoardNotFoundException.java - 존재하지 않는 게시글 id를 나타내는 예외.
 * 수업 참고:
 * https://github.com/jihyeok96-oss/mvclab/blob/main/src/main/java/com/example/mvclab/exception/MemberNotFoundException.java
 *   - 찾을 수 없는 id를 별도 예외 클래스로 표현하는 구조를 참고한다.
 *
 * [수업 예제 외 추가] 원본의 checked Exception/println 대신 RuntimeException과 super(message)를
 * 사용한다. Service에서 발생시킨 예외를 ControllerAdvice가 받아 HTTP 404로 응답할 수 있도록 한다.
 * 이 구분이 있어야 입력 오류(400)와 존재하지 않는 글(404)을 서로 다른 상태 코드로 처리할 수 있다.
 */
package com.example.board.exception;

public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundException(Long id) {
        super(id + "번 게시글을 찾을 수 없습니다.");
    }
}
