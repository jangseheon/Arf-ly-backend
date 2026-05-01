package com.capstone.arfly.community.service;

import com.capstone.arfly.common.constant.S3DIRNAME;
import com.capstone.arfly.common.domain.File;
import com.capstone.arfly.common.domain.FileType;
import com.capstone.arfly.common.dto.FileDetailDto;
import com.capstone.arfly.common.exception.*;
import com.capstone.arfly.common.repository.FileRepository;
import com.capstone.arfly.common.util.S3Uploader;
import com.capstone.arfly.community.constant.LikeEventType;
import com.capstone.arfly.community.domain.Comment;
import com.capstone.arfly.community.domain.CommentMention;
import com.capstone.arfly.community.domain.Post;
import com.capstone.arfly.community.domain.PostImage;
import com.capstone.arfly.community.dto.*;
import com.capstone.arfly.community.event.CommentCreatedEvent;
import com.capstone.arfly.community.event.PostLikeEvent;
import com.capstone.arfly.community.repository.CommentMentionRepository;
import com.capstone.arfly.community.repository.CommentRepository;
import com.capstone.arfly.community.repository.PostImageRepository;
import com.capstone.arfly.community.repository.PostLikeRepository;
import com.capstone.arfly.community.repository.PostRepository;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.repository.MemberRepository;
import jakarta.validation.Valid;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PostImageRepository postImageRepository;
    private final S3Uploader s3Uploader;
    private final PostWriter postWriter;
    private final RedisTemplate<String, String> redisTemplate;
    private final PostLikeRepository postLikeRepository;

    private static final String TOGGLE_LIKE_SCRIPT =
            "local added = redis.call('SADD', KEYS[1], ARGV[1]) " +
                    "if added == 0 then " +
                    "  redis.call('SREM', KEYS[1], ARGV[1]) " +
                    "  redis.call('DECR', KEYS[2]) " +
                    "  return -1 " +
                    "else " +
                    "  redis.call('INCR', KEYS[2]) " +
                    "  return 1 " +
                    "end";
    private static final DefaultRedisScript<Long> script = new DefaultRedisScript<>(TOGGLE_LIKE_SCRIPT,Long.class);


    @Transactional
    public void createComment(Long postId, long userId, CommentRequestDto requestDto) {
        //게시물 존재 여부 확인
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        Member commenter = memberRepository.getReferenceById(userId);
        Set<Long> mentionIds = requestDto.getMentionedUserIds();
        boolean hasMentions = mentionIds != null && !mentionIds.isEmpty();

        //내용 내 id 목록과 mentionIdList 일치 여부 확인
        validateContentMentions(requestDto.getContent(), mentionIds);

        Comment newComment = Comment.builder().post(post).member(commenter).content(requestDto.getContent()).build();
        commentRepository.save(newComment);

        //metionUserIds에 존재하는 id가 존재하는지 여부 확인
        if (hasMentions) {
            List<Member> mentionedUsers = memberRepository.findAllById(mentionIds);

            if (mentionedUsers.size() != mentionIds.size()) {
                throw new UserNotExistsException();
            }

            List<CommentMention> commentMentionList = mentionedUsers.stream()
                    .map(user -> CommentMention.builder()
                            .mentionedUser(user)
                            .comment(newComment)
                            .build())
                    .toList();
            commentMentionRepository.saveAll(commentMentionList);

            //푸시 알람을 위한 Event 생성
            eventPublisher.publishEvent(new CommentCreatedEvent(this,
                    post, commenter, newComment, mentionIds));
        }
    }

    private void validateContentMentions(String content, Set<Long> mentionIds) {
        //본문에서 멘션 ID만 추출
        Set<Long> extractedIds = new HashSet<>();
        Set<Long> mentionIdSet = mentionIds == null ? new HashSet<>() : new HashSet<>(mentionIds);
        Pattern pattern = Pattern.compile("@\\[.*?\\]\\(user:(\\d+)\\)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            extractedIds.add(Long.parseLong(matcher.group(1)));
        }

        if (!extractedIds.equals(mentionIdSet)) {
            throw new InvalidMentionException();
        }
    }


    public PostDetailResponseDto getPostDetail(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        List<CommentDetailResponseDto> commentList = commentRepository.findCommentsWithAuthorByPostId(
                post.getId());
        List<PostDetailFileDto> fileList = postImageRepository.findPostDetailFileByPostId(post.getId());

        PostDetailResponseDto response = PostDetailResponseDto.makePostDetailResponse(post, commentList,
                fileList);

        return response;
    }

    public void createPost(long userId, PostCreateRequestDto requestDto, List<MultipartFile> files) {
        Member author = memberRepository.getReferenceById(userId);
        //게시글 생성 및 저장
        Post newPost = Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .member(author)
                .build();

        if (files != null && !files.isEmpty()) {
            //File MetaData 생성
            List<FileDetailDto> fileDetailList = files.stream()
                    .map(file -> s3Uploader.makeMetaData(file, S3DIRNAME.POST_IMAGE.name())).toList();

            // S3 Upload
            List<String> keys = fileDetailList.stream().map(FileDetailDto::getKey).toList();
            s3Uploader.uploadFiles(keys, files);
            try {
                postWriter.savePostAndImages(newPost, fileDetailList);
            } catch (Exception e) {
                keys.forEach(s3Uploader::deleteFile);
            }
        } else {
            postWriter.savePost(newPost);
        }

    }


    public void toggleLike(Long postId, long userId) {
       postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        String countKey = "post:like:" + postId;
        String userSetKey = "post:like:users:" + postId;


        Long result = redisTemplate.execute(
                script, List.of(userSetKey,countKey),
                String.valueOf(userId)
        );
        if(result == 1L){
            eventPublisher.publishEvent(new PostLikeEvent(this,postId,userId,LikeEventType.LIKE));
        }
        else{
            eventPublisher.publishEvent(new PostLikeEvent(this,postId,userId,LikeEventType.CANCEL));
        }
    }

    @Transactional
    public void deletePost(Long postId, long userId) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        //게시글에 대한 삭제 권한 없음
        if (post.getMember().getId() != userId) {
            throw new PostAuthorMisMatchException();
        }

        //언급 알림 삭제
        commentMentionRepository.deleteByPostId(postId);

        // 댓글 삭제
        commentRepository.deleteByPostId(postId);
        // 좋아요 삭제
        postLikeRepository.deleteByPostId(postId);

        //게시물 관련 파일 삭제 예약 후 이미지 삭제
        List<File> files = postImageRepository.findFileByPostId(postId);
        files.forEach(File::markAsDeleted);
        postImageRepository.deleteByPostId(postId);

        //게시물 삭제
        postRepository.deleteById(postId);

        //redis Key 삭제
        try {
            redisTemplate.delete(List.of("post:like:" + postId, "post:like:users:" + postId));
        } catch (Exception e) {
            log.warn("Redis 게시글 좋아요 키 삭제 실패: postId={}", postId, e);
        }
    }


    public void updatePost(Long postId, long userId, PostUpdateRequestDto requestDto,
                           List<MultipartFile> files) {

        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        if (post.getMember().getId() != userId) {
            throw new PostAuthorMisMatchException();
        }
        if (files != null && !files.isEmpty()) {
            //File MetaData 생성
            List<FileDetailDto> fileDetailList = files.stream()
                    .map(file -> s3Uploader.makeMetaData(file, S3DIRNAME.POST_IMAGE.name())).toList();

            // S3 Upload
            List<String> keys = fileDetailList.stream().map(FileDetailDto::getKey).toList();
            s3Uploader.uploadFiles(keys, files);
            try {
                postWriter.updatePostAndImages(postId, fileDetailList, requestDto);
            } catch (Exception e) {
                keys.forEach(s3Uploader::deleteFile);
                throw e;
            }
        } else {
            postWriter.updatePost(postId, requestDto);
        }
    }


    // 게시글 좋아요 목록 불러오기(무한 스크롤 , 최신순, 좋아요순 정렬)

    // 일반 게시글 목록 조회 (검색어 없을 때)
    @Transactional(readOnly = true)
    public PostListResponseDto getPosts(String sort, Long cursor, int size){
        PageRequest pageRequest = PageRequest.of(0, size+1);
        List<Post> posts;

        if("likes".equalsIgnoreCase(sort)){
            Integer likesCursor = getLikesCursor(cursor);
            posts = postRepository.searchLikedPosts(null, cursor, likesCursor, pageRequest);
        } else {
            posts = postRepository.searchLatestPosts(null, cursor, pageRequest);
        }

        boolean hasNext = posts.size() > size;
        if(hasNext){
            posts.remove(size);
        }
        Long nextCursor = posts.isEmpty() ? null : posts.get(posts.size()-1).getId();

        return createPostListResponse(posts, hasNext, nextCursor, size, null);
    }

    // 검색 전용 API 로직 (검색어 있을 때)
    @Transactional(readOnly = true)
    public PostListResponseDto searchPosts(String keyword, String sort, Long cursor, int size){
        PageRequest pageRequest = PageRequest.of(0, size+1);
        List<Post> posts;

        if("likes".equalsIgnoreCase(sort)){
            Integer likesCursor = getLikesCursor(cursor);
            posts = postRepository.searchLikedPosts(keyword, cursor, likesCursor, pageRequest);
        } else {
            posts = postRepository.searchLatestPosts(keyword, cursor, pageRequest);
        }

        boolean hasNext = posts.size() > size;
        if(hasNext){
            posts.remove(size);
        }
        Long nextCursor = posts.isEmpty() ? null : posts.get(posts.size()-1).getId();

        // UI 시안의 "총 개수"를 위한 쿼리 (첫 페이지일 때만 계산)
        Long totalCount = (cursor == null) ? postRepository.countSearchResults(keyword) : null;

        return createPostListResponse(posts, hasNext, nextCursor, size, totalCount);
    }

    // 커서 ID를 통해 좋아요 수 찾기 헬퍼 메서드
    private Integer getLikesCursor(Long cursor) {
        if (cursor == null) return null;
        return postRepository.findById(cursor)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND))
                .getLikeCount();
    }

    // 공통 DTO 응답 생성 메서드
    private PostListResponseDto createPostListResponse(List<Post> posts, boolean hasNext, Long nextCursor, int size, Long totalCount) {
        List<PostImage> allPostImages = postImageRepository.findAllByPostInWithFile(posts);

        Map<Long, List<PostImage>> imageMap = allPostImages.stream()
                .collect(Collectors.groupingBy(pi -> pi.getPost().getId()));

        List<PostListResponseDto.PostSummary> postSummaries = posts.stream().map(post -> {
            List<PostImage> postImages = imageMap.getOrDefault(post.getId(), Collections.emptyList());
            List<File> postFiles = postImages.stream()
                    .map(PostImage::getFile)
                    .toList();

            List<String> thumbnails = postFiles.stream()
                    .limit(3)
                    .map(file -> s3Uploader.getPublicUrl(file.getFileKey()))
                    .toList();

            boolean hasVideo = postFiles.stream()
                    .anyMatch(file -> file.getFileType() == FileType.VIDEO);

            return PostListResponseDto.PostSummary.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .thumbnails(thumbnails)
                    .hasVideo(hasVideo)
                    .totalMediaCount(postFiles.size())
                    .likeCount(post.getLikeCount())
                    .createdAt(post.getCreatedAt().toLocalDate())
                    .nickname(post.getMember().getNickName())
                    .build();
        }).toList();

        return PostListResponseDto.builder()
                .posts(postSummaries)
                .meta(PostListResponseDto.Meta.builder()
                        .hasNext(hasNext)
                        .nextCursor(nextCursor)
                        .size(size)
                        .totalCount(totalCount)
                        .build())
                .build();
    }


}



