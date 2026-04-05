package com.capstone.arfly.member.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "회원 프로필 상세 정보 응답")
public class UserProfileResponse {

    @Schema(description = "회원 ID", example = "1")
    private Long userId;

    @Schema(description = "닉네임", example = "이름")
    private String nickname;

    @Schema(description = "수의사 여부", example = "false")
    private Boolean doctor;

    @Schema(description = "알림 수신 여부", example = "true")
    private boolean notificationEnabled = true;

    @Schema(description = "전화번호", example = "010-1111-2222")
    private String phoneNumber;

    @Schema(description = "위도", example = "35.1234")
    private Double latitude;

    @Schema(description = "경도", example = "128.1234")
    private Double longitude; // 프론트엔드 스펠링에 맞춤

    @Schema(description = "도로명 주소", example = "경북 경산시 ~")
    private String roadAddress;

}
