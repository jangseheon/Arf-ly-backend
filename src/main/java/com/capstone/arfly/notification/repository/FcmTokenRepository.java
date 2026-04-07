package com.capstone.arfly.notification.repository;

import com.capstone.arfly.notification.domain.FcmToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken>findByToken(String token);

    //푸시 알림 발송에 성공한 FCM Token 최신화
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FcmToken t SET t.lastUsedAt = :now WHERE t.id IN :ids")
    void updateTokenLastUsedAt(@Param("ids") List<Long> ids, @Param("now")LocalDateTime now);

    @Modifying
    void deleteByLastUsedAtBefore(LocalDateTime threshold);
}
