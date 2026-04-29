package com.capstone.arfly.community.event;

import com.capstone.arfly.community.constant.LikeEventType;
import com.capstone.arfly.community.domain.Post;
import com.capstone.arfly.community.domain.PostLike;
import com.capstone.arfly.community.repository.PostLikeRepository;
import com.capstone.arfly.community.repository.PostRepository;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PostLikeEventHandler {
    private final PostLikeRepository postLikeRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void  handlePostLikeEvent(PostLikeEvent  postLikeEvent){
        Optional<Post> optionalPost = postRepository.findById(postLikeEvent.getPostId());
        if(optionalPost.isEmpty()){
            return;
        }
        Post post = optionalPost.get();
        Member member = memberRepository.getReferenceById(postLikeEvent.getMemberId());

        if(postLikeEvent.getLikeEventType().equals(LikeEventType.LIKE)){
            PostLike newPostLike = PostLike.builder().member(member).post(post).build();
            postLikeRepository.save(newPostLike);
            return;
        }
        postLikeRepository.likeCancel(post.getId(),member.getId());

    }
}
