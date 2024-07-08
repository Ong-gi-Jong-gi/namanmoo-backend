package ongjong.namanmoo.service;

import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.member.LoginRequestDto;
import ongjong.namanmoo.dto.member.MemberInfoDto;
import ongjong.namanmoo.dto.member.MemberSignUpDto;
import ongjong.namanmoo.dto.member.MemberUpdateDto;
import org.springframework.transaction.annotation.Transactional;

public interface MemberService {

    /**
     * 회원가입
     * 정보수정
     * 회원탈퇴
     * 정보조회
     */

    void signUp(MemberSignUpDto memberSignUpDto) throws Exception;

    // 아이디 중복 체크
    boolean isDuplicateId(LoginRequestDto loginRequestDto);

    void update(MemberUpdateDto memberUpdateDto) throws Exception;

    void updatePassword(String checkPassword, String toBePassword) throws Exception;

    void withdraw(String checkPassword) throws Exception;

//    MemberInfoDto getInfo(Long memberId) throws Exception;

    MemberInfoDto getMyInfo() throws Exception;

    @Transactional(readOnly = true)
    Member findMemberByLoginId() throws Exception;
}