package com.capstone.arfly.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    private String password;

    @Builder.Default
    @Column(nullable = false, unique = true)
    private String nickName = "유저" + UUID.randomUUID();

    @Column(unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String firebaseUid;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String socialId;

    @Builder.Default
    @Column(nullable = false)
    private double latitude = 35.832870;

    @Builder.Default
    @Column(nullable = false)
    private double longitude = 128.757416;

    //null = false;
    private String road_address;

    @Builder.Default
    private boolean notificationEnabled = true;


    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }


    public void updateProfile(String nickName, Double latitude, Double longitude, String roadAddress, boolean notificationEnabled) {
        if (nickName != null) this.nickName = nickName;
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
        this.road_address = roadAddress;
        this.notificationEnabled = notificationEnabled;
    }
}