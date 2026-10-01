/*
 * 3번. Board.java - board 테이블과 연결되는 JPA Entity
 * 5번/8번. 조회수 증가와 게시글 수정을 제공하고 JSON 요청/응답에도 직접 사용한다.
 * 참고: spring-practice-ans/.../entity/Board.java의 Entity/Lombok/시간 어노테이션,
 *       mobility0917/.../domain/Board.java의 hits 초기값과 생성일 수정 방지 방식.
 * https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/java/com/company/myapp/entity/Board.java
 * https://github.com/jihyeok96-oss/mobility0917/blob/main/src/main/java/com/oji/mobility/domain/Board.java
 */
package com.example.board.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "board")
// [수업 예제 외 추가 / 원본 보완] 시간 어노테이션이 실제 저장/수정 시 실행되도록 리스너를 연결한다.
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {

    // 3번: Long PK를 PostgreSQL의 IDENTITY 기능으로 자동 생성한다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 3번/10번: null뿐 아니라 빈 문자열/공백 제목도 입력 오류로 처리한다.
    // @NotBlank/@Size 검증 방식은 mvclab MemberForm의 Bean Validation 예제를 따른다.
    @Setter
    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max = 255, message = "제목은 255자 이하로 입력해 주세요.")
    @Column(nullable = false, length = 255)
    private String title;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String content;

    @Setter
    @NotBlank(message = "작성자를 입력해 주세요.")
    @Size(max = 255, message = "작성자는 255자 이하로 입력해 주세요.")
    @Column(nullable = false, length = 255)
    private String author;

    // 3번: Lombok Builder로 생성해도 hits가 0L이 되도록 @Builder.Default를 사용한다.
    @Column(nullable = false)
    @Builder.Default
    private Long hits = 0L;

    // 2번/3번: TimeConfig의 서울 Clock을 사용하는 JPA Auditing이 값을 채운다.
    // updatable=false는 UPDATE SQL에서 생성일시가 변경되지 않게 한다.
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    /*
     * 2번/3번/6번/8번. DB에서 읽은 시각을 화면과 JSON에도 서울 시간으로 반환한다.
     * [수업 예제 외 추가] PostgreSQL timestamp with time zone은 ZoneId를 저장하지 않으므로
     * Hibernate가 조회 시 UTC로 읽을 수 있다. withZoneSameInstant는 실제 시점은 유지하고
     * 표현 시간대만 Asia/Seoul로 변경한다. 저장/수정 시각 자체는 계속 Auditing이 관리한다.
     * https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/ZonedDateTime.html#withZoneSameInstant(java.time.ZoneId)
     */
    public ZonedDateTime getCreatedAt() {
        return createdAt == null ? null : createdAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt == null ? null : updatedAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
    }

    /*
     * 5번. 상세 조회에서 호출한다. Service의 쓰기 트랜잭션이 끝날 때
     * JPA 변경 감지(Dirty Checking)가 조회수를 DB에 반영한다.
     * [수업 예제 외 추가] 전체 필드에 Setter를 여는 대신 조회수 변경만 메서드로 제공한다.
     */
    public void increaseHits() {
        this.hits++;
    }

    /*
     * 5번. 기존 Entity의 입력 항목만 수정한다.
     * id, hits, createdAt은 건드리지 않으며 updatedAt은 Auditing에 맡긴다.
     * [수업 예제 외 추가] 수업의 setTitle/setContent/setAuthor 호출을 하나의 메서드로 묶었다.
     */
    public void update(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }
}
