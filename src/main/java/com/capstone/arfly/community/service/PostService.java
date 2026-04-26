package com.capstone.arfly.community.service;

import com.capstone.arfly.common.exception.InvalidMentionException;
import com.capstone.arfly.common.exception.PostNotFoundException;
import com.capstone.arfly.common.exception.UserNotExistsException;
import com.capstone.arfly.community.domain.Comment;
import com.capstone.arfly.community.domain.CommentMention;
import com.capstone.arfly.community.domain.Post;
import com.capstone.arfly.community.dto.CommentRequestDto;
import com.capstone.arfly.community.event.CommentCreatedEvent;
import com.capstone.arfly.community.repository.CommentMentionRepository;
import com.capstone.arfly.community.repository.CommentRepository;
import com.capstone.arfly.community.repository.PostRepository;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.repository.MemberRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createComment(Long postId, long userId, CommentRequestDto requestDto) {
        //게시물 존재 여부 확인
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        Member commenter = memberRepository.getReferenceById(userId);
        Set<Long> mentionIds = requestDto.getMentionedUserIds();
        boolean hasMentions = mentionIds != null && !mentionIds.isEmpty();


        //내용 내 id 목록과 mentionIdList 일치 여부 확인
        validateContentMentions(requestDto.getContent(), mentionIds);

        Comment newComment = Comment.builder().post(post).member(commenter).content(requestDto.getContent()).build();
        commentRepository.save(newComment);

        //metionUserIds에 존재하는 id가 존재하는지 여부 확인
        if (hasMentions) {
            List<Member> mentionedUsers = memberRepository.findAllById(mentionIds);

            if (mentionedUsers.size() != mentionIds.size()) {
                throw new UserNotExistsException();
            }

            List<CommentMention> commentMentionList = mentionedUsers.stream()
                    .map(user -> CommentMention.builder()
                            .mentionedUser(user)
                            .comment(newComment)
                            .build())
                    .toList();
            commentMentionRepository.saveAll(commentMentionList);

            //푸시 알람을 위한 Event 생성
            eventPublisher.publishEvent(new CommentCreatedEvent(this,
                    post,commenter,newComment,mentionIds));
        }
    }

    private void validateContentMentions(String content, Set<Long> mentionIds) {
        //본문에서 멘션 ID만 추출
        Set<Long> extractedIds = new HashSet<>();
        Set<Long> mentionIdSet = mentionIds == null ? new HashSet<>() : new HashSet<>(mentionIds);
        Pattern pattern = Pattern.compile("@\\[.*?\\]\\(user:(\\d+)\\)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            extractedIds.add(Long.parseLong(matcher.group(1)));
        }

        if (!extractedIds.equals(mentionIdSet)) {
            throw new InvalidMentionException();
        }
    }


}



