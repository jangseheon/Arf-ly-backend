package com.capstone.arfly.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.key-path}")
    private String keyPath;

    @PostConstruct
    public void init(){
        try{
            FileInputStream serviceAccount = new FileInputStream(keyPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if(FirebaseApp.getApps().isEmpty()){
                FirebaseApp.initializeApp(options);
                log.info("Firebase Application has been initialized");
            }
        }catch (FileNotFoundException e) {
            log.error("Firebase 키 파일을 찾을 수 없습니다. 경로를 확인하세요: {}", keyPath);
            throw new RuntimeException("Firebase 설정 파일 부재", e);

        } catch (IOException e) {
            log.error("Firebase 인증 정보 스트림을 읽는 중 오류가 발생했습니다.");
            throw new RuntimeException("Firebase 파일을 읽는 중 오류 발생", e);

        } catch (Exception e) {
            log.error("Firebase 초기화 중 예상치 못한 오류 발생: {}", e.getMessage());
            throw new RuntimeException("Firebase 초기화 에러", e);
        }
    }
}
