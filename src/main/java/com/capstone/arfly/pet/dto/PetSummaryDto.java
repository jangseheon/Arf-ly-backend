package com.capstone.arfly.pet.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PetSummaryDto {
    @Schema(description = "반려동물 ID", example = "1")
    private Long petId;

    @Schema(description = "반려동물 이름", example = "코코")
    private String name;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profiles/user2.jpg")
    private String profileImageUrl;
}
