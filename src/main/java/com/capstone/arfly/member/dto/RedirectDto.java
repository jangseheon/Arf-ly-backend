package com.capstone.arfly.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "소셜 로그인(Google, Kakao) 인증을 위한 요청 DTO")
@Getter
@Builder
public class RedirectDto {

    @Schema(
            description = "OAuth 서버로부터 발급받은 인가 코드 (Authorization Code)",
            example = "4/0AdQt8qh...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "인가 코드(authCode)는 필수입니다.")
    private String authCode;

    @Schema(
            description = "인가 코드를 받을 때 사용했던 Redirect URI (프론트엔드 주소)",
            example = "http://localhost:3000/oauth/callback",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Redirect URI가 누락되었습니다.")
    private String redirectUrl;
}
