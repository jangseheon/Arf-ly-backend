package com.capstone.arfly.member.service;


import com.capstone.arfly.common.exception.EmptyTokenException;
import com.capstone.arfly.common.exception.InvalidTokenException;
import com.capstone.arfly.common.exception.MissingTokenException;
import com.capstone.arfly.common.exception.TokenExpiredException;
import com.capstone.arfly.common.exception.TokenRevokedException;
import com.capstone.arfly.member.dto.PhoneAuthInfoDto;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FirebaseService {

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
            if (e.getAuthErrorCode() == AuthErrorCode.REVOKED_ID_TOKEN) {
                throw new TokenRevokedException();
            }
            if (e.getAuthErrorCode() == AuthErrorCode.EXPIRED_ID_TOKEN) {
                throw new TokenExpiredException();
            }
            if (e.getAuthErrorCode() == AuthErrorCode.INVALID_ID_TOKEN) {
                throw new InvalidTokenException();
            }
            throw new InvalidTokenException();
        }
    }
}