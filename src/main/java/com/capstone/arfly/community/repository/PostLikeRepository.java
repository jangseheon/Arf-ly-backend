package com.capstone.arfly.community.repository;

import com.capstone.arfly.community.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE  FROM PostLike p WHERE p.post.id = :postId AND p.member.id = :memberId 
            """)
    void likeCancel(@Param("postId") long postId, @Param("memberId") long memberId);
}
