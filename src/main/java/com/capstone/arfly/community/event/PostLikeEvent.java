package com.capstone.arfly.community.event;

import com.capstone.arfly.community.constant.LikeEventType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PostLikeEvent extends ApplicationEvent {
    private final long postId;
    private final long memberId;
    private final LikeEventType likeEventType;

    public PostLikeEvent(Object src,long postId, long memberId, LikeEventType likeEventType){
        super(src);
        this.postId = postId;
        this.memberId = memberId;
        this.likeEventType = likeEventType;
    }
}
