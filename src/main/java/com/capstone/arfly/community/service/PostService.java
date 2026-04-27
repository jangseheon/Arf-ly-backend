package com.capstone.arfly.community.service;

import com.capstone.arfly.common.constant.S3DIRNAME;
import com.capstone.arfly.common.domain.File;
import com.capstone.arfly.common.dto.FileDetailDto;
import com.capstone.arfly.common.exception.InvalidMentionException;
import com.capstone.arfly.common.exception.PostNotFoundException;
import com.capstone.arfly.common.exception.UserNotExistsException;
import com.capstone.arfly.common.repository.FileRepository;
import com.capstone.arfly.common.util.S3Uploader;
import com.capstone.arfly.community.domain.Comment;
import com.capstone.arfly.community.domain.CommentMention;
import com.capstone.arfly.community.domain.Post;
import com.capstone.arfly.community.domain.PostImage;
import com.capstone.arfly.community.dto.CommentDetailResponseDto;
import com.capstone.arfly.community.dto.CommentRequestDto;
import com.capstone.arfly.community.dto.PostCreateRequestDto;
import com.capstone.arfly.community.dto.PostDetailResponseDto;
import com.capstone.arfly.community.event.CommentCreatedEvent;
import com.capstone.arfly.community.repository.CommentMentionRepository;
import com.capstone.arfly.community.repository.CommentRepository;
import com.capstone.arfly.community.repository.PostImageRepository;
import com.capstone.arfly.community.repository.PostRepository;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.repository.MemberRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PostImageRepository postImageRepository;
    private final S3Uploader s3Uploader;
    private final FileRepository fileRepository;

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
        List<String> fileList = postImageRepository.findFilePathsByPostId(post.getId());

        PostDetailResponseDto response = PostDetailResponseDto.makePostDetailResponse(post, commentList,
                fileList);

        return response;
    }

    @Transactional
    public void createPost(long userId,  PostCreateRequestDto requestDto, List<MultipartFile> files) {
        Member author = memberRepository.getReferenceById(userId);
        //게시글 생성 및 저장
        Post newPost = Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .member(author)
                .build();
        postRepository.save(newPost);

        if(files != null && !files.isEmpty()){
            //File MetaData 생성
            List<FileDetailDto> fileDetailList = files.stream()
                    .map(file -> s3Uploader.makeMetaData(file, S3DIRNAME.POST_IMAGE.name())).toList();

            List<File> fileList = new ArrayList<>();
            List<PostImage> postImages = new ArrayList<>();

            for (int i = 0; i < fileDetailList.size(); i++) {
                FileDetailDto detail = fileDetailList.get(i);

                File fileEntity = File.builder()
                        .fileName(detail.getOriginalFileName())
                        .fileKey(detail.getKey())
                        .fileSize(detail.getFileSize())
                        .fileType(detail.getFileType())
                        .build();

                fileList.add(fileEntity);

                postImages.add(PostImage.builder()
                        .post(newPost)
                        .file(fileEntity)
                        .orderIndex(i)
                        .build());
            }
            postImageRepository.saveAll(postImages);
            fileRepository.saveAll(fileList);

            // S3 Upload
            List<String> keys = fileDetailList.stream().map(FileDetailDto::getKey).toList();
            s3Uploader.uploadFiles(keys,files);
        }

    }
}



