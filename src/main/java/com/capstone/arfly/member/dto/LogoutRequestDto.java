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
@Schema(description = "로그아웃 요청 정보")
public class LogoutRequestDto {

    @Schema(
            description = "로그아웃할 사용자의 리프레시 토큰",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "로그아웃을 위해 리프레시 토큰이 필요합니다.")
    private String refreshToken;
}