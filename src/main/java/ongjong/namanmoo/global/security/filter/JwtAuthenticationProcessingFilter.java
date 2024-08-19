package ongjong.namanmoo.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.global.security.jwt.service.JwtService;
import ongjong.namanmoo.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();//5

//    private final String NO_CHECK_URL = "/login";//1

    /**
     * Access가 유효하지 않은 경우 -> api/refresh-token으로 가서 refresh 토큰이 유효하면 Access 재발급
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Request URI: {}", request.getRequestURI()); // 로그 추가
        String requestURI = request.getRequestURI();
        // 인증이 필요 없는 경로 리스트
        List<String> excludeUrls = Arrays.asList("/signup", "/signup/duplicate", "/login", "/logout", "/api/refresh-token", "/");

        // excludeUrls에 있는 URI는 필터링을 건너뜀
        if (excludeUrls.contains(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Access Token 검증
        String accessToken = jwtService.extractAccessToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        if (accessToken != null) {
            log.info("Access token is valid, proceeding with authentication.");
            checkAccessTokenAndAuthentication(request, response, filterChain);
            return;
        }

        // Access Token이 유효하지 않다면 401 Unauthorized 응답
        log.info("Access token is invalid or missing.");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = "{\"status\": \"401\", \"message\": \"Unauthorized: Access token is invalid or missing.\"}";
        response.getWriter().write(jsonResponse);
    }

    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        jwtService.extractAccessToken(request).filter(jwtService::isTokenValid).ifPresent(
//            accessToken -> jwtService.extractLoginId(accessToken).ifPresent(
//                loginId -> memberRepository.findByLoginId(loginId).ifPresent(
//
//                        member -> saveAuthentication(member)
//                )
//            )
//        );
//        filterChain.doFilter(request,response);
        Optional<String> accessToken = jwtService.extractAccessToken(request)
                .filter(jwtService::isTokenValid);

        if (accessToken.isPresent()) {
            jwtService.extractLoginId(accessToken.get()).ifPresent(
                    loginId -> memberRepository.findByLoginId(loginId).ifPresent(
                            member -> saveAuthentication(member)
                    )
            );
            filterChain.doFilter(request, response); // 유효한 경우에만 다음 필터로 이동
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String jsonResponse = "{\"status\": \"401\", \"message\": \"Access Token is invalid.\"}";
            response.getWriter().write(jsonResponse);
        }
    }

    private void saveAuthentication(Member member) {
        UserDetails user = User.builder()
                .username(member.getLoginId())
                .password(member.getPassword())
                .roles(member.getLogInRole().name())
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,authoritiesMapper.mapAuthorities(user.getAuthorities()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();//5
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

//    private void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) throws IOException {
//        Optional<Member> member = memberRepository.findByRefreshToken(refreshToken);
//
//        if (member.isPresent()) {
//            String newAccessToken = jwtService.createAccessToken(member.get().getLoginId());
//            jwtService.sendAccessToken(response, newAccessToken);
//            log.info("New Access Token issued: {}", newAccessToken);
//        } else {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setContentType("application/json");
//            response.setCharacterEncoding("UTF-8");
//            String jsonResponse = "{\"status\": \"401\", \"message\": \"Refresh Token is invalid.\"}";
//            response.getWriter().write(jsonResponse);
//        }
//    }
}
