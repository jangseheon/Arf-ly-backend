package com.capstone.arfly.pet.dto;

import com.capstone.arfly.pet.domain.Sex;
import com.capstone.arfly.pet.domain.Species;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "반려동물 정보 수정 요청")
public class UpdatePetRequest {

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

    @Schema(description = "출생일", example = "2010-03-01")
    private String birth;

    @Schema(description = "몸무게", example = "2.5")
    private Double weight;

    @Schema(description = "알러지 목록", example = "[\"돼지고기\", \"밀가루\"]")
    private List<String> allergies; // JSON 예시가 allergies로 되어 있어서 맞췄습니다!

    @Schema(description = "특이사항 및 메모", example = "겁이 없어요")
    private String note;
}