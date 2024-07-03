package ongjong.namanmoo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ongjong.namanmoo.domain.Family;
import ongjong.namanmoo.domain.Member;
import ongjong.namanmoo.dto.family.FamilyMemberDto;
import ongjong.namanmoo.repository.FamilyRepository;
import ongjong.namanmoo.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Family createFamily(String familyName, int maxFamilySize, String ownerRole, Long memberId) {
        // 현재 로그인한 유저 정보를 가져옴
        // TODO: 로그인 구현 후 수정
//        String loginId = getCurrentMemberLogin();
//        Member familyOwner = memberRepository.findByLoginId(loginId)
//                .orElseThrow(() -> new IllegalArgumentException("User not found: " + loginId));

        // TODO: memberId 삭제 조치 필요
        Member familyOwner = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        Family family = new Family();
        family.setFamilyName(familyName);
        family.setMaxFamilySize(maxFamilySize);
        family.setFamilyOwnerId(familyOwner.getMemberId());
        family.setCurrentFamilySize(1);

        // 가족 소유자의 역할 설정
        familyOwner.setRole(ownerRole);
        family.getMembers().add(familyOwner);

        // 초대 코드 생성 및 중복 체크
        generateUniqueInviteCode(family);

        return familyRepository.save(family);
    }

//    // TODO: 로그인 구현 후 수정 필요
//    private String getCurrentMemberLogin() {
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (principal instanceof UserDetails) {
//            return ((UserDetails) principal).getUsername();
//        } else {
//            return principal.toString();
//        }
//    }

    private void generateUniqueInviteCode(Family family) {
        boolean isUnique = false;
        while (!isUnique) {
            family.generateInviteCode();
            isUnique = FamilyRepository.findByInviteCode(family.getInviteCode()).isEmpty();
        }
    }

//    // 초대 URL 생성
//    public String createInviteUrl(Family family) {
//        String inviteCode = family.getInviteCode();
//        return "https://localhost/family?code=" + inviteCode;
//    }

    public Optional<Family> findFamilyByInviteCode(String inviteCode) {
        return FamilyRepository.findByInviteCode(inviteCode);
    }

    @Transactional
    public void addMemberToFamily(Long memberId, Long familyId, String role) {
        // TODO: 로그인 구현 후 수정
//        String loginId = getCurrentMemberLogin();
//        Member member = memberRepository.findByLoginId(loginId)
//                .orElseThrow(() -> new IllegalArgumentException("Member not found with loginId: " + loginId));

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("Family not found with id: " + familyId));

        member.setFamily(family);
        member.setRole(role);
        family.getMembers().add(member);

        familyRepository.save(family);
    }

    public List<FamilyMemberDto> getFamilyMembersInfo(String familyId) {
        Family family = familyRepository.findById(Long.valueOf(familyId))
                .orElseThrow(() -> new IllegalArgumentException("Family not found with id: " + familyId));

        List<Member> members = family.getMembers();
        List<FamilyMemberDto> memberListDto = new ArrayList<>();
        for (Member member : members) {
            FamilyMemberDto memberDto = new FamilyMemberDto();
            memberDto.setName(member.getName());
            memberDto.setNickname(member.getNickname());
            memberDto.setRole(member.getRole());
            memberDto.setUserImg(member.getMemberImage());
            memberListDto.add(memberDto);
        }
        return memberListDto;
    }
}
