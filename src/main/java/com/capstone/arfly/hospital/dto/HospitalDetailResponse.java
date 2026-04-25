package com.capstone.arfly.hospital.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "장소 상세 조회 응답 정보")
public class HospitalDetailResponse {

    @Schema(description = "병원 고유 ID(구글 places api에서 주는 고유한 값입니다. 상세정보 요청할 때 해당 id를 주면 됩니다)", example = "ChIJCbxn16IOZjURfh9ULhjaA2s")
    public String id;

    @Schema(description = "병원 이름", example = "영대연합동물병원")
    public String hospitalName;

    @Schema(description = "병원 도로명 주소", example = "대한민국 경상북도 경산시 조영동 311-12")
    public String roadAddress;

    @Schema(description = "병원 사진 주소(리스트, 최대 5개)", example = "/api/v1/hospitals/photo?name=places/ChIJNx.../photos/AUQs...")
    public List<String> imageUrl;

    @Schema(description = "운영 시간 리스트", example = "[11:00 ~ 20:00, 11:00 ~ 20:00, 11:00 ~ 20:00," +
            " 11:00 ~ 20:00, 11:00 ~ 20:00, 11:00 ~ 20:00, 휴무]")
    public List<String> operating;

    @Schema(description = "오픈 여부", example = "true")
    public boolean opened;


}
