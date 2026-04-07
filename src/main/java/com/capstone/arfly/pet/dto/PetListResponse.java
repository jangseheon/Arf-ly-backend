package com.capstone.arfly.pet.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "내 반려동물 목록 응답")
public class PetListResponse {

    @Schema(description = "반려동물 요약 정보 목록")
    private List<PetSummaryDto> pets;

}
