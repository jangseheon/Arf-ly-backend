package com.capstone.arfly.community.repository;

import com.capstone.arfly.community.domain.Post;
import static com.capstone.arfly.community.domain.QPost.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> searchLatestPosts(String keyword, Long cursor, Pageable pageable) {
        return queryFactory
                .selectFrom(post)
                .innerJoin(post.member).fetchJoin() // N+1 방지 (기존 JOIN FETCH 대체)
                .where(
                        containsKeyword(keyword),
                        ltCursorId(cursor)
                )
                .orderBy(post.id.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<Post> searchLikedPosts(String keyword, Long cursor, Integer likesCursor, Pageable pageable) {
        return queryFactory
                .selectFrom(post)
                .innerJoin(post.member).fetchJoin() // N+1 방지
                .where(
                        containsKeyword(keyword),
                        ltLikeCountAndId(cursor, likesCursor)
                )
                .orderBy(post.likeCount.desc(), post.id.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public long countSearchResults(String keyword) {
        Long count = queryFactory
                .select(post.count())
                .from(post)
                .where(containsKeyword(keyword))
                .fetchOne();
        return count != null ? count : 0L;
    }

    //  동적 쿼리 조건
    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        return post.title.containsIgnoreCase(keyword)
                .or(post.content.containsIgnoreCase(keyword));
    }

    private BooleanExpression ltCursorId(Long cursorId) {
        return cursorId == null ? null : post.id.lt(cursorId);
    }

    private BooleanExpression ltLikeCountAndId(Long cursorId, Integer likesCursor) {
        if (cursorId == null || likesCursor == null) return null;
        return post.likeCount.lt(likesCursor)
                .or(post.likeCount.eq(likesCursor).and(post.id.lt(cursorId)));
    }
}