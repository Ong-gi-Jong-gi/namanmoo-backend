package ongjong.namanmoo.global.security.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.global.security.jwt.service.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final JwtService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 로그인 ID를 추출
        String loginId = extractLoginIdFromAuthentication(authentication);

        // 리프레시 토큰 삭제
        jwtService.destroyRefreshToken(loginId);

        // 리프레시 토큰 쿠키 제거
        Cookie cookie = new Cookie("Authorization-refresh", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // if using HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키 제거
        response.addCookie(cookie);
    }

    private String extractLoginIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        throw new IllegalStateException("로그인 ID 추출 실패");
    }
}