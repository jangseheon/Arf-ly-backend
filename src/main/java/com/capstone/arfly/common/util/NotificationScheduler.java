package com.capstone.arfly.common.util;

import com.capstone.arfly.common.dto.MedicationAlarmDto;
import com.capstone.arfly.common.service.FirebaseService;
import com.capstone.arfly.notification.repository.FcmTokenRepository;
import com.capstone.arfly.notification.repository.MedicationReminderRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    //매 분 0초마다 알림 대상을 수집하여 알림을 발송
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void sendScheduledNotifications() {
        log.info("약 알림 발송 스케줄러 작동");
        // 알림 발송 대상자 조회
        LocalTime now = LocalTime.now();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<MedicationAlarmDto> reminders = medicationReminderRepository.findPendingNotifications(now,
                startOfDay);
        if(reminders.isEmpty()) return;

        // 알림 발송 및 실패한 토큰 ID 수집
        List<Long> failedFcmTokenIds = firebaseService.sendAllNotifications(reminders);
        // 발송에 성공한 FCM Token ID를 추출
        List<Long> successFcmTokenIds = reminders.stream()
                .map(MedicationAlarmDto::fcmTokenId)
                .filter(tokenId ->!failedFcmTokenIds.contains(tokenId))
                .distinct()
                .toList();
        // 발송에 성공한 FCM Token 업데이트
        if(!successFcmTokenIds.isEmpty()){
            fcmTokenRepository.updateTokenLastUsedAt(successFcmTokenIds,LocalDateTime.now());
        }
        //발송에 실패한 FCM Token 삭제
        if(!failedFcmTokenIds.isEmpty()){
            fcmTokenRepository.deleteAllByIdInBatch(failedFcmTokenIds);
        }

        // 대상자 알림 전부 업데이트(실패 알람 무한 재시도 막기 위함)
        List<Long> allMedicationReminderIds = reminders.stream()
                .map(MedicationAlarmDto::medicationId)
                .distinct()
                .toList();
        if(!allMedicationReminderIds.isEmpty()){
            medicationReminderRepository.updateReminderLastSendAt(allMedicationReminderIds, LocalDateTime.now());
        }
        log.info("약 알림 발송 스케줄러 종료");
    }

    // 매일 오전 3시에 한달동안 사용하지 않은 Fcm Token 제거
    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanUpInactiveTokens(){
        log.info("휴먼 FCM 토큰 정리 시작");
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        fcmTokenRepository.deleteByLastUsedAtBefore(oneMonthAgo);
        log.info("휴먼 FCM 토큰 정리 완료");
    }
}
