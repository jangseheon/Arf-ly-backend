package com.capstone.arfly.notification.domain;

import com.capstone.arfly.common.domain.BaseTimeEntity;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.notification.dto.CreateMedicationReminderRequest;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import java.time.LocalTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicationReminder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalTime reminderTime;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    //마지막으로 알림을 발송한 일시
    private LocalDateTime lastSentAt;

    // 알람 시간이 마지막으로 수정된 일시
    @Builder.Default
    private LocalDateTime timeUpdatedAt = LocalDateTime.now();


    public void updateTitle(String title){
        this.title = title;
    }

    public void updateContent(String content){
        this.content = content;
    }
    public void updateReminderTime(LocalTime reminderTime){
        if (!this.reminderTime.equals(reminderTime)) {
            this.reminderTime = reminderTime;
            this.timeUpdatedAt = LocalDateTime.now();
        }
    }

    public static MedicationReminder create(CreateMedicationReminderRequest reminderRequest ,Member member) {
       return  MedicationReminder.builder().title(reminderRequest.getTitle())
                .content(reminderRequest.getMemo())
                .reminderTime(reminderRequest.getReminderTime())
                .member(member).build();
    }


}
