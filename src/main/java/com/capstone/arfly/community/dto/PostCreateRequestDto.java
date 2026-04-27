package com.capstone.arfly.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "게시글 작성 요청")
public class PostCreateRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 50, message = "제목은 50자 이내로 입력해주세요.")
    @Schema(description = "게시글 제목", example = "우리 강아지 좀 보세요!", requiredMode =  RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Schema(description = "게시글 내용", example = "너무 귀엽지 않나요?", requiredMode =  RequiredMode.REQUIRED)
    private String content;
}
