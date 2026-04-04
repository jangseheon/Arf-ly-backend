package com.capstone.arfly.pet.dto;

import com.capstone.arfly.pet.domain.PetAllergy;
import com.capstone.arfly.pet.domain.Sex;
import com.capstone.arfly.pet.domain.Species;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreatePetRequest {

    @Schema(description = "반려동물 이름", example = "코코")
    private String name;
    @Schema(description = "동물 종류", example = "DOG")
    private Species species;
    @Schema(description = "품종 이름", example = "말티즈")
    private String breeds;
    @Schema(description = "성별", example = "MALE")
    private Sex sex;
    @Schema(description = "중성화 여부", example = "true")
    private boolean neutered;
    @Schema(description = "출생일 (연도 추출용)", example = "2016-12-22")
    private String birth;
    @Schema(description = "몸무게 (kg 단위)", example = "3.5")
    private Double weight;
    @Schema(description = "특이사항 및 메모", example = "겁이 많고 사람을 좋아해요.")
    private String note;

    @Schema(description = "알러지 목록", example = "[\"소고기\", \"꽃가루\"]")
    private List<String> petAllergies;
}
