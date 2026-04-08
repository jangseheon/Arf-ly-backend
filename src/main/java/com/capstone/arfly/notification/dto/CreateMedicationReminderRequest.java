package com.capstone.arfly.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "약 알람 생성 요청 정보")
public class CreateMedicationReminderRequest {

    @Schema(
            description = "알람의 제목(1자 이상 20자 이하)",
            example = "아침 영양제 복용",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "약 알림 제목은 필수 항목입니다.")
    @Size(min = 1, max = 20, message = "알람 제목은 1자 이상 20자 이하로 입력해주세요.")
    private String title;

    @Schema(
            description = "알림 발송 시간(HH:mm:ss)",
            example = "08:30:00",
            requiredMode = RequiredMode.REQUIRED
    )
    @NotNull(message = "알람 시간은 필수 항목입니다.")
    private LocalTime reminderTime;

    @Schema(
            description = "알람 메모 및 상세 내용",
            example = "식사 후 30분 뒤에 복용할 것"
    )
    private String memo;

}
