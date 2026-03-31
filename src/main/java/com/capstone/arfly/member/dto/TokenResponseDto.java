package com.capstone.arfly.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "인증 성공 시 발급되는 토큰 정보")
public class TokenResponseDto {

    @Schema(
            description = "액세스 토큰",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String accessToken;

    @Schema(
            description = "리프레시 토큰",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;
}