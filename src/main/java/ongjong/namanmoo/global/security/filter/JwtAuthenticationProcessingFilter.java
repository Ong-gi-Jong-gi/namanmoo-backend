package ongjong.namanmoo.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();//5

    private final String NO_CHECK_URL = "/login";//1

    /*
    * 회원 가입 체크를 위한 로거 포함 인증 열외 경로 함수
    * */
//    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationProcessingFilter.class);
//    private final List<String> NO_CHECK_URLS = Arrays.asList("/login", "/signup", "/");
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//        boolean shouldNotFilter = NO_CHECK_URLS.stream().anyMatch(url -> url.equalsIgnoreCase(request.getRequestURI()));
//        logger.info("Request URI: {}, Should not filter: {}", request.getRequestURI(), shouldNotFilter);
//        return shouldNotFilter;
//    }


    /**
     * 1. 리프레시 토큰이 오는 경우 -> 유효하면 AccessToken 재발급후, 필터 진행 X, 바로 튕기기
     *
     * 2. 리프레시 토큰은 없고 AccessToken만 있는 경우 -> 유저정보 저장후 필터 계속 진행
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request, response);
            return;//안해주면 아래로 내려가서 계속 필터를 진행하게됨
        }

        String refreshToken = jwtService
                .extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null); //2


        if(refreshToken != null){
            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);//3
            return;
        }

        checkAccessTokenAndAuthentication(request, response, filterChain);//4
    }

    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        jwtService.extractAccessToken(request).filter(jwtService::isTokenValid).ifPresent(

                accessToken -> jwtService.extractLoginId(accessToken).ifPresent(

                        loginId -> memberRepository.findByLoginId(loginId).ifPresent(

                                member -> saveAuthentication(member)
                        )
                )
        );

        filterChain.doFilter(request,response);
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

    private void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {


        memberRepository.findByRefreshToken(refreshToken).ifPresent(
                member -> jwtService.sendAccessToken(response, jwtService.createAccessToken(member.getLoginId()))
        );


    }

}
