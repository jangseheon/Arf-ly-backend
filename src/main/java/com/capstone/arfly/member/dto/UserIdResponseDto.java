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
@Schema(description = "아이디 담고 있는 응답 정보")
public class UserIdResponseDto {

    @Schema(
            description = "유저 ID",
            example = "test1234",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String userId;
}
