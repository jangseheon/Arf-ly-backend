package com.capstone.arfly.hospital.config;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.maps.places.v1.PlacesClient;
import com.google.maps.places.v1.PlacesSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class GoogleMapConfig {
    @Value("${GOOGLE_MAP_KEY}")
    private String apiKey;

    @Bean(destroyMethod = "close")
    public PlacesClient placesClient() throws IOException {
        // 필드 마스크 설정(장소 id, 병원 이름, 병원 위치, 도로명주소, 사진, 오픈시간)
        String fieldMask = "places.id,places.displayName,places.location,places.shortFormattedAddress,places.photos,places.regularOpeningHours";

        PlacesSettings settings = null;

        try {
            settings = PlacesSettings.newBuilder()
                    .setCredentialsProvider(NoCredentialsProvider.create())
                    .setHeaderProvider(() -> {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("X-Goog-Api-Key", apiKey);
                        headers.put("X-Goog-FieldMask", fieldMask);
                        return headers;
                    }).build();
        }catch (Exception e){
            log.error("구글 맵 초기 설정 오류 발생! 원인 : {}", e.getMessage());
            throw new RuntimeException("구글 맵 클라이언트를 생성할 수 없습니다.", e);
        }

        log.info("구글 맵 api 초기 설정 완료!");

        return PlacesClient.create(settings);
    }
}
