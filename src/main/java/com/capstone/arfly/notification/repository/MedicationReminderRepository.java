package com.capstone.arfly.notification.repository;

import com.capstone.arfly.common.dto.MedicationAlarmDto;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.notification.domain.MedicationReminder;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Long> {

    //알람 대상자 찾기
    @Query("""
                SELECT new com.capstone.arfly.common.dto.MedicationAlarmDto(
                    m.id, m.title, COALESCE(m.content,"알람이 울립니다!"), m.reminderTime, t.id, t.token, t.deviceType
                )
                FROM MedicationReminder m\s
                JOIN m.member mem
                JOIN FcmToken t ON mem = t.member
                WHERE m.reminderTime <= :currentTime\s
                AND m.active = true\s
                AND mem.notificationEnabled = true
                AND (
                    m.lastSentAt IS NULL OR m.lastSentAt < :startOfToday OR\s
                    (m.lastSentAt < m.timeUpdatedAt AND m.reminderTime >= CAST(m.timeUpdatedAt AS localtime ) ))
           \s""")// 아직 한 번도 보낸 적 없거나, 마지막으로 보낸 게 어제 이전이거나, 오늘 보냈어도, 그 이후에 시간을 수정한 경우
    List<MedicationAlarmDto> findPendingNotifications(
            @Param("currentTime") LocalTime currentTime,
            @Param("startOfToday") LocalDateTime startOfToday
    );

    //푸시 알람 전송 시간 업데이트
    @Modifying(clearAutomatically = true)
    @Query("UPDATE MedicationReminder m SET m.lastSentAt = :now WHERE m.id IN :ids")
    void updateReminderLastSendAt(@Param("ids") List<Long> ids, @Param("now")LocalDateTime now);

    List<MedicationReminder> findByMemberIdOrderByReminderTimeAsc(Long memberId);
}
