package com.capstone.arfly.community.service;

import com.capstone.arfly.common.domain.File;
import com.capstone.arfly.common.dto.FileDetailDto;
import com.capstone.arfly.common.exception.PostFileNotFoundException;
import com.capstone.arfly.common.repository.FileRepository;
import com.capstone.arfly.community.domain.Post;
import com.capstone.arfly.community.domain.PostImage;
import com.capstone.arfly.community.dto.PostUpdateRequestDto;
import com.capstone.arfly.community.repository.PostImageRepository;
import com.capstone.arfly.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PostWriter {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final FileRepository fileRepository;


    @Transactional
    public void savePostAndImages(Post newPost, List<FileDetailDto> fileDetailList){
        postRepository.save(newPost);
        saveImages(newPost, fileDetailList);
    }


    @Transactional
    public void updatePostAndImages(Long postId,List<FileDetailDto> fileDetailDtoList,PostUpdateRequestDto updateData){
        Post post = postRepository.getReferenceById(postId);
        post.updatePost(updateData.getTitle(),updateData.getContent());
        //게시글 삭제할 파일 예약
        deletePostFiles(updateData,post.getId());

        saveImages(post,fileDetailDtoList);
    }
    
    @Transactional
    public void updatePost(Long postId,PostUpdateRequestDto updateData){
        Post post = postRepository.getReferenceById(postId);
        post.updatePost(updateData.getTitle(), updateData.getContent());
        deletePostFiles(updateData, post.getId());
    }

    private void deletePostFiles(PostUpdateRequestDto updateData, Long postId) {
        if(updateData.getDeleteFileIds() != null && !updateData.getDeleteFileIds().isEmpty()){
            List<Long> deleteFilIds = new ArrayList<>(updateData.getDeleteFileIds());
            List<File> files = fileRepository.findByPostId(postId, deleteFilIds);

            if(files.size() != deleteFilIds.size()){
                throw new PostFileNotFoundException();
            }
            files.forEach(File::markAsDeleted);
        }
    }

    private void saveImages(Post newPost, List<FileDetailDto> fileDetailList) {
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
        fileRepository.saveAll(fileList);
        postImageRepository.saveAll(postImages);
    }

    @Transactional
    public void savePost(Post newPost){
        postRepository.save(newPost);
    }

}
