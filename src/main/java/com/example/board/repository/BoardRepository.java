/*
 * 4번. BoardRepository.java - 기본 CRUD 및 생성일시 내림차순 제목 검색
 * 참고: 수업의 JpaRepository<Board, Long> 상속과 findByTitleContaining... 파생 쿼리 방식.
 * https://github.com/jihyeok96-oss/spring-practice-ans/blob/master/src/main/java/com/company/myapp/repository/BoardRepository.java
 * [평가문항에 맞춘 변경] 수업의 Page/Pageable 대신 List를 반환하고 최신순 정렬을 이름에 포함한다.
 */
package com.example.board.repository;

import com.example.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 4번: save, findById, findAll, deleteById 등 기본 CRUD는 부모 인터페이스가 제공한다.

    // 전체 게시글을 생성일시 내림차순(최신순)으로 조회
    List<Board> findAllByOrderByCreatedAtDesc();

    // findBy=조회, Title=Entity 필드, Containing=문자열 포함,
    // OrderByCreatedAtDesc=createdAt 내림차순. 직접 SQL/JPQL을 작성하지 않아도 된다.
    List<Board> findByTitleContainingOrderByCreatedAtDesc(String keyword);
}
