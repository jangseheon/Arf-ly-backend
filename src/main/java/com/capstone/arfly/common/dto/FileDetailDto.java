package com.capstone.arfly.common.dto;

import com.capstone.arfly.common.domain.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FileDetailDto {
    private String originalFileName;
    private String key;
    private String contentType;
    private Long fileSize;
    private FileType fileType;
}
