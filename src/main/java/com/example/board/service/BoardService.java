/*
 * [5번] BoardService.java - 게시글 CRUD, 상세 조회 시 조회수 증가, 없는 게시글 예외 처리.
 * [4번 연계] 목록과 제목 검색은 createdAt 내림차순으로 반환한다.
 * [10번 연계] 수정·삭제 전에도 id 존재 여부를 확인하여 잘못된 요청을 구분한다.
 *
 * 수업 참고:
 * https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/java/com/company/myapp/service/BoardService.java
 *   - @Service, @RequiredArgsConstructor, Repository 주입, findById/orElseThrow, 기존 글을 읽은 뒤 수정.
 * https://github.com/jihyeok96-oss/ref-back/blob/master/src/main/java/com/logic/project/service/QnaService.java
 *   - @Transactional(readOnly = true), 쓰기 메서드의 @Transactional, 최신순 목록, Entity 수정 메서드.
 * https://github.com/jihyeok96-oss/project1/blob/main/backend/notice/src/main/java/com/oji/notice/service/NoticeService.java
 *   - 상세 조회의 조회수 증가, Builder로 입력값을 복사해 등록, 수정 시 기존 Entity 보존.
 *
 * 문제 조건에 맞춘 변경: DTO를 만들지 않고 입력·출력 모두 Board를 사용한다.
 * 생성/수정 일시는 문제 2·3번의 JPA Auditing이 관리하므로 LocalDateTime.now()로 직접 대입하지 않는다.
 */
package com.example.board.service;

import com.example.board.domain.Board;
import com.example.board.exception.BoardNotFoundException;
import com.example.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    // final 필드에 생성자 주입: Lombok이 Repository를 받는 생성자를 만들어 준다.
    private final BoardRepository boardRepository;

    // [5번] 전체 목록. [4번] Repository의 OrderByCreatedAtDesc로 최신 글을 먼저 보여 준다.
    public List<Board> findAll() {
        return boardRepository.findAllByOrderByCreatedAtDesc();
    }

    // [4번 연계] 빈 검색어이면 전체 목록, 입력된 검색어이면 제목에 포함된 문자열 검색.
    // spring-practice-ans 예제처럼 trim()으로 검색어 앞뒤 공백을 제거한다.
    public List<Board> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        return boardRepository.findByTitleContainingOrderByCreatedAtDesc(keyword.trim());
    }

    // [5번] 상세 조회는 읽기와 동시에 쓰기가 발생하므로 readOnly가 아닌 트랜잭션을 사용한다.
    @Transactional
    public Board findById(Long id) {
        Board board = getBoard(id);
        board.increaseHits();
        // ref-back의 Entity 변경 방식처럼 변경 감지(Dirty Checking)를 이용한다.
        // 트랜잭션 종료 시 hits 변경이 DB에 반영되며, 반환하는 Board에도 증가된 값이 들어 있다.
        return board;
    }

    // [수업 예제 외 추가 / 5·7번] 수정 폼은 상세 열람과 구분하여 조회수를 올리지 않는다.
    // 수정 화면을 열거나 수정·삭제할 때 상세 조회 메서드를 재사용하면 hits가 의도치 않게 증가한다.
    public Board findByIdForEdit(Long id) {
        return getBoard(id);
    }

    // [5번] 신규 등록. 입력 가능한 세 필드만 새 Entity에 복사하고 id/hits/시간은 서버가 관리한다.
    @Transactional
    public Board save(Board board) {
        Board newBoard = Board.builder()
                .title(board.getTitle())
                .content(board.getContent())
                .author(board.getAuthor())
                .build();
        // [수업 예제 외 추가 / 8·10번] Board를 직접 JSON 입력으로 사용하더라도
        // 클라이언트가 보낸 id, hits, createdAt, updatedAt으로 기존 글이나 서버 값을 덮어쓰지 않는다.
        return boardRepository.save(newBoard);
    }

    // [5번] URL의 id로 기존 글을 읽은 뒤 title/content/author만 변경한다.
    @Transactional
    public Board update(Long id, Board board) {
        Board existingBoard = getBoard(id);
        existingBoard.update(board.getTitle(), board.getContent(), board.getAuthor());
        // 기존 Entity를 저장하므로 id, createdAt, hits는 보존되고 updatedAt은 Auditing이 갱신한다.
        return boardRepository.save(existingBoard);
    }

    // [5번] 먼저 존재 여부를 확인하므로 없는 글을 삭제해도 조용히 성공하지 않는다.
    @Transactional
    public void delete(Long id) {
        Board board = getBoard(id);
        boardRepository.delete(board);
    }

    // [5·10번] Optional<Board>에서 값을 꺼내고, 없으면 공통 예외 처리기로 전달한다.
    private Board getBoard(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException(id));
    }
}
