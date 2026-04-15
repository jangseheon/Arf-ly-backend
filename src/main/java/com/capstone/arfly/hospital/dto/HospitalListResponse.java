package com.capstone.arfly.hospital.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "지도 리스트 조회 응답 정보")
public class HospitalListResponse {

    @Schema(description = "병원 고유 ID(구글 places api에서 주는 고유한 값입니다. 상세정보 요청할 때 해당 id를 주면 됩니다)", example = "ChIJCbxn16IOZjURfh9ULhjaA2s")
    public String id;

    @Schema(description = "병원 이름", example = "영대연합동물병원")
    public String hospitalName;

    @Schema(description = "병원 위도", example = "35.8385071")
    public Double latitude;

    @Schema(description = "병원 경도", example = "128.7557608")
    public Double longitude;

    @Schema(description = "병원 도로명 주소", example = "대한민국 경상북도 경산시 조영동 311-12")
    public String roadAddress;

    @Schema(description = "오픈 여부", example = "true")
    public boolean opened;

    @Schema(description = "병원 사진 주소", example = "/api/v1/hospitals/photo?name=places/ChIJNx.../photos/AUQs...")
    public String imageUrl;
}
