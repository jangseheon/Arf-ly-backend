package com.capstone.arfly;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ArfLyApplication {

    public static void main(String[] args) {
        // 파일이 없어도 에러를 내지 않고 무시하도록 설정 (로컬 개발 .env를 위해서)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(ArfLyApplication.class, args);
    }

}
