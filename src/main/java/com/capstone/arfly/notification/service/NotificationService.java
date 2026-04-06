package com.capstone.arfly.notification.service;

import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.repository.MemberRepository;
import com.capstone.arfly.notification.domain.FcmToken;
import com.capstone.arfly.notification.dto.FcmTokenRequest;
import com.capstone.arfly.notification.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;

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
}
