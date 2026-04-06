package com.capstone.arfly.member.service;

import com.capstone.arfly.common.exception.InvalidTokenException;
import com.capstone.arfly.common.exception.OauthAccessTokenException;
import com.capstone.arfly.member.dto.GoogleAccessTokenDto;
import com.capstone.arfly.member.dto.GoogleProfileDto;
import com.capstone.arfly.member.dto.RedirectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;


@Service
@Slf4j
public class GoogleService {

    private final RestClient restClient = RestClient.create();

    @Value("${oauth.google.client-id}")
    private String googleClientId;
    @Value("${oauth.google.client-secret}")
    private String googleSecret;
    @Value("${oauth.google.grant-type}")
    private String googleGrantType;

    public GoogleAccessTokenDto getAccessToken(RedirectDto redirectDto) {
        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .body(createTokenParams(redirectDto))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("구글 토큰 발급 에러: {}", response.getStatusCode());
                    throw new OauthAccessTokenException();
                })
                .body(GoogleAccessTokenDto.class);
    }

    public GoogleProfileDto getGoogleProfile(String accessToken) {
        if (accessToken == null) {
            log.info("구글 로그인 토큰 정보가 Null입니다.");
            throw new InvalidTokenException();
        }

        return restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("구글 프로필 조회 에러: {}", response.getStatusCode());
                    throw new InvalidTokenException();
                })
                .body(GoogleProfileDto.class);
    }

    private MultiValueMap<String, String> createTokenParams(RedirectDto redirectDto) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", redirectDto.getAuthCode());
        params.add("client_id", googleClientId);
        params.add("client_secret", googleSecret);
        params.add("redirect_uri", redirectDto.getRedirectUrl());
        params.add("grant_type", googleGrantType);
        return params;
    }
}