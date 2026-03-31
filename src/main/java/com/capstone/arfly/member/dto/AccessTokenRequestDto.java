package com.capstone.arfly.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "액세스 토큰 재발급 요청 정보")
public class AccessTokenRequestDto {

    @Schema(
            description = "기존에 발급받았던 리프레시 토큰",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "리프레시 토큰은 필수 항목입니다.")
    private String refreshToken;
}