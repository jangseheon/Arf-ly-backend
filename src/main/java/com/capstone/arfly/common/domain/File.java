package com.capstone.arfly.common.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class File extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, unique = true)
    private String fileKey;

    @Column(nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    public void markAsDeleted() {
        this.deleted = true;
    }
}
