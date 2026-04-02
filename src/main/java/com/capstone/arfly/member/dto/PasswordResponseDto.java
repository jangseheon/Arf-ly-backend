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
@Schema(description = "비밀번호 찾기 응답 정보")
public class PasswordResponseDto {

    @Schema(
            description = "패스워드 재설정을 위한 토큰(패스워드 재설정 요청 때 해당 토큰 함께 첨부)",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String passwordResetToken;

}
