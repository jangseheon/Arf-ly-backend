package com.capstone.arfly.member.service;


import com.capstone.arfly.common.exception.InvalidTokenException;
import com.capstone.arfly.common.exception.OauthAccessTokenException;
import com.capstone.arfly.member.dto.NaverAccessTokenDto;
import com.capstone.arfly.member.dto.NaverProfileDto;
import com.capstone.arfly.member.dto.NaverRedirectDto;
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
public class NaverService {
    @Value("${oauth.naver.client_id}")
    private String naverClientId;
    @Value("${oauth.naver.client-secret}")
    private String naverClientSecret;

    public NaverAccessTokenDto getAccessToken(NaverRedirectDto redirectDto){
        RestClient restClient = RestClient.create();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code",redirectDto.getAuthCode());
        params.add("client_id", naverClientId);
        params.add("state",redirectDto.getState());
        params.add("client_secret",naverClientSecret);
        params.add("grant_type","authorization_code");


        ResponseEntity<NaverAccessTokenDto> result= restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve().onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    log.error("네이버 토큰 발급 클라이언트 오류: {}", response.getStatusCode());
                    throw new OauthAccessTokenException();
                }).onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.error("네이버 서버 오류: {}", response.getStatusCode());
                    throw new RuntimeException("네이버 인증 서버 에러.");
                })
                .toEntity(NaverAccessTokenDto.class);
        return result.getBody();
    }

    public NaverProfileDto getNaverProfile(String accessToken){
        RestClient restClient = RestClient.create();
        ResponseEntity<NaverProfileDto> result= restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization","Bearer "+ accessToken)
                .retrieve().onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new InvalidTokenException();
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new RuntimeException("네이버 서버 에러.");
                })
                .toEntity(NaverProfileDto.class);
        return result.getBody();
    }
}
