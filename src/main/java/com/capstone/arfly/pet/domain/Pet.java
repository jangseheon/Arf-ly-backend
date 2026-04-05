package com.capstone.arfly.pet.domain;

import com.capstone.arfly.common.domain.BaseTimeEntity;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.common.domain.File;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Pet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breeds_id", nullable = false)
    private Breeds breeds;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File profileImage;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Species species;

    @Column(nullable = false)
    private Boolean neutered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    @Column(nullable = false)
    private Integer birth;

    private Double weight;

    private String note;

    public void update(String name, Breeds breeds, File profileImage, Integer birth,
                       Double weight, Boolean neutered, Species species, Sex sex, String note) {
        this.name = name;
        this.breeds = breeds;
        this.profileImage = profileImage;
        this.birth = birth;
        this.weight = weight;
        this.neutered = neutered;
        this.species = species;
        this.sex = sex;
        this.note = note;
    }

}
