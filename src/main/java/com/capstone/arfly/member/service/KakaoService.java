package com.capstone.arfly.member.service;

import com.capstone.arfly.common.exception.InvalidTokenException;
import com.capstone.arfly.common.exception.OauthAccessTokenException;
import com.capstone.arfly.member.dto.KakaoAccessTokenDto;
import com.capstone.arfly.member.dto.KakaoProfileDto;
import com.capstone.arfly.member.dto.RedirectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class KakaoService {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;
    @Value("${oauth.kakao.client-secret}")
    private String kakaoClientSecret;


    private final RestClient restClient = RestClient.create();

    public KakaoAccessTokenDto getAccessToken(RedirectDto redirectDto) {
        return restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(createTokenParams(redirectDto))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("카카오 토큰 발급 에러: {}", response.getStatusCode());
                    throw new OauthAccessTokenException(); // 에러 발생 시 즉시 중단
                })
                .body(KakaoAccessTokenDto.class); // body() 직접 사용
    }

    public KakaoProfileDto getKakaoProfile(String accessToken) {
        if (accessToken == null) {
            log.info("카카오 로그인 토큰 정보가 Null입니다.");
            throw new InvalidTokenException();
        }

        return restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("카카오 프로필 조회 에러: {}", response.getStatusCode());
                    throw new InvalidTokenException();
                })
                .body(KakaoProfileDto.class);
    }

    private MultiValueMap<String, String> createTokenParams(RedirectDto redirectDto) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", redirectDto.getRedirectUrl());
        params.add("code", redirectDto.getAuthCode());
        params.add("client_secret", kakaoClientSecret);
        return params;
    }
}