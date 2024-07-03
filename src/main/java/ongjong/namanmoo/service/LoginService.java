package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        log.info("Attempting to load user by username: {}", loginId);
        Member member = memberRepository
                .findByLoginId(loginId)
                .orElseThrow(()-> new UsernameNotFoundException("아이디가 없습니다"));

        return User.builder()
                .username(member.getLoginId())
                .password(member.getPassword())
                .roles(member.getLogInRole().name())
                .build();
    }
}
