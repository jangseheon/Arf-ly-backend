package com.capstone.arfly.notification.dto;


import com.capstone.arfly.notification.domain.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "FCM Token 등록 및 업데이트 요청 DTO")
public class FcmTokenRequest {
    @Schema(
            description = "FCM 토큰 (브라우저/ 기기 식별자) ",
            example = "fcm_token_sample_12345...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "FCM 토큰은 필수 항목입니다.")
    private String fcmToken;

    @Schema(
            description = "접속 기기 환경 (ANDROID, IOS,DESKTOP 만 가능 )",
            example = "DESKTOP",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "디바이스 타입은 필수 항목입니다.")
    private DeviceType deviceType;
}
