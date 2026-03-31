package com.capstone.arfly.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "개별 약관 동의 결과 정보")
public class UserAgreementDto {

    @Schema(
            description = "동의한 약관의 고유 ID (PK)",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "약관 ID는 필수 입력 항목입니다.")
    private Long termId;

    @Schema(
            description = "약관 동의 여부 (true: 동의 / false: 미동의)",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "약관 동의 여부를 선택해주세요.")
    private Boolean termsOfServiceAgreed;
}
