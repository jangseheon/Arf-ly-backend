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
        // 필드 마스크 설정(Place Details, Nearby Search 따로)
        // Place Details
        String detailFields = "id," +
                "displayName," +
                "shortFormattedAddress," +
                "photos," +
                "regularOpeningHours," +
                "nationalPhoneNumber";

        // Nearby Search
        String searchFields = "places.id," +
                "places.displayName," +
                "places.location," +
                "places.shortFormattedAddress," +
                "places.photos," +
                "places.regularOpeningHours";

        String fieldMask = detailFields + "," + searchFields;

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
