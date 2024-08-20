package ongjong.namanmoo.global.security.jwt.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
@Setter(value = AccessLevel.PRIVATE)
public class JwtServiceImpl implements JwtService{

    //== jwt.yml에 설정된 값 가져오기 ==//
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessTokenValidityInSeconds;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidityInSeconds;
    @Value("${jwt.access.header}")
    private String accessHeader;
//    @Value("${jwt.refresh.header}") // 쿠키 방식을 변경
//    private String refreshHeader;

    //== 1 ==//
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String USERNAME_CLAIM = "loginId";
    private static final String BEARER = "Bearer ";

    private final MemberRepository memberRepository;

    //== 메서드 ==//
    @Override
    public String createAccessToken(String loginId) {
        return JWT.create() //JWT 토큰을 생성하는 빌더를 반환합니다.
                .withSubject(ACCESS_TOKEN_SUBJECT)
                //빌더를 통해 JWT의 Subject를 정합니다. AccessToken이므로 위에서 설정했던
                //AccessToken의 subject를 합니다.
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenValidityInSeconds * 1000))
                //만료시간을 설정하는 것입니다. 현재 시간 + 저희가 설정한 시간(밀리초) * 1000을 하면
                //현재 accessTokenValidityInSeconds이 80이기 때문에
                //현재시간에 80 * 1000 밀리초를 더한 '현재시간 + 80초'가 설정이 되고
                //따라서 80초 이후에 이 토큰은 만료됩니다.
                .withClaim(USERNAME_CLAIM, loginId)
                //클레임으로는 email 하나만 사용합니다.
                //추가적으로 식별자나, 이름 등의 정보를 더 추가가능합니다.
                //추가하는 경우 .withClaim(클래임 이름, 클래임 값) 으로 설정합니다.
                .sign(Algorithm.HMAC512(secret));
                //HMAC512 알고리즘을 사용하여, 저희가 지정한 secret 키로 암호화 합니다.
    }

    @Override
    public String createRefreshToken() {
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withIssuedAt(new Date(System.currentTimeMillis())) // 발행 시간 설정
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenValidityInSeconds * 1000))
                .sign(Algorithm.HMAC512(secret));
    }

    @Override
    public void updateRefreshToken(String loginId, String refreshToken) {
        memberRepository.findByLoginId(loginId)
                .ifPresentOrElse(
                        member -> {
                            member.setRefreshToken(refreshToken);
                            memberRepository.save(member); // 저장 로직 추가
                        },
                        () -> new Exception("회원 조회 실패")
                );
    }

    @Override
    public void destroyRefreshToken(String loginId) {
        memberRepository.findByLoginId(loginId)
                .ifPresentOrElse(
                        member -> {
                            member.setRefreshToken(null); // 리프레시 토큰 무효화
                            memberRepository.save(member); // 변경 사항 저장
                        },
                        () -> {
                            throw new IllegalStateException("회원 조회 실패"); // 예외 발생
                        }
                );
    }

    @Override
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);

        // Refresh Token을 HttpOnly 쿠키로 설정
//        Cookie refreshTokenCookie = new Cookie("Authorization-refresh", refreshToken);
//        refreshTokenCookie.setHttpOnly(true);
//        refreshTokenCookie.setSecure(true); // HTTPS에서만 전송
//        refreshTokenCookie.setPath("/");
////        refreshTokenCookie.setDomain("localhost");
////        refreshTokenCookie.setMaxAge(300); // 5분 동안 유효
//        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 일주일 동안 유효
//        response.addCookie(refreshTokenCookie);

        // Refresh Token을 HttpOnly 쿠키로 설정하고, SameSite=None을 적용
        String cookieValue = String.format(
                "Authorization-refresh=%s; Path=/; HttpOnly; Secure; Max-Age=%d; SameSite=None",
                refreshToken, 7 * 24 * 60 * 60  // 일주일 유효
        );
        response.setHeader("Set-Cookie", cookieValue);
    }

    @Override
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response, accessToken);
    }

    // HTTP 요청 헤더에서 AccessToken 추출
    @Override
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader)).filter(
                accessToken -> accessToken.startsWith(BEARER)
        ).map(accessToken -> accessToken.replace(BEARER, ""));
    }

    // 쿠키에서 RefreshToken 추출
    @Override
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
//        return Optional.ofNullable(request.getHeader(refreshHeader)).filter(
//                refreshToken -> refreshToken.startsWith(BEARER)
//        ).map(refreshToken -> refreshToken.replace(BEARER, ""));
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> "Authorization-refresh".equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst());
    }

    // 토큰에서 유저정보 추출
    @Override
    public Optional<String> extractLoginId(String accessToken) {
        try {
            return Optional.ofNullable(
                    JWT.require(Algorithm.HMAC512(secret))
                            //토큰의 서명의 유효성을 검사하는데 사용할 알고리즘이 있는
                            //JWT verifier builder를 반환합니다
                            .build().verify(accessToken)
                            //반환된 빌더로 JWT verifier를 생성합니다
                            .getClaim(USERNAME_CLAIM)
                            //claim을 가져옵니다
                            .asString());
        } catch (Exception e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader, accessToken);
    }

//    @Override
//    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
//        response.setHeader(refreshHeader, refreshToken);
//    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secret)).build().verify(token);

            DecodedJWT decodedJWT = JWT.decode(token);
            log.info("Token is valid: {}", decodedJWT.getSubject());
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 Token입니다", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isTokenNearExpiry(String token, double threshold) {
        Date expirationDate = JWT.decode(token).getExpiresAt();
        Date now = new Date();
        long validDuration = expirationDate.getTime() - now.getTime();
        long totalDuration = expirationDate.getTime() - JWT.decode(token).getIssuedAt().getTime();

        return validDuration <= (totalDuration * threshold);
    }
}
