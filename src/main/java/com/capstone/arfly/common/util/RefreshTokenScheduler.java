package com.capstone.arfly.common.util;

import com.capstone.arfly.member.repository.RefreshTokenRepository;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenScheduler {
    private final RefreshTokenRepository refreshTokenRepository;

    //매일 오전 3시마다 만료된 리프레시 토큰을 삭제하는 스케줄러
    @Scheduled(cron = "0 * 3 * * *", zone = "Asia/Seoul")
    public void cleanUpExpiredTokens() {
        log.info("만료된 리프레시 토큰 정리 시작");
        refreshTokenRepository.deleteByExpiredAtBefore(new Date());
        log.info("만료된 리프레시 토큰 정리 완료");
    }

}
