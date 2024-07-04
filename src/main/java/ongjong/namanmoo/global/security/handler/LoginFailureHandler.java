package ongjong.namanmoo.global.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

@Slf4j
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
//        response.setStatus(HttpServletResponse.SC_OK);//보안을 위해 로그인 오류지만 200 반환

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //401 인증 실패
        log.info("로그인 실패");

        // JSON 응답 작성
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = "{\"status\": \"401\", \"message\": \"Login Failed.\"}";

        response.getWriter().write(jsonResponse);
    }
}