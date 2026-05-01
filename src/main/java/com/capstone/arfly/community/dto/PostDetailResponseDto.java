package com.capstone.arfly.community.dto;

import com.capstone.arfly.common.dto.FileDetailDto;
import com.capstone.arfly.community.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "게시글 상세 정보 응답")
public class PostDetailResponseDto {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "작성자 닉네임", example = "유저123")
    private String authorNickname;

    @Schema(description = "제목", example = "게시글 제목입니다.")
    private String title;

    @Schema(description = "내용", example = "게시글 내용입니다.")
    private String content;

    @Schema(description = "좋아요 수", example = "10")
    private Integer likeCount;

    @Schema(description = "게시글 생성일")
    private LocalDateTime createdAt;

    @Schema(description = "게시물 이미지 목록")
    private List<PostDetailFileDto> images;

    @Schema(description = "댓글 목록")
    private List<CommentDetailResponseDto> comments;





    public static PostDetailResponseDto makePostDetailResponse(Post post, List<CommentDetailResponseDto>
                                                        comments, List<PostDetailFileDto> files){
        return PostDetailResponseDto.builder()
                .id(post.getId()).authorNickname(post.getMember().getNickName()
                ).title(post.getTitle()).content(post.getContent()).likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt()).images(files).comments(comments).build();
    }
}
