package com.capstone.arfly;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/")
    public String healthCheck() {

        return "🎉 Arfly 서버 배포 대성공! CI/CD 파이프라인이 완벽하게 작동하고 있습니다. 🎉";

    }
}