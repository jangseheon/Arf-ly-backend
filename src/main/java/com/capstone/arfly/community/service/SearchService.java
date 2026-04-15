package com.capstone.arfly.community.service;

import com.capstone.arfly.common.constant.RedisConstant;
import com.capstone.arfly.community.dto.GetRecentSearchResponseDto;
import com.capstone.arfly.community.dto.GetRecentSearchResponseDto.GetRecentSearchResponseDtoBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final RedisTemplate<String, String> redisTemplate;

    public List<GetRecentSearchResponseDto> getRecentSearchHistory(long userId) {
        String key = generateSearchHistoryKey(userId);
        Set<String> searchSet = redisTemplate.opsForZSet().reverseRange(
                key, 0, RedisConstant.MAX_SEARCH_HISTORY_SIZE - 1
        );
        List<GetRecentSearchResponseDto> response = searchSet.stream()
                .map(keyWord -> GetRecentSearchResponseDto.builder()
                        .keyword(keyWord).build())
                .toList();
        return response;
    }

    private String generateSearchHistoryKey(Long memberId) {
        return RedisConstant.SEARCH_HISTORY_PREFIX + memberId;
    }

    public void removeRecentSearch(long userId, String keyword) {
        String key = generateSearchHistoryKey(userId);
        redisTemplate.opsForZSet().remove(key,keyword);
    }
}
