package com.capstone.arfly.notification.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "약 알람 수정 요청 정보")
public class UpdateMedicationReminderRequest {

    @Schema(description = "수정할 알람 제목 (변경 없으면 null)", example = "점심 영양제")
    @Size(min = 1, max = 20, message = "제목은 1자 이상 20자 이하로 입력해주세요.")
    private String title;

    @Schema(description = "수정할 알람 메모 (변경 없으면 null)", example = "비타민 C 포함")
    private String memo;

    @Schema(description = "수정할 알람 시간 (HH:mm:ss , 변경 없으면 null)", example = "13:00:00")
    private LocalTime reminderTime;
}
