package com.capstone.arfly.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "사용자의 검색 기록 검색 응답 정보")
public class GetRecentSearchResponseDto {

    @Schema(
            description = "검색했던 키워드",
            example = "강아지 영양제 추천",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String keyword;
}
