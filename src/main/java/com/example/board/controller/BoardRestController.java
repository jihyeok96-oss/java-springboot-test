/*
 * [8번] BoardRestController.java - JSON으로 게시글 목록·상세·등록·수정·삭제를 제공한다.
 * [9번 연계] GET/PUT 200, POST 201, DELETE 204로 API 테스트 결과를 확인할 수 있다.
 * [10번 연계] @Valid로 필수값을 확인하고 Service 예외는 공통 처리기에서 400/404로 구분한다.
 *
 * 수업 참고:
 * https://github.com/jihyeok96-oss/ref-back/blob/master/src/main/java/com/logic/project/api/QnaRestController.java
 *   - @RestController, ResponseEntity.ok(Entity), 삭제 시 ResponseEntity.noContent().build().
 * https://github.com/jihyeok96-oss/project1/blob/main/backend/notice/src/main/java/com/oji/notice/controller/NoticeRestController.java
 *   - REST CRUD 경로, @Valid @RequestBody, 생성 201·삭제 204 상태 코드.
 * https://github.com/jihyeok96-oss/mobility_project_backend/blob/main/src/main/java/com/kim/mobility/controller/BoardRestController.java
 *   - /api/boards 경로와 @GetMapping/@PostMapping/@PutMapping/@DeleteMapping 작성 방식.
 *
 * 문제 조건에 맞춘 변경: NoticeRequest/NoticeResponse 같은 DTO 대신 Board Entity를 직접 사용한다.
 */
package com.example.board.controller;

import com.example.board.domain.Board;
import com.example.board.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardRestController {

    private final BoardService boardService;

    // [8번] GET /api/boards: List<Board>가 JSON 배열로 직렬화된다.
    // [4번 연계] /api/boards?keyword=검색어로 MVC와 같은 제목 검색도 사용할 수 있다.
    @GetMapping
    public ResponseEntity<List<Board>> list(
            @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        return ResponseEntity.ok(boardService.search(keyword));
    }

    // [8번] GET /api/boards/{id}: 단건 JSON. [5번] 조회수가 증가된 Board를 반환한다.
    @GetMapping("/{id}")
    public ResponseEntity<Board> detail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(boardService.findById(id));
    }

    // [8번] POST /api/boards: Content-Type: application/json, title/content/author를 받는다.
    @PostMapping
    public ResponseEntity<Board> create(@Valid @RequestBody Board board) {
        Board savedBoard = boardService.save(board);
        // [수업 예제 외 추가 / 9번] 201의 Location 헤더에 생성된 게시글 API 주소도 제공한다.
        // ResponseEntity는 ref-back, 생성 상태 201은 project1 예제의 방식을 조합했다.
        return ResponseEntity.created(URI.create("/api/boards/" + savedBoard.getId()))
                .body(savedBoard);
    }

    // [8번] PUT /api/boards/{id}: 요청 JSON의 id 대신 URL의 id를 기준으로 기존 글을 수정한다.
    @PutMapping("/{id}")
    public ResponseEntity<Board> update(@PathVariable("id") Long id,
                                        @Valid @RequestBody Board board) {
        return ResponseEntity.ok(boardService.update(id, board));
    }

    // [8번] DELETE /api/boards/{id}: 성공 시 204로 반환하므로 응답 본문은 없다.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
