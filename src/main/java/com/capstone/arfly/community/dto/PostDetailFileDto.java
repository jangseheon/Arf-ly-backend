package com.capstone.arfly.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "파일 상세 정보 응답")
public record PostDetailFileDto(
        @Schema(description = "파일 id")
        long fileId,
        @Schema(description = "파일 이미지 URL")
        String fileKey) {}
