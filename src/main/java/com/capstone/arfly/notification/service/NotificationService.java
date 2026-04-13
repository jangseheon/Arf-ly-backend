package com.capstone.arfly.notification.service;

import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.repository.MemberRepository;
import com.capstone.arfly.notification.domain.FcmToken;
import com.capstone.arfly.notification.dto.FcmTokenRequest;
import com.capstone.arfly.notification.repository.FcmTokenRepository;
import com.capstone.arfly.notification.repository.MedicationReminderRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;
    private final MedicationReminderRepository medicationReminderRepository;

    public void registerFcmToken(FcmTokenRequest fcmTokenRequest, Long userId){
        // Proxy Member 객체 생성
        Member member = memberRepository.getReferenceById(userId);

        // 토큰 존재 여부 확인 및 처리
        fcmTokenRepository.findByToken(fcmTokenRequest.getFcmToken())
                .ifPresentOrElse(
                        existingToken -> {
                            // 기존 토큰이 있다면 주인만 최신화
                            existingToken.updateMember(member);
                        },
                        () -> {
                            // 신규 토큰이라면 저장
                            FcmToken newToken = FcmToken.builder()
                                    .member(member)
                                    .token(fcmTokenRequest.getFcmToken())
                                    .deviceType(fcmTokenRequest.getDeviceType())
                                    .build();
                            fcmTokenRepository.save(newToken);
                        }
                );
    }

    //알람 발송 후 토큰 업데이트 및 알람 업데이트
    public void updateAlarmResults(List<Long> successFcmTokenIds, List<Long>failedFcmTokenIds, List<Long> reminderIds){

        if(!reminderIds.isEmpty()){
            medicationReminderRepository.updateReminderLastSendAt(reminderIds, LocalDateTime.now());
        }

        // 발송에 성공한 FCM Token 업데이트
        if(!successFcmTokenIds.isEmpty()){
            fcmTokenRepository.updateTokenLastUsedAt(successFcmTokenIds,LocalDateTime.now());
        }
        //발송에 실패한 FCM Token 삭제
        if(!failedFcmTokenIds.isEmpty()){
            fcmTokenRepository.deleteAllByIdInBatch(failedFcmTokenIds);
        }

    }
}
