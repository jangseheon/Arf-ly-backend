package com.capstone.arfly.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;


import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Schema(description = "게시글 목록 조회 응답(무한 스킄롤)")
public class PostListResponseDto
{

    @Schema(description = "게시글 목록")
    private List<PostSummary> posts;

    @Schema(description = "페이지 메타 정보")
    private Meta meta;

    @Getter
    @Builder
    public static class PostSummary {
        @Schema(description = "게시글 ID", example = "1")
        private Long id;

        @Schema(description = "게시글 제목", example = "우리 집 강아지 첫 산책!")
        private String title;

        @Schema(description = "썸네일 이미지 URL 목록 (최대 3개)", example = "[\"https://s3.../img1.jpg\", \"https://s3.../img2.jpg\"]")
        private List<String> thumbnails;

        @Schema(description = "비디오 파일 포함 여부 (true면 프론트에서 재생 아이콘 표시)", example = "true")
        private boolean hasVideo;

        @Schema(description = "전체 첨부파일 개수 (프론트에서 '...' 같은 뱃지를 띄우기 위함)", example = "5")
        private int totalMediaCount;

        @Schema(description = "좋아요 수", example = "200")
        private Integer likeCount;

        @Schema(description = "작성일자", example = "2026-03-01")
        private LocalDate createdAt;

        @Schema(description = "작성자 닉네임", example = "김민준")
        private String nickname;
    }

    @Getter
    @Builder
    public static class Meta {
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private boolean hasNext;

        @Schema(description = "다음 스크롤 조회를 위한 커서 (마지막 게시글 ID)", example = "119")
        private Long nextCursor;

        @Schema(description = "요청한 페이지 사이즈", example = "20")
        private Integer size;

        @Schema(description = "검색 결과 총 개수 (검색 첫 페이지 조회 시에만 포함, 일반 조회 시에는 null)", example = "148")
        private Long totalCount;
    }
}
