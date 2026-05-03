package com.capstone.arfly.diagnosis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description="스마트 진단 기록 목록 조회 (무한 스크롤), 강아지별")
public class DiagnosisListResponseDto {

    @Schema(description = "진단 기록 목록")
    private List<DiagnosisSummary> diagnoses;

    @Schema(description = "페이지 메타 정보")
    private Meta meta;

    @Getter
    @Builder
    public static class DiagnosisSummary {
        @Schema(description = "진단 기록 ID", example = "3")
        private Long id;

        @Schema(description = "진단 일자 (형식: YYYY-MM-DD 또는 프론트가 원하는 형식)", example = "2025-05-21")
        private String createdAt;

        @Schema(description = "진단 시 촬영한 이미지 URL", example = "https://s3.../diagnosis_img.jpg")
        private String imageUrl;

        // 👇 피그마 시안을 반영하여 새롭게 추가된 필드들 👇

        @Schema(description = "진단된 병명", example = "진균성 피부염 (Dermatophytosis)")
        private String diseaseName;

        @Schema(description = "반려동물 이름", example = "누룽지")
        private String petName;

        @Schema(description = "반려동물 품종", example = "시고르자브종")
        private String breedName;

        @Schema(description = "반려동물 태어난 연도 (프론트엔드에서 나이 계산용)", example = "2019")
        private Integer birthYear;
    }

    @Getter
    @Builder
    public static class Meta {
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private boolean hasNext;

        @Schema(description = "다음 스크롤 조회를 위한 커서 (마지막 진단 기록 ID)", example = "119")
        private Long nextCursor;

        @Schema(description = "요청한 페이지 사이즈", example = "20")
        private Integer size;
    }

}
