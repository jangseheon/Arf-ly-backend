package com.capstone.arfly.community.util;

import com.capstone.arfly.community.repository.PostRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeSyncScheduler {
    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(
            initialDelay = 1,
            fixedDelay = 5,
            timeUnit = TimeUnit.MINUTES
    )
    public void updatePostLike() {
        String sql = "UPDATE post SET like_count = ? WHERE id = ? ";

        log.info("Redis와 DB의 좋아요 수 동기화 작업 시작");
        ScanOptions options = ScanOptions.scanOptions()
                        .match("post:like:[0-9]*")
                                .count(100)
                                        .build();
        Map<Long,Integer> countUpdateMap = new HashMap<>();
        redisTemplate.executeWithStickyConnection(connection -> {
            try(Cursor<byte[]> cursor = connection.keyCommands().scan(options)){
                while(cursor.hasNext()){
                    String key = new String(cursor.next());
                    String countStr = redisTemplate.opsForValue().get(key);
                    if(countStr != null){
                        Long postId = Long.parseLong(key.split(":")[2]);
                        int count = Integer.parseInt(countStr);
                        countUpdateMap.put(postId,count);
                    }
                }
                List<Object[]> params = countUpdateMap.entrySet().stream().map(
                        e-> new Object[]{e.getValue(),e.getKey()}).toList();
                jdbcTemplate.batchUpdate(sql, params);
            }catch (Exception e){
                log.error("동기화 중 오류 발생",e);
            }
            return null;
        });
        log.info("Redis와 DB의 좋아요 수 동기화 작업 종료");
    }
}
