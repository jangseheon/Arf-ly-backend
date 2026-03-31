package com.capstone.arfly.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "로그인 요청 정보")
public class MemberLoginDto {

    @Schema(
            description = "사용자 아이디 (영문/숫자 조합, 4~20자)",
            example = "member1234",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "아이디를 입력해주세요.")
    @Pattern(regexp = "^[A-Za-z0-9]{4,20}$", message = "아이디 형식이 올바르지 않습니다.")
    private String userId;

    @Schema(
            description = "비밀번호 (8~20자)",
            example = "password1234!",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하입니다.")
    private String password;
}