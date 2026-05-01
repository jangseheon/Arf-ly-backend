package com.capstone.arfly.community.repository;

import com.capstone.arfly.common.domain.File;
import com.capstone.arfly.community.domain.Post;
import com.capstone.arfly.community.domain.PostImage;
import com.capstone.arfly.community.dto.PostDetailFileDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage,Long> {

    @Query("""
                SELECT new com.capstone.arfly.community.dto.PostDetailFileDto(f.id,f.fileKey)
                FROM PostImage i
                JOIN i.file f
                WHERE i.post.id = :postId
                ORDER BY i.orderIndex ASC
            """)
   List<PostDetailFileDto> findPostDetailFileByPostId(@Param("postId")Long postId);


    @Query("""
            SELECT f          
            FROM PostImage p JOIN p.file f
            WHERE p.post.id = :postId          
            """)
    List<File> findFileByPostId(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PostImage p WHERE p.post.id = :postId ")
    void deleteByPostId(@Param("postId") Long postId);

    @Query("SELECT pi FROM PostImage pi JOIN FETCH pi.file WHERE pi.post IN :posts ORDER BY pi.orderIndex ASC")
    List<PostImage> findAllByPostInWithFile(@Param("posts") List<Post> posts);


}
