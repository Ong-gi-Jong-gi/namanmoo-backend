package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import ongjong.namanmoo.domain.Lucky;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.member.LoginRequestDto;
import ongjong.namanmoo.dto.member.MemberInfoDto;
import ongjong.namanmoo.dto.member.MemberSignUpDto;
import ongjong.namanmoo.dto.member.MemberUpdateDto;
import ongjong.namanmoo.dto.recapMember.MemberAndCountDto;
import ongjong.namanmoo.global.security.util.SecurityUtil;
import ongjong.namanmoo.repository.AnswerRepository;
import ongjong.namanmoo.repository.LuckyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final AnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AwsS3Service awsS3Service;
    private final LuckyRepository luckyRepository;

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

    // 아이디 중복 체크
    @Override
    public boolean isDuplicateId(LoginRequestDto loginRequestDto) {
        return memberRepository.existsByLoginId(loginRequestDto.getLoginId());
    }

    // 회원 정보 수정
    @Override
    public void update(MemberUpdateDto memberUpdateDto, Optional<MultipartFile> userImg) throws Exception {
        Member member = memberRepository
                .findByLoginId(SecurityUtil.getLoginLoginId())
                .orElseThrow(() -> new Exception("회원이 존재하지 않습니다"));

        // 요청에서 필드가 존재하는 경우에만 업데이트 진행
        memberUpdateDto.name().ifPresent(member::setName);
        memberUpdateDto.nickname().ifPresent(member::setNickname);
        memberUpdateDto.role().ifPresent(member::setRole);

        // 파일을 전송했을 경우에만 S3 파일 업로드 수행
        if (userImg.isPresent() && !userImg.get().isEmpty()) {
            try {
                String imagePath = awsS3Service.uploadFile(userImg.get());
                member.setMemberImage(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("S3 업로드 중 에러가 발생했습니다.", e);
            }
        }

        memberRepository.save(member);
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

    @Transactional(readOnly = true)
    public Member findMemberByLoginId() throws Exception{
        return memberRepository.findByLoginId(SecurityUtil.getLoginLoginId()).orElseThrow(() -> new Exception("회원이 없습니다"));
    }

    // member 정보와 각 member에 대한 답변 입력 횟수 반환
    @Transactional(readOnly = true)
    public List<MemberAndCountDto> getMemberAndCount(Lucky lucky) {
        String startDate = lucky.getChallengeStartDate();
        Long familyId = lucky.getFamily().getFamilyId();
        List<Member> memberList = memberRepository.findByFamilyFamilyId(familyId);
        List<MemberAndCountDto> memberCountList = new ArrayList<>();
        for (Member member : memberList) {
            int count = 0;
            String currentDate = startDate;
            for (int i = 0; i < lucky.getLifetime().getDays(); i++) {
                if (answerRepository.existsByMemberAndCreateDateAndAnswerContentIsNotNull(member, currentDate)) {
                    count++;
                }
                currentDate = DateUtil.getInstance().addDaysToStringDate(currentDate, 1);
            }
            memberCountList.add(new MemberAndCountDto(member, count));
        }
        return memberCountList;
    }

    // luckId로 해당 가족의 member 모두 반환
    @Transactional(readOnly = true)
    public List<Member> getMembersByLuckyId(Long luckyId) {
        Lucky lucky = luckyRepository.findById(luckyId).get();
        Long familyId = lucky.getFamily().getFamilyId();
        return memberRepository.findByFamilyFamilyId(familyId);
    }
}