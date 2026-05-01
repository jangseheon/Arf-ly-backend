package com.capstone.arfly.community.controller;

import com.capstone.arfly.community.dto.PostListResponseDto;
import com.capstone.arfly.community.dto.CommentRequestDto;
import com.capstone.arfly.community.dto.PostCreateRequestDto;
import com.capstone.arfly.community.dto.PostDetailResponseDto;
import com.capstone.arfly.community.dto.PostUpdateRequestDto;
import com.capstone.arfly.community.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @Operation(
            summary = "게시글 작성",
            description = "새로운 게시글을 작성합니다. 제목, 본문, 그리고 다중 파일(이미지/동영상) 업로드를 지원합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시글 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 만료 혹은 유효하지 않은 토큰)"),
            @ApiResponse(responseCode = "500", description = "서버 ERROR(EX.S3 Upload 과정 중 오류 발생)")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
            @Valid @RequestPart("request") PostCreateRequestDto requestDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        long userId = Long.parseLong(userDetails.getUsername());
        postService.createPost(userId, requestDto, files);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "특정 게시글의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 만료 혹은 유효하지 않은 토큰)")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable Long postId) {
        PostDetailResponseDto responseDto = postService.getPostDetail(postId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "댓글 달기",
            description = "특정 게시글에 댓글을 작성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "댓글 달기 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 본문의 멘션 형식과 전달된 ID 목록 불일치)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 만료 혹은 유효하지 않은 토큰)"),
            @ApiResponse(responseCode = "404", description = "게시글 혹은 멘션 대상 사용자를 찾을 수 없음")
    })
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        long userId = Long.parseLong(userDetails.getUsername());
        postService.createComment(postId, userId, requestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(
            summary = "게시글 수정",
            description = "특정 게시글을 수정합니다. 변경할 필드만 전달하면 됩니다. 작성자 본인만 수정할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 만료 혹은 유효하지 않은 토큰)"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음 (작성자 불일치)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 ERROR(EX.S3 Upload 과정 중 오류 발생)")
    })
    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @Valid @RequestPart("request") PostUpdateRequestDto requestDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        long userId = Long.parseLong(userDetails.getUsername());
        postService.updatePost(postId, userId, requestDto, files);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "게시글 삭제",
            description = "특정 게시글을 삭제합니다. 작성자 본인만 삭제할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 만료 혹은 유효하지 않은 토큰)"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자 불일치)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        long userId = Long.parseLong(userDetails.getUsername());
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "게시글 좋아요",
            description = "특정 게시글에 좋아요를 누르거나 취소합니다. (토글 방식)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 만료 혹은 유효하지 않은 토큰)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        long userId = Long.parseLong(userDetails.getUsername());
        postService.toggleLike(postId, userId);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "게시글 목록 조회 (무한 스크롤)", description = "최신순 또는 인기순(좋아요순)으로 게시글 목록을 무한 스크롤 방식으로 조회합니다.")
    @GetMapping
    public ResponseEntity<PostListResponseDto> getPosts(
            @Parameter(description = "정렬 방식 (latest: 최신순, likes: 좋아요순)", example = "latest")
            @RequestParam(defaultValue = "latest") String sort,

            @Parameter(description = "마지막으로 조회한 게시글 ID (첫 페이지 조회 시에는 비워두세요)", example = "119")
            @RequestParam(required = false) Long cursor,

            @Parameter(description = "한 번에 조회할 게시글 개수", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        PostListResponseDto response = postService.getPosts(sort, cursor, size);
        return ResponseEntity.ok(response);
    }

    // 검색 api 포인트
    @Operation(summary = "게시글 검색", description = "키워드로 게시글을 검색하고 총 검색 결과 개수를 함께 반환합니다.")
    @GetMapping("/search")
    public ResponseEntity<PostListResponseDto> searchPosts(
            @Parameter(description = "검색할 키워드", required = true)
            @RequestParam String keyword,

            @Parameter(description = "정렬 방식 (latest: 최신순, likes: 좋아요순)", example = "latest")
            @RequestParam(defaultValue = "latest") String sort,

            @Parameter(description = "마지막으로 조회한 게시글 ID (첫 페이지 조회 시에는 비워두세요)", example = "119")
            @RequestParam(required = false) Long cursor,

            @Parameter(description = "한 번에 조회할 게시글 개수", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        PostListResponseDto response = postService.searchPosts(keyword, sort, cursor, size);
        return ResponseEntity.ok(response);
    }


}
