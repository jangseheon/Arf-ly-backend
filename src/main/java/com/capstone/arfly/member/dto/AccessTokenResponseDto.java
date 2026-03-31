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
@Schema(description = "새로 발급된 액세스 토큰 응답 정보")
public class AccessTokenResponseDto {

    @Schema(
            description = "새로 발급된 액세스 토큰",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String accessToken;
}