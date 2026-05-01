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
public interface PostRepository extends JpaRepository<Post,Long>, PostRepositoryCustom {
    @EntityGraph(attributePaths = {"member"})
    Optional<Post>findById(Long id);

}