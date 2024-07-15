package ongjong.namanmoo.global.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.global.security.filter.JsonUsernamePasswordAuthenticationFilter;
import ongjong.namanmoo.global.security.filter.JwtAuthenticationProcessingFilter;
import ongjong.namanmoo.global.security.handler.LoginFailureHandler;
import ongjong.namanmoo.global.security.handler.LoginSuccessJWTProvideHandler;
import ongjong.namanmoo.global.security.jwt.service.JwtService;
import ongjong.namanmoo.repository.MemberRepository;
import ongjong.namanmoo.service.LoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginService loginService;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)  // HTTP Basic 비활성화
                .formLogin(AbstractHttpConfigurer::disable)  // 폼 로그인 비활성화
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/signup", "/", "/signup/duplicate", "/login").permitAll() // signup, login 페이지는 인증 없이 접근 가능하도록 설정
                        .anyRequest().authenticated()) // 그 외의 모든 요청은 인증을 필요로 함
//				.formLogin(formLogin -> formLogin
//						.loginPage("/login")
//						.defaultSuccessUrl("/home"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login") // 로그아웃 성공 시 리다이렉트될 URL 설정
                        .invalidateHttpSession(true)) // HTTP 세션 무효화 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 관리 정책 설정 (STATELESS: 세션을 사용하지 않음)
                .cors(cors -> cors.configurationSource(corsConfigurationSource)); // CORS 설정 추가
        http
                .addFilterAfter(jsonUsernamePasswordLoginFilter(), LogoutFilter.class) // 추가 : 커스터마이징 된 필터를 SpringSecurityFilterChain에 등록
                .addFilterBefore(jwtAuthenticationProcessingFilter(), JsonUsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    // bcrypt 기반
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    // 인증 관리자 관련 설정
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() throws Exception {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();

        daoAuthenticationProvider.setUserDetailsService(loginService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());

        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {//2 - AuthenticationManager 등록
        DaoAuthenticationProvider provider = daoAuthenticationProvider();//DaoAuthenticationProvider 사용
        return new ProviderManager(provider);
    }

    @Bean
    public LoginSuccessJWTProvideHandler loginSuccessJWTProvideHandler(){
        return new LoginSuccessJWTProvideHandler(jwtService, memberRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler(){
        return new LoginFailureHandler();
    }

    @Bean
    public JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter() throws Exception {
        JsonUsernamePasswordAuthenticationFilter jsonUsernamePasswordLoginFilter = new JsonUsernamePasswordAuthenticationFilter(objectMapper);
        jsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
        jsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessJWTProvideHandler());
        jsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return jsonUsernamePasswordLoginFilter;
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter(){
        JwtAuthenticationProcessingFilter jsonUsernamePasswordLoginFilter = new JwtAuthenticationProcessingFilter(jwtService, memberRepository);

        return jsonUsernamePasswordLoginFilter;
    }
}
