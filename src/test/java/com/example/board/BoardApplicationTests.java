/*
 * 9번/10번. BoardApplicationTests.java - Repository/Service/MVC/REST 통합 검증
 * 참고: java-springboot-test의 @SpringBootTest/@Test 테스트 클래스 작성 방식.
 * [수업 예제 외 추가] MockMvc, H2, 테스트용 Clock으로 CRUD/조회수/시간/입력 오류를 자동 검증한다.
 * HTTP 요청은 실제 Controller와 Service, JPA를 거치며 Repository를 Mock으로 바꾸지 않는다.
 * 각 테스트는 @Transactional로 끝에서 롤백한다. 기존 PostgreSQL 게시글은 변경하지 않는다.
 */
package com.example.board;

import com.example.board.domain.Board;
import com.example.board.repository.BoardRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BoardApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdjustableClock clock;

    private static final Instant START = Instant.parse("2026-10-01T01:00:00Z");

    @BeforeEach
    void resetClock() {
        clock.setInstant(START);
    }

    // 1번/2번: 전체 Spring 설정과 서울 Clock Bean이 정상 구성되는지 확인한다.
    @Test
    void contextLoads() {
        assertThat(clock.getZone()).isEqualTo(ZoneId.of("Asia/Seoul"));
    }

    /*
     * 3번/4번: 생성 시간이 테스트 Clock에서 채워지고 제목 검색/전체 목록이 최신순인지 확인한다.
     * 제목에 없는 키워드가 내용에만 있는 게시글은 제목 검색 결과에서 제외되어야 한다.
     */
    @Test
    void auditingAndTitleSearchUseClockAndLatestOrder() {
        Board oldBoard = persist("스프링 첫 수업", "본문", "학생");
        clock.setInstant(START.plusSeconds(60));
        Board newestBoard = persist("스프링 두 번째 수업", "본문", "학생");
        clock.setInstant(START.plusSeconds(120));
        Board otherBoard = persist("자바 수업", "스프링은 내용에만 있음", "학생");

        assertThat(oldBoard.getCreatedAt().toInstant()).isEqualTo(START);
        assertThat(oldBoard.getCreatedAt().getZone()).isEqualTo(ZoneId.of("Asia/Seoul"));
        assertThat(oldBoard.getUpdatedAt().toInstant()).isEqualTo(START);
        assertThat(oldBoard.getHits()).isZero();
        assertThat(boardRepository.findByTitleContainingOrderByCreatedAtDesc("스프링"))
                .extracting(Board::getId).containsExactly(newestBoard.getId(), oldBoard.getId());
        assertThat(boardRepository.findAllByOrderByCreatedAtDesc())
                .extracting(Board::getId).containsExactly(otherBoard.getId(), newestBoard.getId(), oldBoard.getId());
    }

    /*
     * 5번/8번/9번: GET 목록 → POST 등록 → PUT 수정 → GET 단건 → DELETE 흐름.
     * Entity를 JSON으로 직접 받고 반환하되 등록 입력의 id/hits/시간은 서버가 결정한다.
     * 수정에서는 PK/생성일/조회수를 보존하고 상세 조회에서만 hits가 1 증가한다.
     */
    @Test
    void restCrudPreservesIdentityAndIncrementsHits() throws Exception {
        mockMvc.perform(get("/api/boards")).andExpect(status().isOk()).andExpect(content().json("[]"));

        MvcResult created = mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":999,"title":"REST 등록","content":"내용","author":"학생","hits":999,
                                 "createdAt":"2000-01-01T00:00:00+09:00","updatedAt":"2000-01-01T00:00:00+09:00"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hits").value(0))
                .andReturn();
        JsonNode createdJson = json(created);
        Long id = createdJson.get("id").longValue();
        String createdAt = createdJson.get("createdAt").asString();
        assertThat(createdAt).endsWith("+09:00");
        assertThat(id).isNotEqualTo(999L);
        assertThat(created.getResponse().getHeader("Location")).endsWith("/api/boards/" + id);
        flushAndClear();

        mockMvc.perform(get("/api/boards/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.hits").value(1));
        flushAndClear();
        assertThat(boardRepository.findById(id).orElseThrow().getHits()).isEqualTo(1L);

        clock.setInstant(START.plusSeconds(60));
        mockMvc.perform(put("/api/boards/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":888,"title":"REST 수정","content":"수정 내용","author":"수정 학생","hits":900,
                                 "createdAt":"2000-01-01T00:00:00+09:00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("REST 수정"))
                .andExpect(jsonPath("$.createdAt").value(createdAt))
                .andExpect(jsonPath("$.hits").value(1));
        flushAndClear();
        Board updated = boardRepository.findById(id).orElseThrow();
        assertThat(updated.getCreatedAt().toInstant()).isEqualTo(START);
        assertThat(updated.getUpdatedAt().toInstant()).isEqualTo(START.plusSeconds(60));
        assertThat(updated.getAuthor()).isEqualTo("수정 학생");

        mockMvc.perform(get("/api/boards/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.hits").value(2));
        mockMvc.perform(delete("/api/boards/{id}", id))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        flushAndClear();
        assertThat(boardRepository.existsById(id)).isFalse();
        mockMvc.perform(get("/api/boards/{id}", id)).andExpect(status().isNotFound());
    }

    /*
     * 6번/7번/10번: MVC 등록 → 목록 → 상세 → 수정 폼 → 수정 → 목록 → 삭제.
     * 편집 폼과 목록은 hits를 바꾸지 않고, 실제 상세 화면만 증가시켜야 한다.
     * MVC로 생성한 게시글이 같은 Repository를 사용하는 JSON API에도 보여야 한다.
     */
    @Test
    void mvcCrudAndRestShareSameData() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/boards"));
        mockMvc.perform(get("/boards/new"))
                .andExpect(status().isOk()).andExpect(view().name("board/form"));
        mockMvc.perform(post("/boards")
                        .param("title", "화면 등록").param("content", "첫째 줄\n둘째 줄").param("author", "학생")
                        .param("id", "999").param("hits", "999"))
                .andExpect(status().is3xxRedirection());
        flushAndClear();
        Board board = boardRepository.findAll().getFirst();
        Long id = board.getId();
        assertThat(board.getHits()).isZero();
        assertThat(id).isNotEqualTo(999L);

        mockMvc.perform(get("/boards").param("keyword", "화면"))
                .andExpect(status().isOk()).andExpect(view().name("board/index"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("화면 등록")));
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mockMvc.perform(get("/boards/{id}", id))
                .andExpect(status().isOk()).andExpect(view().name("board/detail"));
        flushAndClear();
        mockMvc.perform(get("/boards/{id}/edit", id))
                .andExpect(status().isOk()).andExpect(view().name("board/form"));
        assertThat(boardRepository.findById(id).orElseThrow().getHits()).isEqualTo(1L);

        mockMvc.perform(post("/boards/{id}", id)
                        .param("title", "화면 수정").param("content", "수정 내용").param("author", "수정 학생")
                        .param("id", "777").param("hits", "900"))
                .andExpect(status().is3xxRedirection());
        flushAndClear();
        Board edited = boardRepository.findById(id).orElseThrow();
        assertThat(edited.getTitle()).isEqualTo("화면 수정");
        assertThat(edited.getHits()).isEqualTo(1L);
        assertThat(edited.getCreatedAt().toInstant()).isEqualTo(START);
        mockMvc.perform(get("/boards")).andExpect(status().isOk());
        mockMvc.perform(post("/boards/{id}/delete", id))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/boards"));
        flushAndClear();
        assertThat(boardRepository.existsById(id)).isFalse();
    }

    // 10번: 필수 입력 오류는 DB에 저장하지 않고 입력 화면/HTTP 400으로 응답한다.
    @Test
    void invalidRequestsAndUnknownIdsAreHandled() throws Exception {
        mockMvc.perform(post("/boards").param("title", " ").param("author", ""))
                .andExpect(status().isBadRequest()).andExpect(view().name("board/form"))
                .andExpect(model().attributeHasFieldErrors("board", "title", "author"));
        mockMvc.perform(post("/api/boards").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"author\":\"\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/boards").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/boards/not-a-number")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/boards/999999")).andExpect(status().isNotFound());
        mockMvc.perform(put("/api/boards/999999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"정상 제목\",\"author\":\"학생\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/boards/999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/boards/999999")).andExpect(status().isNotFound());
        mockMvc.perform(post("/boards/999999/delete")).andExpect(status().isNotFound());
        assertThat(boardRepository.count()).isZero();
    }

    // 7번/10번: 잘못된 수정 폼을 다시 보여줄 때 수정 대상 URL과 기존 DB 값을 유지한다.
    @Test
    void invalidEditKeepsActionAndOriginalEntity() throws Exception {
        Board original = persist("기존 제목", "기존 내용", "기존 작성자");
        Long id = original.getId();
        mockMvc.perform(post("/boards/{id}", id).param("title", "").param("author", "학생"))
                .andExpect(status().isBadRequest()).andExpect(view().name("board/form"))
                .andExpect(model().attributeHasFieldErrors("board", "title"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/boards/" + id + "\"")));
        flushAndClear();
        assertThat(boardRepository.findById(id).orElseThrow().getTitle()).isEqualTo("기존 제목");
    }

    // 6번/10번: 사용자 HTML은 실행되는 태그가 아닌 이스케이프된 텍스트로 출력한다.
    @Test
    void userContentIsEscapedInHtml() throws Exception {
        Board board = persist("<script>alert(1)</script>", "<b>본문</b>", "학생");
        MvcResult result = mockMvc.perform(get("/boards/{id}", board.getId())).andExpect(status().isOk()).andReturn();
        String html = result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(html).contains("&lt;script&gt;alert(1)&lt;/script&gt;", "&lt;b&gt;본문&lt;/b&gt;");
        assertThat(html).doesNotContain("<script>alert(1)</script>");
    }

    private Board persist(String title, String content, String author) {
        return boardRepository.saveAndFlush(Board.builder().title(title).content(content).author(author).build());
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    }

    // 수정 시간은 JPA UPDATE 직전에 채워지므로 flush 후 영속성 컨텍스트를 비우고 DB 값을 확인한다.
    private void flushAndClear() {
        boardRepository.flush();
        entityManager.clear();
    }

    @TestConfiguration
    static class ClockTestConfig {
        @Bean
        @Primary
        AdjustableClock testClock() {
            return new AdjustableClock(START, ZoneId.of("Asia/Seoul"));
        }
    }

    /*
     * [수업 예제 외 추가] Thread.sleep 없이 생성/수정 시각을 검증하는 테스트용 Clock.
     * 운영 코드의 Clock은 그대로 두고 테스트 프로파일에서 주입 우선순위만 변경한다.
     */
    static class AdjustableClock extends Clock {
        private Instant now;
        private final ZoneId zone;

        AdjustableClock(Instant now, ZoneId zone) {
            this.now = now;
            this.zone = zone;
        }

        void setInstant(Instant now) {
            this.now = now;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(now, zone);
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
