package com.capstone.arfly.diagnosis.controller;

import com.capstone.arfly.diagnosis.dto.DiagnosisListResponseDto;
import com.capstone.arfly.diagnosis.service.DiagnosisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pets/diagnoses")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @Operation(
            summary = "스마트 진단 기록 목록 조회 (무한 스크롤)",
            description = "사용자의 전체 스마트 진단 기록을 최신순으로 조회합니다. petId를 전달하면 특정 반려동물의 기록만 필터링합니다."
    )
    @GetMapping
    public ResponseEntity<DiagnosisListResponseDto> getDiagnosisList(
            @Parameter(description = "필터링할 반려동물 ID (입력하지 않으면 사용자의 모든 진단 기록 조회)", example = "1")
            @RequestParam(required = false) Long petId,

            @Parameter(description = "마지막으로 조회한 진단 기록 ID (첫 페이지 조회 시에는 비워두세요)", example = "119")
            @RequestParam(required = false) Long cursor,

            @Parameter(description = "한 번에 조회할 진단 기록 개수", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. 토큰에서 사용자 ID 추출
        Long memberId = Long.parseLong(userDetails.getUsername());

        // 2. 서비스 로직 호출
        DiagnosisListResponseDto response = diagnosisService.getDiagnosisList(memberId, petId, cursor, size);

        // 3. 200 OK 응답과 함께 DTO 반환
        return ResponseEntity.ok(response);
    }
}