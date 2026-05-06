package com.capstone.arfly.diagnosis.dto;

import com.capstone.arfly.pet.domain.Sex;
import com.capstone.arfly.pet.domain.Species;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description="스마트 진단 기록 조회")
public class DiagnosisResponseDto {

    @Schema(description = "진단 기록 아이디", example = "1")
    private Long id;

    @Schema(description = "강아지 이름", example = "코코")
    private String petName;

    @Schema(description = "동물 종류", example = "DOG")
    private Species species;

    @Schema(description = "품종 이름", example = "말티즈")
    private String breed;

    @Schema(description = "성별", example = "FEMALE")
    private Sex sex;

    @Schema(description = "중성화 여부", example = "true")
    private Boolean neutered;

    @Schema(description = "출생년도", example = "2010")
    private Integer birth;

    @Schema(description = "진단 사진 url", example = "https://example.com/profiles/user2.jpg")
    private String imageUrl;

    @Schema(description = "진단명", example = "습진")
    private String diseaseName;

    @Schema(description = "확률", example = "75.4")
    private Double probability;

    @Schema(description = "관리법", example = "1. 잘 씻겨주기\n주 1~2회 ...")
    private String management;
}
