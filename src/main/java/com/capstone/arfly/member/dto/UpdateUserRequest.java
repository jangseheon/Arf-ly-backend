package com.capstone.arfly.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "유저 프로필 수정 요청")
public class UpdateUserRequest {
    @Schema(description = "닉네임", example = "장세헌")
    private String nickname;

    @Schema(description = "변경할 비밀번호 (안 바꾸면 null 또는 빈 칸)", example = "asd123#")
    private String password;

    @Schema(description = "위도", example = "35.0")
    private Double latitude;

    @Schema(description = "경도", example = "128.0")
    private Double longitude;

    @Schema(description = "도로명 주소", example = "경북 경산시 ~")
    private String roadAddress;

    @Schema(description = "알림 수신 여부", example = "true")
    private boolean notificationEnabled;

}
