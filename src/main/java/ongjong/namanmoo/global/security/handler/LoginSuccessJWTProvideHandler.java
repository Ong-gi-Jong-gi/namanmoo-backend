package ongjong.namanmoo.global.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.global.security.jwt.service.JwtService;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessJWTProvideHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String loginId = extractLoginId(authentication);
        String accessToken = jwtService.createAccessToken(loginId);
        String refreshToken = jwtService.createRefreshToken();

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        // 한 번만 조회하여 재사용
        Optional<Member> memberOptional = memberRepository.findByLoginId(loginId);
        memberOptional.ifPresent(member -> {
            member.setRefreshToken(refreshToken);
            memberRepository.save(member);
        });

        log.info("로그인에 성공합니다. loginId: {}", loginId);
        log.info("AccessToken 을 발급합니다. AccessToken: {}", accessToken);
        log.info("RefreshToken 을 발급합니다. RefreshToken: {}", refreshToken);

        // 사용자 정보를 포함한 JSON 응답 작성
        memberOptional.ifPresent(member -> {
            try {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String familyId = member.getFamily() != null ? member.getFamily().getFamilyId().toString() : "null";
                String jsonResponse = String.format(
                        "{\"status\": \"200\", \"message\": \"Login Success.\", \"data\": {\"name\": \"%s\", \"nickname\": \"%s\", \"role\": \"%s\", \"userImg\": \"%s\", \"familyId\": \"%s\"}}",
                        member.getName(), member.getNickname(), member.getRole(), member.getMemberImage(), familyId
                );

                response.getWriter().write(jsonResponse);
            } catch (IOException e) {
                log.error("JSON 응답 작성 중 오류 발생", e);
            }
        });
    }

    private String extractLoginId(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
