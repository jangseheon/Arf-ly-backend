package com.capstone.arfly.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${AI_MODEL_ADDRESS}")
    private String aiAddress;

    @Bean
    public WebClient aiWebClient(){
        return WebClient.builder().baseUrl(aiAddress).build();
    }
}
