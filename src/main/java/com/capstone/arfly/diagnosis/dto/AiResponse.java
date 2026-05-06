package com.capstone.arfly.diagnosis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiResponse {

    private Prediction prediction;

    @Getter
    @NoArgsConstructor
    public static class Prediction {
        private String disease;
        private String probability;
    }
}
