package com.capstone.arfly.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenaiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.
        defaultSystem("""
                [작성 조건]
                1. 보호자가 이해하기 쉬운 말로 작성해 주세요.
                2. 의료 진단처럼 단정하지 마세요.
                3. 알레르기 정보가 있다면 관리 방법에 반영해 주세요.
                4. 정확한 진단과 치료는 동물병원 진료가 필요하다는 문장을 포함해 주세요.
                5. 제목 + 설명 형식으로 3~4가지 관리 방법을 작성해 주세요.
                6. 각 관리 방법은 너무 길지 않게 작성해 주세요.
                7. 반려동물의 나이, 품종, 알레르기, 특이사항을 고려해 주세요.
                8. Markdown 문법을 사용하지 마세요. 굵게 표시(**), 제목 표시(#), 글머리 기호(-, *)를 사용하지 마세요.
                9. 각 항목은 번호, 제목, 본문만 작성하세요.
                10. 본문 앞에 "설명", "설명:", "내용", "내용:" 같은 라벨을 붙이지 마세요.
                
                [출력 형식]
                1. 제목
                설명
                
                2. 제목
                설명
                
                3. 제목
                설명
                """)
                .build();
    }
}