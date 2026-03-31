package com.capstone.arfly.member.service;

import com.capstone.arfly.common.exception.InvalidTokenException;
import com.capstone.arfly.common.exception.OauthAccessTokenException;
import com.capstone.arfly.member.dto.GoogleAccessTokenDto;
import com.capstone.arfly.member.dto.GoogleProfileDto;
import com.capstone.arfly.member.dto.RedirectDto;
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
@Transactional
@Slf4j
public class GoogleService {

    @Value("${oauth.google.client-id}")
    private String googleClientId;
    @Value("${oauth.google.client-secret}")
    private String googleSecret;

    @Value("${oauth.google.grant-type}")
    private String googleGrantType;

    //AccessToken을 발급받기 위해 필요한 Meta data가 필요
    // 인가코드, clientId, client_secret, redirect_uri,grant_type
    public GoogleAccessTokenDto getAccessToken(RedirectDto redirectDto){
        RestClient restClient = RestClient.create();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code",redirectDto.getAuthCode());
        params.add("client_id",googleClientId);
        params.add("client_secret",googleSecret);
        params.add("redirect_uri",redirectDto.getRedirectUrl());
        params.add("grant_type",googleGrantType);


        ResponseEntity<GoogleAccessTokenDto> result= restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                //retrieve는 응답 body 값만 추출하는 메서드
                .retrieve().onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    log.error("구글 토큰 발급 클라이언트 오류: {}", response.getStatusCode());
                    throw new OauthAccessTokenException();
                }).onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.error("구글 서버 오류: {}", response.getStatusCode());
                    throw new RuntimeException("구글 인증 서버 에러.");
                })
                .toEntity(GoogleAccessTokenDto.class);
        return result.getBody();
    }

    public GoogleProfileDto getGoogleProfile(String accessToken){
        RestClient restClient = RestClient.create();
        ResponseEntity<GoogleProfileDto> result= restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization","Bearer "+ accessToken)
                .retrieve().onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new InvalidTokenException();
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("구글 서버 에러.");
                })
                .toEntity(GoogleProfileDto.class);
        return result.getBody();
    }


}
