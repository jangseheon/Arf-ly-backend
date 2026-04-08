package com.capstone.arfly.notification;

import com.capstone.arfly.common.exception.ErrorResponse;
import com.capstone.arfly.notification.dto.CreateMedicationReminderRequest;
import com.capstone.arfly.notification.dto.GetMedicationRemindersResponse;
import com.capstone.arfly.notification.dto.UpdateMedicationReminderRequest;
import com.capstone.arfly.notification.service.MedicationReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MedicationReminder", description = "약 알림 서비스 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class MedicationReminderController {
    private final MedicationReminderService medicationReminderService;


    @Operation(summary = "복약 알람 생성", description = "새로운 복약 알람 스케줄을 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "알람 생성 성공(Body Data X)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 유효성 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/alarms")
    public ResponseEntity<?> createMedicationReminder(
            @Valid @RequestBody CreateMedicationReminderRequest reminderRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        medicationReminderService.createReminder(reminderRequest, userId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "복약 알람 목록 조회", description = "로그인한 사용자가 등록한 모든 복약 알람 리스트를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = GetMedicationRemindersResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/alarms")
    public ResponseEntity<?> getMedicationReminders(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<GetMedicationRemindersResponse> reminderList = medicationReminderService.getReminderList(userId);
        return new ResponseEntity<>(reminderList, HttpStatus.OK);
    }

    @Operation(summary = "복약 알람 수정", description = "기존 복약 알람의 정보를 변경한다. 제목, 시간, 메모 중 변경이 필요한 필드만 전송하면 해당 항목만 수정.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "약 알람 수정 성공(Body 데이터 X)"
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(글자 수 제한 초과 등 입력 값 유효성 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401", description = "인증되지 않은 사용자(엑세스 토큰 오류)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "알람을 찾을 수 없음 (ID가 존재하지 않거나 본인의 알람이 아님)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/alarms/{alarmId}")
    public ResponseEntity<?> updateMedicationReminder(@PathVariable(name = "alarmId") Long alarmId,
                                                      @Valid @RequestBody UpdateMedicationReminderRequest updateReminderRequest,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        medicationReminderService.updateMedicationReminder(alarmId, updateReminderRequest, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
