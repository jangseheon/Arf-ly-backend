package com.capstone.arfly.community.event;

import com.capstone.arfly.community.domain.Comment;
import com.capstone.arfly.community.domain.Post;
import com.capstone.arfly.member.domain.Member;
import java.util.Set;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CommentCreatedEvent extends ApplicationEvent {
    private final Post post;
    private final Member commenter;
    private final Comment comment;
    private final Set<Long> mentionedUserIds;

    public CommentCreatedEvent(Object source,Post post,Member commenter,Comment comment, Set<Long> mentionedUserIds){
        super(source);
        this.post = post;
        this.commenter = commenter;
        this.comment = comment;
        this.mentionedUserIds = mentionedUserIds;
    }
}
