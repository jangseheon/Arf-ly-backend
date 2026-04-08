package com.capstone.arfly.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
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
@Schema(description = "약 알람 리스트 조회 응답 정보")
public class GetMedicationRemindersResponse {

    @Schema(description = "알람 고유 ID", example = "1")
    private Long id;

    @Schema(description = "알람 제목", example = "아침 영양제 복용")
    private String title;

    @Schema(description = "알람 메모", example = "식후 30분 뒤 복용")
    private String memo;

    @Schema(description = "알람 설정 시간", example = "08:30:00")
    private LocalTime reminderTime;

    @Schema(description = "알람 활성화 여부", example = "true")
    private Boolean active;

    @Schema(description = "알람 생성 일시", example = "2026-04-08T21:15:27")
    private LocalDateTime createdAt;

}
