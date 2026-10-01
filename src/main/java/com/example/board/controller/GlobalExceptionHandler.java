/*
 * [10번] GlobalExceptionHandler.java - 없는 id는 404, 잘못된 입력·JSON·id 형식은 400으로 처리한다.
 * [7번 연계] MVC 요청은 error/404 또는 error/400 Thymeleaf 화면을 반환한다.
 * [8·9번 연계] API 요청은 상태 코드와 message가 들어 있는 JSON을 반환한다. 오류 DTO도 만들지 않는다.
 *
 * 수업 참고:
 * https://github.com/jihyeok96-oss/mobility_project_backend/blob/main/src/main/java/com/kim/mobility/controller/GlobalExceptionHandler.java
 *   - @ExceptionHandler, ResponseEntity.badRequest(), Map.of(), @Valid 검증 오류의 첫 메시지.
 * https://github.com/jihyeok96-oss/ref-back/blob/master/src/main/java/com/logic/project/controller/ApiExceptionHandler.java
 *   - Map.of("status", 상태값, "message", 메시지) 형식의 API 오류 응답.
 *
 * [수업 예제 외 추가] MVC와 REST를 한 애플리케이션에서 제공하므로 @ControllerAdvice가
 * 요청 경로를 보고 HTML/JSON 응답을 분리한다. 숫자가 아닌 id와 문법이 잘못된 JSON도 명시적으로
 * 400 처리하고, BoardNotFoundException은 일반 입력 오류와 분리하여 404로 처리한다.
 */
package com.example.board.controller;

import com.example.board.exception.BoardNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@ControllerAdvice(assignableTypes = {BoardController.class, BoardRestController.class})
public class GlobalExceptionHandler {

    // [10번] DB에서 해당 id를 찾지 못한 경우: 브라우저와 API 모두 HTTP 404.
    @ExceptionHandler(BoardNotFoundException.class)
    public Object notFound(BoardNotFoundException exception, HttpServletRequest request) {
        return errorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    // [10번] @Valid @RequestBody 검증 오류: 필수 제목/작성자와 길이 제한을 확인하도록 안내한다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("입력값을 확인하세요.");
        return errorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // [10번] /boards/abc처럼 Long 변환이 불가능한 id와, 빈 요청/잘못된 JSON을 400 처리한다.
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public Object badRequest(Exception exception, HttpServletRequest request) {
        String message;
        if (exception instanceof MethodArgumentTypeMismatchException) {
            message = "게시글 id는 숫자로 입력하세요.";
        } else if (exception instanceof HttpMessageNotReadableException) {
            message = "JSON 요청 본문을 확인하세요. title, content, author 형식을 사용하세요.";
        } else {
            message = exception.getMessage();
        }
        return errorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // [수업 예제 외 추가] 컨텍스트 경로가 설정되어도 /api/ 요청 여부를 정확히 판단한다.
    private Object errorResponse(HttpStatus status, String message, HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/api/")) {
            return ResponseEntity.status(status)
                    .body(Map.of("status", status.value(), "message", message));
        }

        // ModelAndView의 상태 코드를 설정해야 오류 화면을 표시하면서 200을 반환하지 않는다.
        ModelAndView modelAndView = new ModelAndView("error/" + status.value());
        modelAndView.setStatus(status);
        modelAndView.addObject("message", message);
        return modelAndView;
    }
}
