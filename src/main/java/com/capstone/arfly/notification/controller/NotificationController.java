package com.capstone.arfly.notification.controller;

import com.capstone.arfly.notification.dto.FcmTokenRequest;
import com.capstone.arfly.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "notification", description = "알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
@Slf4j
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "FCM 토큰 전송", description = "푸시 알림을 위한 FCM 토큰을 전송한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "FCM 토큰 저장 성공(Body 데이터 없음)")
    })
    @PostMapping("/token")
    public ResponseEntity<?> registerFcmToken(@Valid @RequestBody FcmTokenRequest fcmTokenRequest,
                                              @AuthenticationPrincipal
                                              UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationService.registerFcmToken(fcmTokenRequest, userId);
        return ResponseEntity.ok().build();
    }

}
