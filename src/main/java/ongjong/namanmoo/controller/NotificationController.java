package ongjong.namanmoo.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.dto.fcm.NotificationRequest;
import ongjong.namanmoo.dto.fcm.TokenRequest;
import ongjong.namanmoo.service.FCMService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
public class NotificationController {
    private final FCMService fcmService;

    @Autowired
    public NotificationController(FCMService fcmService) {
        this.fcmService = fcmService;
    }

    @PostMapping("/send-notification")
    public String sendNotification(HttpSession session, @RequestBody NotificationRequest notificationRequest) {
        try {
            String token = fcmService.getToken(session);
            if (token == null) {
                return "FCM 토큰이 세션에 저장되어 있지 않습니다.";
            }

            notificationRequest.setToken(token); // 세션에서 가져온 토큰 설정
            fcmService.send(notificationRequest);
            return "Notification has been sent.";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "Error sending notification: " + e.getMessage();
        }
    }

    @PostMapping("/save-token")
    public String saveToken(HttpSession session, @RequestBody TokenRequest tokenRequest) {
        fcmService.saveToken(session, tokenRequest.getToken());
        log.info("Token: {}", tokenRequest.getToken());
        return "Token has been saved successfully.";
    }

    @PostMapping("/get-token")
    public String getToken(HttpSession session) {
        String token = fcmService.getToken(session);
        if (token == null) {
            return "No token found in session.";
        }
        return token;
    }
}
