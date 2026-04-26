package com.capstone.arfly.community.event;

import com.capstone.arfly.common.service.FirebaseService;
import com.capstone.arfly.notification.domain.FcmToken;
import com.capstone.arfly.notification.repository.FcmTokenRepository;
import com.capstone.arfly.notification.service.NotificationService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventHandler {
    private final FcmTokenRepository fcmTokenRepository;
    private final FirebaseService firebaseService;
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase =  TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreatedEvent(CommentCreatedEvent event){
        log.info("Comment Mention 푸시 알람 발송 시작");
        Set<Long> mentionedUserIds = event.getMentionedUserIds();

        List<FcmToken> fcmTokens = fcmTokenRepository.findByMemberIdAndNotificationEnabled(
                mentionedUserIds);

        Set<Long> failedFcmTokenIds = firebaseService.sendAllMentionNotifications(event.getPost(), event.getCommenter()
                ,  fcmTokens);
        List<Long> successFcmTokenIds = fcmTokens.stream()
                .map(FcmToken::getId)
                .filter(tokenId -> !failedFcmTokenIds.contains(tokenId))
                .distinct().toList();
        notificationService.updateCommentResults(successFcmTokenIds,failedFcmTokenIds);
        log.info("Comment Mention 푸시 알람 발송 종료");
    }
}
