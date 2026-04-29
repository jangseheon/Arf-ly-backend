package com.capstone.arfly.community.util;

import com.capstone.arfly.community.repository.PostRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeSyncScheduler {
    private final PostRepository postRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(
            initialDelay = 1,
            fixedDelay = 5,
            timeUnit = TimeUnit.MINUTES
    )
    public void updatePostLike() {
        log.info("Redis와 DB의 좋아요 수 동기화 작업 시작");
        ScanOptions options = ScanOptions.scanOptions()
                        .match("post:like:[0-9]*")
                                .count(100)
                                        .build();
        redisTemplate.executeWithStickyConnection(connection -> {
            try(Cursor<byte[]> cursor = connection.keyCommands().scan(options)){
                while(cursor.hasNext()){
                    String key = new String(cursor.next());
                    String countStr = redisTemplate.opsForValue().get(key);
                    if(countStr != null){
                        Long postId = Long.parseLong(key.split(":")[2]);
                        int count = Integer.parseInt(countStr);
                        postRepository.updateLikeCount(postId,count);
                    }
                }
            }catch (Exception e){
                log.error("동기화 중 오류 발생",e);
            }
            return null;
        });
        log.info("Redis와 DB의 좋아요 수 동기화 작업 종료");
    }
}
