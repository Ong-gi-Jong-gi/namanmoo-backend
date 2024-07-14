package ongjong.namanmoo.service;

import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.member.LoginRequestDto;
import ongjong.namanmoo.dto.member.MemberInfoDto;
import ongjong.namanmoo.dto.member.MemberSignUpDto;
import ongjong.namanmoo.dto.member.MemberUpdateDto;
import ongjong.namanmoo.dto.recap.MemberAndCountDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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

    void update(MemberUpdateDto memberUpdateDto, Optional<MultipartFile> userImg) throws Exception;

    void updatePassword(String checkPassword, String toBePassword) throws Exception;

    void withdraw(String checkPassword) throws Exception;

    MemberInfoDto getMyInfo() throws Exception;

    Member findMemberByLoginId() throws Exception;

    List<MemberAndCountDto> getMemberAndCount(Lucky lucky);

    List<Member> getMembersByLuckyId(Long luckyId);
}