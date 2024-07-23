package ongjong.namanmoo.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class FCMTokenService {
    public void saveToken(HttpSession session, String token) {
        session.setAttribute("FCM_TOKEN", token);
    }

    public String getToken(HttpSession session) {
        return (String) session.getAttribute("FCM_TOKEN");
    }
}
