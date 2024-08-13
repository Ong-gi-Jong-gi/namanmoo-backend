package ongjong.namanmoo.global.security.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ongjong.namanmoo.dto.ApiResponse;
import ongjong.namanmoo.global.security.jwt.service.JwtService;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Autowired
    public AuthController(JwtService jwtService, MemberRepository memberRepository) {
        this.jwtService = jwtService;
        this.memberRepository = memberRepository;
    }

    @PostMapping("/refresh-token")
    public ApiResponse<Object> refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Refresh Token 추출
        String refreshToken = jwtService.extractRefreshToken(request)
                .orElseThrow(() -> new Exception("No refresh token provided"));

        // Refresh Token 검증
        if (!jwtService.isTokenValid(refreshToken)) {
            return new ApiResponse<>("401", "Invalid refresh token", null);
        }

        // Refresh Token을 통해 사용자를 조회하고, 새로운 Access Token 발급
        return memberRepository.findByRefreshToken(refreshToken)
                .map(member -> {
                    String newAccessToken = jwtService.createAccessToken(member.getLoginId());
                    boolean isRefreshTokenNearExpiry = jwtService.isTokenNearExpiry(refreshToken, 0.6); // 60퍼 지났는지 확인

                    // Refresh Token이 만료 60% 가까워지면 새로 발급
                    if (isRefreshTokenNearExpiry) {
                        String newRefreshToken = jwtService.createRefreshToken();
                        jwtService.updateRefreshToken(member.getLoginId(), newRefreshToken);
                        jwtService.sendAccessAndRefreshToken(response, newAccessToken, newRefreshToken);
                        return new ApiResponse<>("200", "New Access Token and Refresh Token issued", null);
                    } else {
                        // 그렇지 않다면 Access Token만 발급
                        jwtService.sendAccessToken(response, newAccessToken);
                        return new ApiResponse<>("200", "New Access Token issued", null);
                    }
                })
                .orElseGet(() -> new ApiResponse<>("401", "Invalid refresh token", null));
    }
}
