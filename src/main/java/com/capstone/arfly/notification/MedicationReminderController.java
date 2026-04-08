package com.capstone.arfly.notification;

import com.capstone.arfly.common.exception.ErrorResponse;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.dto.MemberCreateDto;
import com.capstone.arfly.member.dto.TokenResponseDto;
import com.capstone.arfly.notification.dto.CreateMedicationReminderRequest;
import com.capstone.arfly.notification.service.MedicationReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<?> createMedicationReminder(@Valid @RequestBody CreateMedicationReminderRequest reminderRequest,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        medicationReminderService.createReminder(reminderRequest,userId);
        return  new ResponseEntity<>(HttpStatus.CREATED);
    }
}
