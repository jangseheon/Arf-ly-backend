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
@Schema(description = "네이버 소셜 로그인 인증 요청 정보")
public class NaverRedirectDto {

    @Schema(
            description = "네이버 OAuth 서버로부터 발급받은 인가 코드",
            example = "aBcDeFgHiJ12345",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "인가 코드(authCode)는 필수입니다.")
    private String authCode;

    @Schema(
            description = "프론트 애플리케이션이 생성한 상태 토큰",
            example = "987654321",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "보안을 위한 state 값은 필수입니다.")
    private String state;
}