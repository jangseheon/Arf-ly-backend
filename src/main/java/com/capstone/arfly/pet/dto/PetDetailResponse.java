package com.capstone.arfly.pet.dto;


import com.capstone.arfly.pet.domain.Sex;
import com.capstone.arfly.pet.domain.Species;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "반려동물 상세 정보 응답")
public class PetDetailResponse {
    @Schema(description = "반려동물 이름", example = "코코")
    private String name;

    @Schema(description = "동물 종류", example = "DOG")
    private Species species; // 프론트엔드 예시엔 "강아지"지만, DB 타입에 맞춰 DOG로 내려갑니다.

    @Schema(description = "품종 이름", example = "말티즈")
    private String breed; // 기존에는 breeds 였지만 프론트엔드 명세에 맞춰 breed로 작성했습니다.

    @Schema(description = "성별", example = "FEMALE")
    private Sex sex; // 프론트엔드 JSON 예시엔 없었지만 DB에 저장된 값이니 추가했습니다.

    @Schema(description = "중성화 여부", example = "true")
    private Boolean neutered;

    @Schema(description = "출생년도", example = "2010")
    private Integer birth; // 🌟 아래 중요 체크 포인트 참고!

    @Schema(description = "몸무게", example = "3.5")
    private Double weight;

    @Schema(description = "알러지 목록", example = "[\"소고기\", \"꽃가루\"]")
    private List<String> allergies;

    @Schema(description = "특이사항 및 메모", example = "겁이 많고 사람을 좋아해요.")
    private String note;

    @Schema(description = "프로필 이미지 URL", example = "https://arfly-bucket.s3.ap-northeast-2.amazonaws.com/pets/123_dog.jpg")
    private String profileImageUrl;

}
