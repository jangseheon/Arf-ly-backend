package com.capstone.arfly.common.util;

import com.capstone.arfly.common.domain.FileType;
import com.capstone.arfly.common.dto.FileDetailDto;
import com.capstone.arfly.common.exception.BusinessException;
import com.capstone.arfly.common.exception.ErrorCode;
import com.capstone.arfly.common.repository.FileRepository;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    private final S3Template s3Template;
    private final FileRepository fileRepository;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxSizeByte;

    @Value("${spring.servlet.multipart.max-request-size}")
    private DataSize requestMaxByte;

    @Value("${file.max-image-size}") // 이미지 파일 업로드 용량 제한을 위해서
    private DataSize maxImageSize;

    // 이미지 확장자
    private static final Set<String> IMAGE_EXTENSIONS = Set.of( // pdf는 이미지 아니라서 뻄.
            "jpg","jpeg","png","gif","bmp","webp","heic"
    );

    // 비디오 확장자 (게시판에 동영상 가능했던 것 같아서)
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4","mov","avi","mkv","webm"
    );

    public FileType getFileType(String fileName) {
        String extension = getExtension(fileName);
        if(IMAGE_EXTENSIONS.contains(extension)) {
            return FileType.IMAGE;
        } else if(VIDEO_EXTENSIONS.contains(extension)) {
            return FileType.VIDEO;
        } else {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    // 파일 메타데이터 생성
    public FileDetailDto makeMetaData(MultipartFile file, String dirName) {
        validateFile(file);
        String originalFilename = file.getOriginalFilename();

        String extension = getExtension(originalFilename);
        String baseName = getBaseName(originalFilename).replaceAll("[^a-zA-Z0-9._-]", "_");

        String key = dirName + "/" + UUID.randomUUID() + "_" + baseName
                + (extension.isEmpty() ? "" : "." + extension);

        return FileDetailDto.builder()
                .originalFileName(originalFilename)
                .key(key)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .fileType(getFileType(originalFilename))
                .build();

    }

    // 단일 파일 업로드
    public void uploadFile(String upLoadKey, MultipartFile file) {
        validateFile(file);
        fileUpLoad(file, upLoadKey);
    }

    // 여러 파일 업로드 실패시 기존 파일 삭제
    public void uploadFiles(List<String> upLoadKeys,List<MultipartFile> files) {
        List<String> successfulKeys = new ArrayList<>();
        long totalSize = getTotalSize(files);
        if(totalSize > requestMaxByte.toBytes()) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        try{
            IntStream.range(0, files.size())
                    .forEach(i -> {
                        uploadFile(upLoadKeys.get(i), files.get(i));
                        successfulKeys.add(upLoadKeys.get(i));
                    });
        }
        catch(BusinessException e){
            cleanUpS3File(successfulKeys);
            throw e;
        }
    }

    // 단일 파일 수정
    public void correctFile(String upLoadKey, MultipartFile file){
        uploadFile(upLoadKey, file);
    }

    // 여러 파일 수정
    public void correctFiles(List<String> keys,List<MultipartFile> files) {
        IntStream.range(0, files.size()).forEach(i->
                uploadFile(keys.get(i),files.get(i))
        );
    }

    public String getUrlFile(String key){
        validateKey(key);
        try{
            return s3Template.createSignedGetURL(bucket,key, Duration.ofMinutes(10)).toString();
        }catch(S3Exception e){
            log.error("S3 파일 가져오기에 실패했습니다. key: {}, error: {}", key, e.getMessage());
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    // 반려동물 프로필 , 게시글 이미지 같은 항상 띄워둬야 하는 퍼블릭 url
    public String getPublicUrl(String key){
        validateKey(key);
        return String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucket, key);
    }

    // 여러 파일에 대한 url 반환 실패시 전부 실패
    public List<String> getUrlFiles(List<String> keys){
        if(keys == null || keys.isEmpty()){
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return keys.stream().map(this::getUrlFile).collect(Collectors.toList());
    }

    // 스케줄러에 의해서 파일 자동으로 자정에 삭제할려고 함
    public void deleteFile(String key){
        validateKey(key);
        try{
            s3Template.deleteObject(bucket,key);
        }catch(S3Exception e){
            log.error("S3 File Delete Exeption. key: {}, error: {}", key, e.getMessage());
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    //파일+키 검증
    private void validateKey(String key){
        if(!StringUtils.hasText(key)){
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    private void validateFile(MultipartFile file){
        if(file == null || file.isEmpty()){
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        String originalFilename = file.getOriginalFilename();
        if(originalFilename == null || originalFilename.isBlank()){
            throw new BusinessException(ErrorCode.FILENAME_MISSING);
        }

        String extension = getExtension(originalFilename);
        boolean isImage = IMAGE_EXTENSIONS.contains(extension);
        boolean isVideo = VIDEO_EXTENSIONS.contains(extension);
        if(!isImage && !isVideo){
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE); // 지원하지 않는 확장자
        }

        long fileSize = file.getSize();
        // 이미지파일은 50MB로 제한
        if(isImage&&fileSize > maxImageSize.toBytes()){
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        if(isVideo&&fileSize > maxSizeByte.toBytes()){
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    //파일이름에서확장자 가져오기
    private String getExtension(String filename){
        if(filename == null || filename.isBlank()){
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if(lastDotIndex == -1 || lastDotIndex == filename.length() - 1){
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    // 토탈 파일 사이즈 체크
    private long getTotalSize(List<MultipartFile> files){
        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .mapToLong(MultipartFile::getSize)
                .sum();
    }

    //파일 업로드 S3에다
    private void fileUpLoad(MultipartFile file, String key) {
        try{
            s3Template.upload(bucket, key, file.getInputStream());
        } catch(IOException e){
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        catch(S3Exception e){
            log.error("S3 파일 업로드 실패. key: {}, error: {}", key, e.getMessage());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
    // S3 업로드 도중 실패시 성공한 파일들 S3에서 제거 (롤백개념)
    private void cleanUpS3File(List<String> keysToDelete){
        if(keysToDelete == null || keysToDelete.isEmpty()){return;}
        keysToDelete.forEach(this::deleteFile);
    }

    //파일이름에서 확장자를 제외한 본래 이름만 가져오기
    private String getBaseName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex == -1) return originalFilename;
        return originalFilename.substring(0, lastDotIndex);
    }

}
