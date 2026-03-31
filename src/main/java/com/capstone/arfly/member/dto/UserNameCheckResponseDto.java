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
@Schema(description = "닉네임 중복 확인 응답 정보")
public class UserNameCheckResponseDto {

    @Schema(
            description = "닉네임 사용 가능 여부 (true: 사용 가능 / false: 중복됨)",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean isAvailable;

    public static UserNameCheckResponseDto from(boolean isAvailable) {
        return UserNameCheckResponseDto.builder()
                .isAvailable(isAvailable)
                .build();
    }
}