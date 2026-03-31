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
@Schema(description = "파이어베이스 인증 정보 DTO (UID 및 전화번호)")
public class FirebaseToken {

    @Schema(
            description = "파이어베이스에서 추출한 사용자 고유 ID (sub)",
            example = "ABC1234567890",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Firebase UID(tokenId)는 필수입니다.")
    private String tokenId;

    @Schema(
            description = "파이어베이스 토큰에서 추출한 인증된 전화번호",
            example = "+821012345678",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "전화번호 정보가 누락되었습니다.")
    private String phoneNumber;
}