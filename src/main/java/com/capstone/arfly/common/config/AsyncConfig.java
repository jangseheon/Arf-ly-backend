package com.capstone.arfly.common.config;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public @Nullable AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return ((ex, method, params) -> {
            log.error("[Async 예외 발생] 메서드: {}, 파라미터: {}, 예외: {}", method.getName(), Arrays.toString(params), ex.getMessage(), ex);
        });
    }
}
