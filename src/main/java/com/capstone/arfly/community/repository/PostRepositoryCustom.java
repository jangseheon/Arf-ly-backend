package com.capstone.arfly.community.repository;


import com.capstone.arfly.community.domain.Post;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PostRepositoryCustom {
    List<Post> searchLatestPosts(String keyword, Long cursor, Pageable pageable);
    List<Post> searchLikedPosts(String keyword, Long cursor, Integer likesCursor, Pageable pageable);
    long countSearchResults(String keyword);
}
