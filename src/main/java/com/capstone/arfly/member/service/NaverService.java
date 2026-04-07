package com.capstone.arfly.member.service;

import com.capstone.arfly.common.exception.InvalidTokenException;
import com.capstone.arfly.common.exception.OauthAccessTokenException;
import com.capstone.arfly.member.dto.NaverAccessTokenDto;
import com.capstone.arfly.member.dto.NaverProfileDto;
import com.capstone.arfly.member.dto.NaverRedirectDto;
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
public class NaverService {

    @Value("${oauth.naver.client_id}")
    private String naverClientId;
    @Value("${oauth.naver.client-secret}")
    private String naverClientSecret;


    private final RestClient restClient = RestClient.create();

    public NaverAccessTokenDto getAccessToken(NaverRedirectDto redirectDto) {
        return restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(createTokenParams(redirectDto))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("네이버 토큰 발급 에러: {}", response.getStatusCode());
                    throw new OauthAccessTokenException(); // 에러 발생 시 즉시 중단
                })
                .body(NaverAccessTokenDto.class);
    }

    public NaverProfileDto getNaverProfile(String accessToken) {
        if (accessToken == null) {
            log.info("네이버 로그인 토큰 정보가 Null입니다.");
            throw new InvalidTokenException();
        }

        return restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("네이버 프로필 조회 에러: {}", response.getStatusCode());
                    throw new InvalidTokenException();
                })
                .body(NaverProfileDto.class);
    }

    private MultiValueMap<String, String> createTokenParams(NaverRedirectDto redirectDto) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("code", redirectDto.getAuthCode());
        params.add("state", redirectDto.getState());
        return params;
    }
}