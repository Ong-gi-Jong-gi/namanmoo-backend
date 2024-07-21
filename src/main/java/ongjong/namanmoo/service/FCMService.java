package ongjong.namanmoo.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import jakarta.servlet.http.HttpSession;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.fcm.NotificationRequest;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;

@Service
public class FCMService {
    private static final Logger logger = LoggerFactory.getLogger(FCMService.class);
    private final MemberRepository memberRepository;

    public FCMService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void send(final NotificationRequest notificationRequest) throws InterruptedException, ExecutionException {
        Message message = Message.builder()
                .setToken(notificationRequest.getToken())
                .setWebpushConfig(WebpushConfig.builder().putHeader("ttl", "300")
                        .setNotification(new WebpushNotification(notificationRequest.getTitle(),
                                notificationRequest.getMessage()))
                        .build())
                .build();

        String response = FirebaseMessaging.getInstance().sendAsync(message).get();
        logger.info("Sent message: " + response);
    }

    public void saveToken(HttpSession session, String token) {
        session.setAttribute("FCM_TOKEN", token);
    }

    public String getToken(HttpSession session) {
        return (String) session.getAttribute("FCM_TOKEN");
    }

    @Transactional
    public String saveMemberToken(HttpSession session, String token) throws Exception {
        // 해당 아이디 가진 유저가 존재하는지 검사
        Member member = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId())
                .orElseThrow(() -> new RuntimeException("로그인한 멤버를 찾을 수 없습니다."));

        saveToken(session, token);
        return "토큰이 성공적으로 저장되었습니다.";
    }
}
