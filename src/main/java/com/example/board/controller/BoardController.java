/*
 * [7번] BoardController.java - Thymeleaf 화면에 데이터를 전달하는 MVC Controller.
 * [6번 연계] board/index, board/form, board/detail 템플릿을 반환한다.
 * [10번 연계] 잘못된 등록·수정 입력을 검증하고 입력 폼에 오류를 표시한다.
 *
 * 수업 참고:
 * https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/java/com/company/myapp/controller/BoardController.java
 *   - @Controller/@RequiredArgsConstructor, Model, @ModelAttribute Board, 상세·등록·수정·삭제 후 redirect.
 * https://github.com/jihyeok96-oss/ref-back/blob/master/src/main/java/com/logic/project/controller/QnaController.java
 *   - /목록/{id}, /new, /{id}/edit, /{id}/delete 형식의 MVC 경로.
 * https://github.com/jihyeok96-oss/mvclab/blob/main/src/main/java/com/example/mvclab/web/MemberController.java
 *   - @Valid, BindingResult.hasErrors()로 폼 검증 실패 시 입력 화면을 다시 반환한다.
 *
 * 문제 조건에 맞춘 변경: 경로는 TEST.md의 /boards 기준, DTO 없이 Board를 폼 객체로 사용한다.
 */
package com.example.board.controller;

import com.example.board.domain.Board;
import com.example.board.service.BoardService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // [수업 예제 외 추가 / 7·10번] 폼에서는 title/content/author만 Entity에 바인딩한다.
    // id와 조회수·시간을 hidden input 등으로 임의 전송해도 서버 관리 값을 수정할 수 없다.
    @InitBinder("board")
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields("title", "content", "author");
    }

    // [7번] GET /boards. [4번 연계] keyword가 있으면 제목 검색 결과를 최신순으로 표시한다.
    @GetMapping
    public String list(@RequestParam(name = "keyword", defaultValue = "") String keyword,
                       Model model) {
        model.addAttribute("boards", boardService.search(keyword));
        model.addAttribute("keyword", keyword);
        return "board/index";
    }

    // [7번] GET /boards/new: 빈 Board를 th:object에 연결해 등록 폼을 제공한다.
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("board", new Board());
        model.addAttribute("edit", false);
        return "board/form";
    }

    // [7번] POST /boards: 등록 성공 후 목록으로 redirect하여 새로고침 중복 등록을 예방한다.
    @PostMapping
    public String create(@Valid @ModelAttribute("board") Board board,
                         BindingResult bindingResult, Model model,
                         HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("edit", false);
            // [수업 예제 외 추가 / 10번] 폼을 다시 보여 주면서 HTTP 상태도 입력 오류인 400으로 설정한다.
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "board/form";
        }
        boardService.save(board);
        return "redirect:/boards";
    }

    // [7번] GET /boards/{id}: Service에서 hits를 1 올린 Board를 상세 화면에 전달한다.
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("board", boardService.findById(id));
        return "board/detail";
    }

    // [7번] GET /boards/{id}/edit: 등록과 같은 form.html에 기존 글을 채운다.
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("board", boardService.findByIdForEdit(id));
        model.addAttribute("edit", true);
        model.addAttribute("boardId", id);
        return "board/form";
    }

    // [7번] POST /boards/{id}: 수정 성공 후 상세 화면으로 redirect한다.
    @PostMapping("/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("board") Board board,
                         BindingResult bindingResult, Model model,
                         HttpServletResponse response) {
        // [5·10번] 입력 오류가 있더라도 URL이 가리키는 글의 존재 여부부터 확인한다.
        boardService.findByIdForEdit(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("edit", true);
            // [수업 예제 외 추가] 바인딩된 Board에는 id가 없으므로 URL의 id를 별도로 전달한다.
            // 오류 폼의 th:action이 /boards/{id}를 유지해 등록 요청으로 바뀌지 않도록 한다.
            model.addAttribute("boardId", id);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "board/form";
        }
        boardService.update(id, board);
        return "redirect:/boards/" + id;
    }

    // [7번] POST /boards/{id}/delete: 삭제 성공 후 목록으로 redirect한다.
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        boardService.delete(id);
        return "redirect:/boards";
    }
}
