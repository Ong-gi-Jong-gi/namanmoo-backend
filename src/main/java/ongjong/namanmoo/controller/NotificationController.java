package ongjong.namanmoo.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.dto.ApiResponse;
import ongjong.namanmoo.dto.fcm.NotificationRequest;
import ongjong.namanmoo.dto.fcm.TokenRequest;
import ongjong.namanmoo.service.FCMService;
import ongjong.namanmoo.service.FCMTokenService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.token.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
public class NotificationController {
    @Autowired
    private FCMService fcmService;

    @Autowired
    private FCMTokenService fcmTokenService;

    // FCM 토큰 저장
    @PostMapping("/save-token")
    public ApiResponse<String> saveToken(HttpSession session, @RequestBody TokenRequest tokenRequest) {
        fcmTokenService.saveToken(session, tokenRequest.getToken());
        return new ApiResponse<>("200", "Token saved successfully", null);
    }

    // 알림 전송
    @PostMapping("/send-notification")
    public ApiResponse<String> sendNotification(HttpSession session, @RequestBody NotificationRequest notificationRequest) {
        try {
            String token = fcmTokenService.getToken(session);
            if (token == null) {
                return new ApiResponse<>("404", "FCM token is not stored in the session", null);
            }

            notificationRequest.setToken(token); // 세션에서 토큰 설정
            log.info("Sending notification to token: {}", token);
            fcmService.send(notificationRequest);
            return new ApiResponse<>("200", "Notification has been sent.", null);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error sending notification", e);
            return new ApiResponse<>("500", "Error sending notification: " + e.getMessage(), null);
        }
    }
}
