package com.capstone.arfly.community.repository;

import com.capstone.arfly.community.domain.Post;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.domain.Pageable;


@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    @EntityGraph(attributePaths = {"member"})
    Optional<Post>findById(Long id);

    // 1. 최신순 조회 (id 내림차순)
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.member m " + // 작성자 닉네임을 가져오기 위한 Fetch Join (N+1 방지)
            "WHERE (:cursor IS NULL OR p.id < :cursor) " +
            "ORDER BY p.id DESC")
    List<Post> findLatestPosts(@Param("cursor") Long cursor, Pageable pageable);

    // 2. 좋아요순 조회 (likeCount 내림차순, id 내림차순) likeCount가 같으면 id기준 정렬
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.member m " +
            "WHERE (:cursor IS NULL OR " +
            "      (p.likeCount < :likesCursor) OR " +
            "      (p.likeCount = :likesCursor AND p.id < :cursor)) " +
            "ORDER BY p.likeCount DESC, p.id DESC")
    List<Post> findLikedPosts(@Param("cursor") Long cursor,
                              @Param("likesCursor") Integer likesCursor,
                              Pageable pageable);
}