package com.capstone.arfly.community.repository;

import com.capstone.arfly.community.domain.Post;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    Optional<Post>findById(Long id);

    @Modifying
    @Query("UPDATE Post p SET p.like_count = :count WHERE p.id = :postId")
    void updateLikeCount(@Param("postId") Long postId, @Param("count") int count);
}