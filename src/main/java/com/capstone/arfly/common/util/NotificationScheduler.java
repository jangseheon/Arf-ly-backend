package com.capstone.arfly.common.util;

import com.capstone.arfly.common.dto.MedicationAlarmDto;
import com.capstone.arfly.common.service.FirebaseService;
import com.capstone.arfly.notification.repository.FcmTokenRepository;
import com.capstone.arfly.notification.repository.MedicationReminderRepository;
import com.capstone.arfly.notification.service.NotificationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationScheduler {
    private final MedicationReminderRepository medicationReminderRepository;
    private final FirebaseService firebaseService;
    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationService notificationService;

    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    //매 분 0초마다 알림 대상을 수집하여 알림을 발송
    @Scheduled(cron = "0 * * * * *")
    public void sendScheduledNotifications() {
        if(!isProcessing.compareAndSet(false, true)){
            log.warn("이전 알림 발송 작업이 완료되지 않아 이번 작업을 건너뜁니다.");
            return;
        }

        log.info("약 알림 발송 스케줄러 작동");
        LocalTime now = LocalTime.now();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        int pageNumber = 0;
        int pageSize = 500; // FCM 일괄 전송은 최대 500개까지만 가능
        while (true) {
            // DB에서 500개씩 끊어서 조회
            Slice<MedicationAlarmDto> reminderSlice = medicationReminderRepository.findPendingNotifications(
                    now, startOfDay, PageRequest.of(pageNumber, pageSize)
            );
            if (reminderSlice.isEmpty()) {
                break;
            }
            List<MedicationAlarmDto> reminders = reminderSlice.getContent();

            // 알림 발송 및 실패한 토큰 ID 수집
            Set<Long> failedFcmTokenIds = firebaseService.sendAllNotifications(reminders);

            // 발송에 성공한 FCM Token ID를 추출
            List<Long> successFcmTokenIds = reminders.stream()
                    .map(MedicationAlarmDto::fcmTokenId)
                    .filter(tokenId -> !failedFcmTokenIds.contains(tokenId))
                    .distinct()
                    .toList();
            // 대상자 알림 전부 업데이트(실패 알람 무한 재시도 막기 위함)
            List<Long> allMedicationReminderIds = reminders.stream()
                    .map(MedicationAlarmDto::medicationId)
                    .distinct()
                    .toList();

            List<Long> failedFcmTokenList = failedFcmTokenIds.stream().toList();
            //알람 결과 및 FCM Token DB 업데이트
            notificationService.updateAlarmResults(successFcmTokenIds, failedFcmTokenList, allMedicationReminderIds);
            if(!reminderSlice.hasNext()) break;

        }
        log.info("약 알림 발송 스케줄러 종료");
        isProcessing.set(false);

    }

    // 매일 오전 3시에 한달동안 사용하지 않은 Fcm Token 제거
    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanUpInactiveTokens() {
        log.info("휴먼 FCM 토큰 정리 시작");
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        fcmTokenRepository.deleteByLastUsedAtBefore(oneMonthAgo);
        log.info("휴먼 FCM 토큰 정리 완료");
    }
}
