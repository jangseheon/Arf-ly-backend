package com.capstone.arfly.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "비밀번호 재설정 요청 정보")
public class PasswordResetRequestDto {
    @Schema(
            description = "패스워드 재설정 토큰",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String passwordResetToken;

    @Schema(description = "새로운 비밀번호 (8~20자)", example = "password1234!", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String newPassword;
}
