package com.capstone.arfly.common.service;


import com.capstone.arfly.common.dto.MedicationAlarmDto;
import com.capstone.arfly.common.exception.EmptyTokenException;
import com.capstone.arfly.common.exception.InvalidTokenException;
import com.capstone.arfly.common.exception.MissingTokenException;
import com.capstone.arfly.common.exception.TokenExpiredException;
import com.capstone.arfly.common.exception.TokenRevokedException;
import com.capstone.arfly.member.dto.PhoneAuthInfoDto;
import com.google.firebase.ErrorCode;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseService {
    private final FirebaseMessaging firebaseMessaging;

    //토큰을 검증하고 사용자의 정보를 추출
    public PhoneAuthInfoDto verifyTokenAndGetInfo(String token) {
        try {
            // 토큰 확인 및 형식 검증
            if (token == null || !token.startsWith("Bearer ")) {
                throw new InvalidTokenException();
            }

            // Bearer 제거
            String idToken = token.substring(7);
            if (idToken.isBlank()) {
                throw new EmptyTokenException();
            }

            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            String uid = decodedToken.getUid();
            String phoneNumber = (String) decodedToken.getClaims().get("phone_number");

            if (uid == null || uid.isBlank()||phoneNumber == null || phoneNumber.isBlank()) {
                throw new MissingTokenException();
            }

            return PhoneAuthInfoDto.builder().uid(uid).phoneNumber(phoneNumber).build();

        } catch (FirebaseAuthException e) {
            throw switch (e.getAuthErrorCode()) {
                case REVOKED_ID_TOKEN -> new TokenRevokedException();
                case EXPIRED_ID_TOKEN -> new TokenExpiredException();
                case INVALID_ID_TOKEN -> new InvalidTokenException();
                default -> new InvalidTokenException();
            };
        }
    }


    //여러 건의 알림을 리스트로 받아 실제 푸시 알림을 발송
    public Set<Long> sendAllNotifications(List<MedicationAlarmDto> alarmList){
        if(alarmList == null || alarmList.isEmpty()){
            return Collections.emptySet();
        }
        //  일괄 전송을 위한 MessageList 생성
        List<Message> messages = alarmList.stream()
                .map(alarm -> Message.builder()
                        .setToken(alarm.token())
                        .setNotification(Notification.builder()
                                .setTitle(alarm.title())
                                .setBody(alarm.content())
                                .build())
                        .build())
                .toList();
        Set<Long> failedTokenSet = new HashSet<>();
        try{
            // 일괄 전송
            BatchResponse response = firebaseMessaging.sendEach(messages);

            //발송 결과 처리
            for(int i =0; i<response.getResponses().size();i++){
                SendResponse sendResponse = response.getResponses().get(i);
                MedicationAlarmDto alarm = alarmList.get(i);

                if(sendResponse.isSuccessful()){
                    log.debug("알림 발송 성공 - Target: {}, Title:{}",alarm.token(),alarm.title());
                }//실패한 경우 Set에 추가
                else{
                    FirebaseMessagingException exception = sendResponse.getException();
                    MessagingErrorCode errorCode = exception.getMessagingErrorCode();
                    if(errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT){
                        log.warn("알림 발송 실패 - ErrorCode:{}, Target: {}, Title:{}",errorCode,alarm.token(),alarm.title());
                        failedTokenSet.add(alarm.fcmTokenId());
                    }
                    else{// 네트워크 문제 혹은 파이어베이스 서버 임시 점검인 경우
                        log.error("알림 발송 실패(SERVER_ERROR): Token: {}, Title: {}", alarm.token(), alarm.title(), exception);
                        failedTokenSet.add(alarm.fcmTokenId());
                    }
                }


            }
            //일괄 전송 실패 시 모든 FCM Token 반납
        }catch (FirebaseMessagingException e){
            log.error("FCM 일괄 발송 중 에러 발생",e);
            return alarmList.stream().map(MedicationAlarmDto::fcmTokenId).collect(Collectors.toSet());
        }

        return  failedTokenSet;
    }


}