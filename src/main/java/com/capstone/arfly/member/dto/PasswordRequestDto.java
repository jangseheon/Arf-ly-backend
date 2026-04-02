package com.capstone.arfly.member.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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
@Schema(description = "비밀번호 찾기 요청 정보")
public class PasswordRequestDto {

    @Schema(description = "사용자 아이디", example = "member1234", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호를 찾을 아이디를 입력해주세요.")
    private String userId;
}
