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
@Schema(description = "닉네임 중복 확인 요청 정보")
public class UserNameCheckRequestDto {

    @Schema(
            description = "중복 확인을 진행할 닉네임",
            example = "조지123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "닉네임을 입력해주세요")
    private String nickname;

}
