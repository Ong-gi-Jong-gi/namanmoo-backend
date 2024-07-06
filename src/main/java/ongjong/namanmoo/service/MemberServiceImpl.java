package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.member.MemberInfoDto;
import ongjong.namanmoo.dto.member.MemberSignUpDto;
import ongjong.namanmoo.dto.member.MemberUpdateDto;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 가입 진행
    @Override
    public void signUp(MemberSignUpDto memberSignUpDto) throws Exception {
        Member member = memberSignUpDto.toEntity();
        member.addUserAuthority(); // USER이라는 권한 설정
        member.encodePassword(passwordEncoder);

        // 아이디 중복 체크
        if (memberRepository.findByLoginId(memberSignUpDto.loginId()).isPresent()) {
            throw new Exception("이미 존재하는 아이디 입니다.");
        }

        memberRepository.save(member);
    }

    // 회원 정보 수정
    @Override
    public void update (MemberUpdateDto memberUpdateDto) throws Exception {
        Member member = memberRepository
                .findByLoginId(SecurityUtil.getLoginLoginId())
                .orElseThrow(() -> new Exception("회원이 존재하지 않습니다"));

        // 필드가 존재하는 경우에만 업데이트 진행
        memberUpdateDto.name().ifPresent(member::updateName);
        memberUpdateDto.nickname().ifPresent(member::updateNickname);
    }

    // 비밀번호 변경 -> 비밀번호를 입력 받는다
    @Override
    public void updatePassword(String checkPassword, String newPassword) throws Exception {
        Member member = memberRepository
                .findByLoginId(SecurityUtil.getLoginLoginId())
                .orElseThrow(() -> new Exception("회원이 존재하지 않습니다"));


        if(!member.matchPassword(passwordEncoder, checkPassword) ) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }

        member.updatePassword(passwordEncoder, newPassword);
    }

    // 비밀번호 입력 받아 같을때만 회원탈퇴 진행
    @Override
    public void withdraw(String checkPassword) throws Exception {
        Member member = memberRepository
                .findByLoginId(SecurityUtil.getLoginLoginId())
                .orElseThrow(() -> new Exception("회원이 존재하지 않습니다"));

        if(!member.matchPassword(passwordEncoder, checkPassword) ) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }

        memberRepository.delete(member);
    }

    // 나의 정보를 가져오는 메서드
    @Override
    public MemberInfoDto getMyInfo() throws Exception {
        Member findMember = memberRepository.findByLoginId(SecurityUtil.getLoginLoginId()).orElseThrow(() -> new Exception("회원이 없습니다"));
        return new MemberInfoDto(findMember);
    }

}
