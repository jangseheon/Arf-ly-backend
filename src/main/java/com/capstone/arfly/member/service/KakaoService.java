package com.capstone.arfly.member.service;


import com.capstone.arfly.common.exception.InvalidTokenException;
import com.capstone.arfly.common.exception.OauthAccessTokenException;
import com.capstone.arfly.member.dto.KakaoAccessTokenDto;
import com.capstone.arfly.member.dto.KakaoProfileDto;
import com.capstone.arfly.member.dto.RedirectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KakaoService {
    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;
    @Value("${oauth.kakao.client-secret}")
    private String kakaoClientSecret;

    public KakaoAccessTokenDto getAccessToken(RedirectDto redirectDto) {
        RestClient restClient = RestClient.create();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", redirectDto.getAuthCode());
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", redirectDto.getRedirectUrl());
        params.add("client_secret", kakaoClientSecret);
        params.add("grant_type", "authorization_code");

        ResponseEntity<KakaoAccessTokenDto> result = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve().onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    log.error("카카오 토큰 발급 클라이언트 오류: {}", response.getStatusCode());
                    throw new OauthAccessTokenException();
                }).onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.error("카카오 서버 오류: {}", response.getStatusCode());
                    throw new RuntimeException("카카오 인증 서버 에러.");
                })
                .toEntity(KakaoAccessTokenDto.class);
        return result.getBody();
    }

    public KakaoProfileDto getKakaoProfile(String accessToken) {
        RestClient restClient = RestClient.create();
        ResponseEntity<KakaoProfileDto> result = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new InvalidTokenException();
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("카카오 서버 에러");
                })
                .toEntity(KakaoProfileDto.class);
        return result.getBody();
    }
}
