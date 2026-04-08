package com.capstone.arfly.notification.repository;

import com.capstone.arfly.common.dto.MedicationAlarmDto;
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
                    m.id, m.title, COALESCE(m.content, '약 복용 시간입니다!'), m.reminderTime, t.id, t.token, t.deviceType
                )
                FROM MedicationReminder m\s
                JOIN m.member mem
                JOIN FcmToken t ON mem = t.member
                WHERE m.reminderTime <= :currentTime\s
                  AND m.active = true\s
                  AND mem.notificationEnabled = true
                  AND (
                      ((m.lastSentAt IS NULL OR m.lastSentAt < :startOfToday)\s
                        AND m.reminderTime >= CAST(m.timeUpdatedAt AS localtime))
                      OR\s
                      (m.lastSentAt < m.timeUpdatedAt\s
                        AND m.reminderTime >= CAST(m.timeUpdatedAt AS localtime))
                  )
            """)//Case 1: 10시에 설정했는데 8시에 울려야하는 경우, Case 2: 10시에 설정했는데 10시 10분에  울려야하는 경우
    //Case 3: 3. 10시에 설정해서 울렸는데 시간 변경으로 11시로해서 또 울려야하는 경우
    List<MedicationAlarmDto> findPendingNotifications(
            @Param("currentTime") LocalTime currentTime,
            @Param("startOfToday") LocalDateTime startOfToday
    );
    //푸시 알람 전송 시간 업데이트
    @Modifying(clearAutomatically = true)
    @Query("UPDATE MedicationReminder m SET m.lastSentAt = :now WHERE m.id IN :ids")
    void updateReminderLastSendAt(@Param("ids") List<Long> ids, @Param("now")LocalDateTime now);
}
