package com.capstone.arfly.member.dto;


import com.capstone.arfly.member.domain.Terms;
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
@Schema(description = "최신 약관 상세 정보 응답")
public class LatestTermsResponseDto {

    @Schema(description = "약관 고유 식별자 (ID)", example = "1")
    private Long termsId;

    @Schema(description = "약관 제목", example = "서비스 이용약관")
    private String title;

    @Schema(description = "약관 상세 내용", example = "제 1조...")
    private String content;

    @Schema(description = "필수 동의 여부 (true: 필수 / false: 선택)", example = "true")
    private Boolean required;

    @Schema(description = "화면 노출 순서 (낮은 숫자 우선)", example = "1")
    private int orderIndex;

    public static LatestTermsResponseDto from(Terms terms) {
        return LatestTermsResponseDto.builder()
                .termsId(terms.getId())
                .title(terms.getTitle())
                .content(terms.getContent())
                .required(terms.getRequired())
                .orderIndex(terms.getOrderIndex())
                .build();
    }


}
