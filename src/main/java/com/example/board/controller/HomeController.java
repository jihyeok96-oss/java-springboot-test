/*
 * [7·10번 연계] HomeController.java - 브라우저의 시작 주소에서 게시글 목록으로 이동한다.
 * 수업 참고:
 * https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/java/com/company/myapp/controller/BoardController.java
 *   - @Controller에서 "redirect:/경로" 문자열을 반환하는 방식.
 * [수업 예제 외 추가] TEST.md에 필수인 /boards 경로 외에 GET / 시작 주소를 편의상 연결한다.
 */
package com.example.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/boards";
    }
}
